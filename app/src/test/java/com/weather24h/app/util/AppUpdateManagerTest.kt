package com.weather24h.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun testIsNewerVersion_patchUpgrade() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.41", "1.0.40"))
        assertTrue(AppUpdateManager.isNewerVersion("v1.0.41", "1.0.40"))
        assertTrue(AppUpdateManager.isNewerVersion("V1.0.41", "v1.0.40"))
    }

    @Test
    fun testIsNewerVersion_minorAndMajorUpgrade() {
        assertTrue(AppUpdateManager.isNewerVersion("1.1.0", "1.0.40"))
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0", "1.0.40"))
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0", "1.99.99"))
    }

    @Test
    fun testIsNewerVersion_sameVersion() {
        assertFalse(AppUpdateManager.isNewerVersion("1.0.40", "1.0.40"))
        assertFalse(AppUpdateManager.isNewerVersion("v1.0.40", "1.0.40"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.40", "v1.0.40"))
    }

    @Test
    fun testIsNewerVersion_olderVersion() {
        assertFalse(AppUpdateManager.isNewerVersion("1.0.39", "1.0.40"))
        assertFalse(AppUpdateManager.isNewerVersion("v1.0.28", "1.0.40"))
        assertFalse(AppUpdateManager.isNewerVersion("0.9.9", "1.0.0"))
    }

    @Test
    fun testIsNewerVersion_extraParts() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.40.1", "1.0.40"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.40", "1.0.40.1"))
    }

    @Test
    fun testSanitizeReleaseNotes_removesGithubLinksAndRepoArtifacts() {
        val rawGithubNotes = """
            ## What's Changed
            * feat: Redesign hero card by @congdu28 in https://github.com/congdu28/Weather-24h/pull/12
            * fix(widget): Keep favorite cities across updates in https://github.com/congdu28/Weather-24h/pull/13
            * [perf]: Improve particle frame rate

            **Full Changelog**: https://github.com/congdu28/Weather-24h/compare/v1.0.41...v1.0.42
        """.trimIndent()

        val sanitized = AppUpdateManager.sanitizeReleaseNotes(rawGithubNotes)
        assertFalse(sanitized.contains("github.com"))
        assertFalse(sanitized.contains("congdu28"))
        assertFalse(sanitized.contains("Full Changelog"))
        assertFalse(sanitized.contains("What's Changed"))
        assertFalse(sanitized.contains("feat:"))
        assertFalse(sanitized.contains("fix(widget):"))
        assertFalse(sanitized.contains("[perf]"))
        assertTrue(sanitized.contains("Redesign hero card"))
        assertTrue(sanitized.contains("Keep favorite cities across updates"))
        assertTrue(sanitized.contains("Improve particle frame rate"))
    }

    @Test
    fun testSanitizeReleaseNotes_emptyFallback() {
        val sanitized = AppUpdateManager.sanitizeReleaseNotes("")
        assertTrue(sanitized.contains("Tối ưu hóa hiệu năng"))
    }
}
