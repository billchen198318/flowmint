package org.qifu.fm.controller;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.externalapi.FmExternalOrganizationQueryService;
import org.qifu.fm.domain.externalapi.FmExternalApiConflictException;
import org.qifu.fm.dto.external.FmExternalDepartmentDetailRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentHeadView;
import org.qifu.fm.dto.external.FmExternalDepartmentEmployeesRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentEmployeesView;
import org.qifu.fm.dto.external.FmExternalDepartmentTreeRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentTreeView;
import org.qifu.fm.dto.external.FmExternalDepartmentView;
import org.qifu.fm.dto.external.FmExternalEmployeeDetailRequest;
import org.qifu.fm.dto.external.FmExternalEmployeeDepartmentHeadView;
import org.qifu.fm.dto.external.FmExternalEmployeeDepartmentsView;
import org.qifu.fm.dto.external.FmExternalEmployeeApprovalLevelView;
import org.qifu.fm.dto.external.FmExternalEmployeeManagerView;
import org.qifu.fm.dto.external.FmExternalEmployeeOrganizationRequest;
import org.qifu.fm.dto.external.FmExternalEmployeeParentDepartmentView;
import org.qifu.fm.dto.external.FmExternalEmployeeView;
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
@RequestMapping("/api/fm/external/v1/org")
public class FmExternalOrganizationController {

	private final FmExternalOrganizationQueryService organizationQueryService;

	public FmExternalOrganizationController(
			FmExternalOrganizationQueryService organizationQueryService) {
		this.organizationQueryService = organizationQueryService;
	}

	@PostMapping("/departments/detail")
	public ResponseEntity<FmExternalResponse<FmExternalDepartmentView>> departmentDetail(
			@RequestBody FmExternalRequest<FmExternalDepartmentDetailRequest> body,
			HttpServletRequest servletRequest) {
		String requestId = String.valueOf(servletRequest.getAttribute(
				FmExternalApiAuthenticationFilter.REQUEST_ID_ATTRIBUTE));
		try {
			FmExternalDepartmentView value = organizationQueryService.departmentDetail(
					body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "ORG_UNIT_NOT_FOUND",
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

	@PostMapping("/departments/primary-head")
	public ResponseEntity<FmExternalResponse<FmExternalDepartmentHeadView>> primaryHead(
			@RequestBody FmExternalRequest<FmExternalDepartmentDetailRequest> body,
			HttpServletRequest servletRequest) {
		String requestId = requestId(servletRequest);
		try {
			FmExternalDepartmentHeadView value = organizationQueryService
					.primaryDepartmentHead(body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "ORG_UNIT_NOT_FOUND",
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

	@PostMapping("/departments/employees")
	public ResponseEntity<FmExternalResponse<FmExternalDepartmentEmployeesView>> departmentEmployees(
			@RequestBody FmExternalRequest<FmExternalDepartmentEmployeesRequest> body,
			HttpServletRequest servletRequest) {
		String requestId = requestId(servletRequest);
		try {
			FmExternalDepartmentEmployeesView value = organizationQueryService
					.departmentEmployees(body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "ORG_UNIT_NOT_FOUND",
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

	@PostMapping("/departments/tree")
	public ResponseEntity<FmExternalResponse<FmExternalDepartmentTreeView>> departmentTree(
			@RequestBody FmExternalRequest<FmExternalDepartmentTreeRequest> body,
			HttpServletRequest servletRequest) {
		String requestId = requestId(servletRequest);
		try {
			FmExternalDepartmentTreeView value = organizationQueryService
					.departmentTree(body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "ORG_UNIT_NOT_FOUND",
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

	@PostMapping("/employees/detail")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeView>> employeeDetail(
			@RequestBody FmExternalRequest<FmExternalEmployeeDetailRequest> body,
			HttpServletRequest servletRequest) {
		String requestId = requestId(servletRequest);
		try {
			FmExternalEmployeeView value = organizationQueryService.employeeDetail(
					body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "EMPLOYEE_NOT_FOUND",
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

	@PostMapping("/employees/departments")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeDepartmentsView>> employeeDepartments(
			@RequestBody FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest servletRequest) {
		return employeeResponse(body, servletRequest,
				organizationQueryService::employeeDepartments);
	}

	@PostMapping("/employees/primary-department-head")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeDepartmentHeadView>> employeePrimaryDepartmentHead(
			@RequestBody FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest servletRequest) {
		return employeeResponse(body, servletRequest,
				organizationQueryService::employeePrimaryDepartmentHead);
	}

	@PostMapping("/employees/parent-department")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeParentDepartmentView>> employeeParentDepartment(
			@RequestBody FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest servletRequest) {
		return employeeResponse(body, servletRequest,
				organizationQueryService::employeeParentDepartment);
	}

	@PostMapping("/employees/approval-level")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeApprovalLevelView>> employeeApprovalLevel(
			@RequestBody FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest servletRequest) {
		return employeeResponse(body, servletRequest,
				organizationQueryService::employeeApprovalLevel);
	}

	@PostMapping("/employees/direct-manager")
	public ResponseEntity<FmExternalResponse<FmExternalEmployeeManagerView>> employeeDirectManager(
			@RequestBody FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest servletRequest) {
		return employeeResponse(body, servletRequest,
				organizationQueryService::employeeDirectManager);
	}

	private <T> ResponseEntity<FmExternalResponse<T>> employeeResponse(
			FmExternalRequest<FmExternalEmployeeOrganizationRequest> body,
			HttpServletRequest request,
			ExternalEmployeeQuery<T> query) {
		String requestId = requestId(request);
		try {
			T value = query.execute(body == null ? null : body.data());
			if (value == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						FmExternalResponse.error(requestId, "EMPLOYEE_NOT_FOUND",
								"The requested resource was not found."));
			}
			return ResponseEntity.ok(FmExternalResponse.success(requestId, value));
		} catch (FmExternalApiConflictException exception) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(FmExternalResponse.error(
					requestId, exception.getCode(), exception.getMessage()));
		} catch (ServiceException exception) {
			return ResponseEntity.badRequest().body(FmExternalResponse.error(requestId,
					"REQUEST_INVALID", exception.getMessage()));
		} catch (Exception exception) {
			return ResponseEntity.internalServerError().body(FmExternalResponse.error(
					requestId, "INTERNAL_ERROR", "The request could not be completed."));
		}
	}

	@FunctionalInterface
	private interface ExternalEmployeeQuery<T> {
		T execute(FmExternalEmployeeOrganizationRequest request) throws ServiceException;
	}

	private String requestId(HttpServletRequest request) {
		return String.valueOf(request.getAttribute(
				FmExternalApiAuthenticationFilter.REQUEST_ID_ATTRIBUTE));
	}
}
