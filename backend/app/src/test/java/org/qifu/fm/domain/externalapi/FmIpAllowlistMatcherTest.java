package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class FmIpAllowlistMatcherTest {

	@Test
	void emptyAllowlistAllowsAnyAddress() {
		assertTrue(FmIpAllowlistMatcher.allows(List.of(), "203.0.113.8"));
	}

	@Test
	void supportsExactIpv4AndCidr() {
		assertTrue(FmIpAllowlistMatcher.allows(
				List.of("192.0.2.8", "10.20.0.0/16"), "192.0.2.8"));
		assertTrue(FmIpAllowlistMatcher.allows(
				List.of("10.20.0.0/16"), "10.20.30.40"));
		assertFalse(FmIpAllowlistMatcher.allows(
				List.of("10.20.0.0/16"), "10.21.30.40"));
	}

	@Test
	void supportsIpv6AndRejectsInvalidRules() {
		assertTrue(FmIpAllowlistMatcher.allows(List.of("2001:db8::/32"),
				"2001:db8:1::20"));
		assertFalse(FmIpAllowlistMatcher.allows(List.of("10.0.0.0/99"),
				"10.0.0.1"));
	}

	@Test
	void rejectsHostnamesAndNormalizesOnlyLiteralNetworks() {
		assertFalse(FmIpAllowlistMatcher.allows(List.of("localhost"), "127.0.0.1"));
		assertFalse(FmIpAllowlistMatcher.allows(List.of("127.0.0.1"), "localhost"));
		assertThrows(IllegalArgumentException.class,
				() -> FmIpAllowlistMatcher.normalizeRule("localhost"));
		assertThrows(IllegalArgumentException.class,
				() -> FmIpAllowlistMatcher.normalizeRule("10.0.0.0/33"));
		assertEquals("10.20.0.0/16",
				FmIpAllowlistMatcher.normalizeRule("10.20.30.40/16"));
		assertEquals("2001:db8:0:0:0:0:0:0/32",
				FmIpAllowlistMatcher.normalizeRule("2001:db8:1::20/32"));
	}
}
