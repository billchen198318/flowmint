package org.qifu.fm.domain.externalapi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public final class FmIpAllowlistMatcher {

	private FmIpAllowlistMatcher() {
	}

	public static boolean allows(Collection<String> allowlist, String sourceIp) {
		if (allowlist == null || allowlist.isEmpty()) {
			return true;
		}
		if (StringUtils.isBlank(sourceIp)) {
			return false;
		}
		for (String rule : allowlist) {
			if (matches(StringUtils.trimToEmpty(rule), sourceIp.trim())) {
				return true;
			}
		}
		return false;
	}

	static boolean matches(String rule, String sourceIp) {
		try {
			byte[] addressBytes = literalAddress(sourceIp);
			int separator = rule.indexOf('/');
			byte[] networkBytes = literalAddress(separator < 0 ? rule : rule.substring(0, separator));
			if (networkBytes.length != addressBytes.length) {
				return false;
			}
			int prefix = separator < 0 ? networkBytes.length * Byte.SIZE
					: Integer.parseInt(rule.substring(separator + 1));
			if (prefix < 0 || prefix > networkBytes.length * Byte.SIZE) {
				return false;
			}
			for (int index = 0; index < networkBytes.length; index++) {
				int remaining = prefix - index * Byte.SIZE;
				int mask = remaining >= Byte.SIZE ? 0xff
						: remaining <= 0 ? 0 : 0xff << (Byte.SIZE - remaining) & 0xff;
				if ((networkBytes[index] & mask) != (addressBytes[index] & mask)) {
					return false;
				}
			}
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	public static String normalizeRule(String rule) {
		if (StringUtils.isBlank(rule)) {
			throw new IllegalArgumentException("IP rule is blank");
		}
		String value = rule.trim();
		int slash = value.indexOf('/');
		if (slash >= 0 && value.indexOf('/', slash + 1) >= 0) {
			throw new IllegalArgumentException("IP rule has multiple prefixes");
		}
		byte[] bytes = literalAddress(slash < 0 ? value : value.substring(0, slash));
		int prefix = bytes.length * Byte.SIZE;
		if (slash >= 0) {
			try {
				prefix = Integer.parseInt(value.substring(slash + 1));
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException("Invalid IP prefix", exception);
			}
			if (prefix < 0 || prefix > bytes.length * Byte.SIZE) {
				throw new IllegalArgumentException("IP prefix out of range");
			}
		}
		for (int index = 0; index < bytes.length; index++) {
			int remaining = prefix - index * Byte.SIZE;
			int mask = remaining >= Byte.SIZE ? 0xff
					: remaining <= 0 ? 0 : 0xff << (Byte.SIZE - remaining) & 0xff;
			bytes[index] = (byte) (bytes[index] & mask);
		}
		try {
			String address = InetAddress.getByAddress(bytes).getHostAddress();
			return slash < 0 ? address : address + "/" + prefix;
		} catch (UnknownHostException exception) {
			throw new IllegalArgumentException("Invalid IP address", exception);
		}
	}

	private static byte[] literalAddress(String value) {
		if (value.indexOf(':') >= 0) {
			if (!value.matches("[0-9a-fA-F:]+")) {
				throw new IllegalArgumentException("Invalid IPv6 literal");
			}
			try {
				byte[] bytes = InetAddress.getByName(value).getAddress();
				if (bytes.length == 16) return bytes;
			} catch (UnknownHostException exception) {
				throw new IllegalArgumentException("Invalid IPv6 literal", exception);
			}
			throw new IllegalArgumentException("Invalid IPv6 literal");
		}
		String[] octets = value.split("\\.", -1);
		if (octets.length != 4) throw new IllegalArgumentException("Invalid IPv4 literal");
		byte[] bytes = new byte[4];
		for (int index = 0; index < octets.length; index++) {
			if (!octets[index].matches("[0-9]{1,3}")) {
				throw new IllegalArgumentException("Invalid IPv4 literal");
			}
			int number = Integer.parseInt(octets[index]);
			if (number > 255) throw new IllegalArgumentException("Invalid IPv4 literal");
			bytes[index] = (byte) number;
		}
		return bytes;
	}
}
