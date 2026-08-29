package org.qifu.fm.controller;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.externalapi.FmExternalApiConflictException;
import org.qifu.fm.domain.externalapi.FmExternalRequestSubmitService;
import org.qifu.fm.domain.externalapi.FmExternalRequestStatusService;
import org.qifu.fm.dto.external.FmExternalRequest;
import org.qifu.fm.dto.external.FmExternalRequestSubmitRequest;
import org.qifu.fm.dto.external.FmExternalRequestSubmitView;
import org.qifu.fm.dto.external.FmExternalRequestStatusRequest;
import org.qifu.fm.dto.external.FmExternalRequestStatusView;
import org.qifu.fm.dto.external.FmExternalResponse;
import org.qifu.fm.filter.FmExternalApiAuthenticationFilter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/fm/external/v1/runtime/requests")
public class FmExternalRequestController {

	private final FmExternalRequestSubmitService submitService;
	private final FmExternalRequestStatusService statusService;

	public FmExternalRequestController(FmExternalRequestSubmitService submitService,
			FmExternalRequestStatusService statusService) {
		this.submitService = submitService;
		this.statusService = statusService;
	}

	@PostMapping("/submit")
	public ResponseEntity<FmExternalResponse<FmExternalRequestSubmitView>> submit(
			@RequestHeader(name = "Idempotency-Key", required = false)
			String idempotencyKey,
			@RequestBody FmExternalRequest<FmExternalRequestSubmitRequest> body,
			HttpServletRequest request) {
		String requestId = String.valueOf(request.getAttribute(
				FmExternalApiAuthenticationFilter.REQUEST_ID_ATTRIBUTE));
		try {
			FmExternalRequestSubmitView value = submitService.submit(
					body == null ? null : body.requestTime(),
					body == null ? null : body.data(), idempotencyKey);
			return ResponseEntity.ok().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.success(requestId, value));
		} catch (FmExternalApiConflictException exception) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.error(requestId, exception.getCode(),
							exception.getMessage()));
		} catch (ServiceException exception) {
			return ResponseEntity.unprocessableEntity().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.error(requestId, "SUBMIT_REJECTED",
							exception.getMessage()));
		} catch (Exception exception) {
			return ResponseEntity.internalServerError().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.error(requestId, "INTERNAL_ERROR",
							"The request could not be completed."));
		}
	}

	@PostMapping("/status")
	public ResponseEntity<FmExternalResponse<FmExternalRequestStatusView>> status(
			@RequestBody FmExternalRequest<FmExternalRequestStatusRequest> body,
			HttpServletRequest request) {
		String requestId = String.valueOf(request.getAttribute(
				FmExternalApiAuthenticationFilter.REQUEST_ID_ATTRIBUTE));
		try {
			FmExternalRequestStatusView value = statusService.status(
					body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.cacheControl(CacheControl.noStore())
						.body(FmExternalResponse.error(requestId, "REQUEST_NOT_FOUND",
								"The requested resource was not found."));
			}
			return ResponseEntity.ok().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.success(requestId, value));
		} catch (ServiceException exception) {
			return ResponseEntity.badRequest().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.error(requestId, "STATUS_LOOKUP_KEY_INVALID",
							exception.getMessage()));
		} catch (Exception exception) {
			return ResponseEntity.internalServerError().cacheControl(CacheControl.noStore())
					.body(FmExternalResponse.error(requestId, "INTERNAL_ERROR",
							"The request could not be completed."));
		}
	}
}
