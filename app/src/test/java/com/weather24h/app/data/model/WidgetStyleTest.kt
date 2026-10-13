package com.weather24h.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WidgetStyleTest {

    @Test
    fun widgetStyleEnum_containsAllExpectedStyles() {
        val styles = WidgetStyle.values()
        assertEquals(4, styles.size)
        assertNotNull(WidgetStyle.valueOf("FROSTED_GLASS"))
        assertNotNull(WidgetStyle.valueOf("MINIMAL_CLEAN"))
        assertNotNull(WidgetStyle.valueOf("ADAPTIVE_COLOR"))
        assertNotNull(WidgetStyle.valueOf("SOLID_DARK"))
    }

    @Test
    fun widgetStyle_hasValidDisplayAttributes() {
        WidgetStyle.values().forEach { style ->
            assert(style.id.isNotBlank())
            assert(style.title.isNotBlank())
            assert(style.description.isNotBlank())
            assert(style.iconEmoji.isNotBlank())
        }
    }
}
