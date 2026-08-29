package org.qifu.fm.domain.ai;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.util.EncryptorUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FmAiApiKeyCipher {

	private static final String KEY_PROPERTY = "fm.ai.encryption-key";
	private final String encryptionKey;

	public FmAiApiKeyCipher(Environment environment) {
		this.encryptionKey = environment.getRequiredProperty(KEY_PROPERTY);
		EncryptorUtils.validateGcmKey(encryptionKey);
	}

	public String encrypt(String apiKey) throws ServiceException {
		if (StringUtils.isBlank(apiKey)) {
			throw new ServiceException("AI Provider API Key 必填");
		}
		return EncryptorUtils.encryptGcm(encryptionKey, apiKey.trim());
	}

	public String decrypt(String encryptedApiKey) throws ServiceException {
		if (StringUtils.isBlank(encryptedApiKey)) {
			throw new ServiceException("AI Provider API Key 尚未設定");
		}
		return EncryptorUtils.decryptGcm(encryptionKey, encryptedApiKey);
	}

	public String mask(String encryptedApiKey) throws ServiceException {
		String apiKey = decrypt(encryptedApiKey);
		int visibleLength = Math.min(4, apiKey.length());
		return "****" + apiKey.substring(apiKey.length() - visibleLength);
	}
}
