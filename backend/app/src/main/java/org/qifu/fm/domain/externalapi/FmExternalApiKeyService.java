package org.qifu.fm.domain.externalapi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FmExternalApiKeyService {

	private static final String PEPPER_PROPERTY = "fm.external-api.key-pepper";
	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String KEY_PREFIX = "fmk_live_";
	private final SecureRandom secureRandom = new SecureRandom();
	private final byte[] pepper;

	public FmExternalApiKeyService(Environment environment) {
		String configured = environment.getRequiredProperty(PEPPER_PROPERTY);
		try {
			this.pepper = Base64.getDecoder().decode(configured);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("外部 API Key Pepper 必須是 Base64", exception);
		}
		if (pepper.length < 32) {
			throw new IllegalStateException("外部 API Key Pepper 至少需要 256 bit");
		}
	}

	public GeneratedKey generate() throws ServiceException {
		byte[] secretBytes = new byte[32];
		secureRandom.nextBytes(secretBytes);
		String keyId = UUID.randomUUID().toString();
		String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
		String plainText = KEY_PREFIX + keyId + "." + secret;
		return new GeneratedKey(keyId, plainText, KEY_PREFIX + keyId.substring(0, 8),
				plainText.substring(plainText.length() - 4), hash(plainText));
	}

	public boolean matches(String plainText, String expectedHash) throws ServiceException {
		if (StringUtils.isAnyBlank(plainText, expectedHash)) {
			return false;
		}
		byte[] actual = HexFormat.of().parseHex(hash(plainText));
		try {
			byte[] expected = HexFormat.of().parseHex(expectedHash);
			return java.security.MessageDigest.isEqual(actual, expected);
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	public String extractKeyId(String plainText) {
		if (StringUtils.isBlank(plainText) || !plainText.startsWith(KEY_PREFIX)) {
			return null;
		}
		int separator = plainText.indexOf('.', KEY_PREFIX.length());
		if (separator < 0) {
			return null;
		}
		String keyId = plainText.substring(KEY_PREFIX.length(), separator);
		try {
			return UUID.fromString(keyId).toString();
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private String hash(String plainText) throws ServiceException {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(pepper, HMAC_ALGORITHM));
			return HexFormat.of().formatHex(
					mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new ServiceException("無法產生外部 API Key");
		}
	}

	public record GeneratedKey(String keyId, String plainText, String prefix,
			String lastFour, String secretHash) {
	}
}
