package org.qifu.fm.domain.datasource;

import org.qifu.util.EncryptorUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FmDataSourcePasswordCipher {

	private static final String KEY_PROPERTY = "fm.datasource.encryption-key";
	private final String encryptionKey;

	public FmDataSourcePasswordCipher(Environment environment) {
		this.encryptionKey = environment.getRequiredProperty(KEY_PROPERTY);
		EncryptorUtils.validateGcmKey(encryptionKey);
	}

	public String encrypt(String password) {
		return EncryptorUtils.encryptGcm(encryptionKey, password);
	}

	public String decrypt(String encryptedPassword) {
		return EncryptorUtils.decryptGcm(encryptionKey, encryptedPassword);
	}
}
