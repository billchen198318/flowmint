package org.qifu.core.scheduled;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SendMailHelperJobReplacementTest {

	@Test
	void runtimeLoadsFlowMintAppReplacementInsteadOfCoreJarClass() throws Exception {
		Path location = Path.of(SendMailHelperJob.class.getProtectionDomain()
				.getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
		String normalized = location.toString().replace('\\', '/');

		assertTrue(normalized.endsWith("/app/target/classes"),
				() -> "Expected app replacement but loaded SendMailHelperJob from "
						+ normalized);
	}
}
