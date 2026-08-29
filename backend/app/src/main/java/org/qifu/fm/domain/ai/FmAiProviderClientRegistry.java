package org.qifu.fm.domain.ai;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class FmAiProviderClientRegistry {

	private final List<FmAiProviderClient> clients;

	public FmAiProviderClientRegistry(List<FmAiProviderClient> clients) {
		this.clients = List.copyOf(clients);
	}

	public FmAiProviderClient required(String providerType) throws ServiceException {
		return clients.stream()
				.filter(client -> client.providerType().equals(providerType))
				.findFirst()
				.orElseThrow(() -> new ServiceException(
						"此 AI Provider Adapter 尚未啟用"));
	}
}
