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
			int separator = rule.indexOf('/');
			if (separator < 0) {
				return InetAddress.getByName(rule).equals(InetAddress.getByName(sourceIp));
			}
			InetAddress network = InetAddress.getByName(rule.substring(0, separator));
			InetAddress address = InetAddress.getByName(sourceIp);
			byte[] networkBytes = network.getAddress();
			byte[] addressBytes = address.getAddress();
			if (networkBytes.length != addressBytes.length) {
				return false;
			}
			int prefix = Integer.parseInt(rule.substring(separator + 1));
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
		} catch (UnknownHostException | NumberFormatException exception) {
			return false;
		}
	}
}
