package org.qifu.fm.domain.externalapi;

import org.qifu.base.exception.ServiceException;

public final class FmExternalApiContext {

	private static final ThreadLocal<FmExternalApiPrincipal> CURRENT = new ThreadLocal<>();

	private FmExternalApiContext() {
	}

	public static void set(FmExternalApiPrincipal principal) {
		CURRENT.set(principal);
	}

	public static FmExternalApiPrincipal getRequired() throws ServiceException {
		FmExternalApiPrincipal principal = CURRENT.get();
		if (principal == null) {
			throw new ServiceException("External API authentication context is missing.");
		}
		return principal;
	}

	public static FmExternalApiPrincipal requireScope(String scope)
			throws ServiceException {
		FmExternalApiPrincipal principal = getRequired();
		if (!principal.hasScope(scope)) {
			throw new ServiceException("External API scope is not granted: " + scope);
		}
		return principal;
	}

	public static void clear() {
		CURRENT.remove();
	}
}
