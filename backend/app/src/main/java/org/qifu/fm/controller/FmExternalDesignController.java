package org.qifu.fm.controller;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.externalapi.FmExternalDesignQueryService;
import org.qifu.fm.dto.external.FmExternalFormTemplateRequest;
import org.qifu.fm.dto.external.FmExternalFormTemplateView;
import org.qifu.fm.dto.external.FmExternalProcessFormsRequest;
import org.qifu.fm.dto.external.FmExternalProcessFormsView;
import org.qifu.fm.dto.external.FmExternalRequest;
import org.qifu.fm.dto.external.FmExternalResponse;
import org.qifu.fm.filter.FmExternalApiAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/fm/external/v1/design")
public class FmExternalDesignController {

	private final FmExternalDesignQueryService designQueryService;

	public FmExternalDesignController(FmExternalDesignQueryService designQueryService) {
		this.designQueryService = designQueryService;
	}

	@PostMapping("/processes/forms")
	public ResponseEntity<FmExternalResponse<FmExternalProcessFormsView>> processForms(
			@RequestBody FmExternalRequest<FmExternalProcessFormsRequest> body,
			HttpServletRequest request) {
		return response(request, () -> designQueryService.processForms(
				body == null ? null : body.data()), "PROCESS_NOT_FOUND");
	}

	@PostMapping("/forms/template")
	public ResponseEntity<FmExternalResponse<FmExternalFormTemplateView>> formTemplate(
			@RequestBody FmExternalRequest<FmExternalFormTemplateRequest> body,
			HttpServletRequest request) {
		return response(request, () -> designQueryService.formTemplate(
				body == null ? null : body.data()), "FORM_NOT_FOUND");
	}

	private <T> ResponseEntity<FmExternalResponse<T>> response(HttpServletRequest request,
			ExternalQuery<T> query, String notFoundCode) {
		String requestId = String.valueOf(request.getAttribute(
				FmExternalApiAuthenticationFilter.REQUEST_ID_ATTRIBUTE));
		try {
			T value = query.execute();
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, notFoundCode,
								"The requested resource was not found."));
			}
			return ResponseEntity.ok(FmExternalResponse.success(requestId, value));
		} catch (ServiceException exception) {
			return ResponseEntity.badRequest().body(FmExternalResponse.error(requestId,
					"REQUEST_INVALID", exception.getMessage()));
		} catch (Exception exception) {
			return ResponseEntity.internalServerError().body(FmExternalResponse.error(
					requestId, "INTERNAL_ERROR", "The request could not be completed."));
		}
	}

	@FunctionalInterface
	private interface ExternalQuery<T> {
		T execute() throws ServiceException;
	}
}
