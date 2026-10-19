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
}
