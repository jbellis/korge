package com.soywiz.korim.text

import com.soywiz.korim.font.FontMetrics
import com.soywiz.korim.font.GlyphMetrics
import com.soywiz.korma.geom.Anchor

data class TextAlignment(
    val horizontal: HorizontalAlign,
    val vertical: VerticalAlign,
) {
    val justified get() = horizontal == HorizontalAlign.JUSTIFY
    val anchor: Anchor = Anchor(horizontal.ratioFake, vertical.ratioFake)

    fun withHorizontal(horizontal: HorizontalAlign) = fromAlign(horizontal, vertical)
    fun withVertical(vertical: VerticalAlign) = fromAlign(horizontal, vertical)

    companion object {
        private val horizontals = listOf(HorizontalAlign.LEFT, HorizontalAlign.CENTER, HorizontalAlign.RIGHT, HorizontalAlign.JUSTIFY)

        private val TOP = horizontals.map { TextAlignment(it, VerticalAlign.TOP) }
        private val BASELINE = horizontals.map { TextAlignment(it, VerticalAlign.BASELINE) }
        private val MIDDLE = horizontals.map { TextAlignment(it, VerticalAlign.MIDDLE) }
        private val BOTTOM = horizontals.map { TextAlignment(it, VerticalAlign.BOTTOM) }

        val TOP_LEFT = TOP[0]
        val TOP_CENTER = TOP[1]
        val TOP_RIGHT = TOP[2]
        val TOP_JUSTIFIED = TOP[3]

        val BASELINE_LEFT = BASELINE[0]
        val BASELINE_CENTER = BASELINE[1]
        val BASELINE_RIGHT = BASELINE[2]
        val BASELINE_JUSTIFIED = BASELINE[3]

        val LEFT = TOP[0]
        val CENTER = TOP[1]
        val RIGHT = TOP[2]
        val JUSTIFIED = TOP[3]

        val MIDDLE_LEFT = MIDDLE[0]
        val MIDDLE_CENTER = MIDDLE[1]
        val MIDDLE_RIGHT = MIDDLE[2]
        val MIDDLE_JUSTIFIED = MIDDLE[3]

        val BOTTOM_LEFT = BOTTOM[0]
        val BOTTOM_CENTER = BOTTOM[1]
        val BOTTOM_RIGHT = BOTTOM[2]
        val BOTTOM_JUSTIFIED = BOTTOM[3]

        fun fromAlign(horizontal: HorizontalAlign, vertical: VerticalAlign): TextAlignment {
            val horizontalIndex = when (horizontal) {
                HorizontalAlign.LEFT -> 0
                HorizontalAlign.CENTER -> 1
                HorizontalAlign.RIGHT -> 2
                HorizontalAlign.JUSTIFY -> 3
                else -> return TextAlignment(horizontal, vertical)
            }
            return when (vertical) {
                VerticalAlign.TOP -> TOP[horizontalIndex]
                VerticalAlign.BASELINE -> BASELINE[horizontalIndex]
                VerticalAlign.MIDDLE -> MIDDLE[horizontalIndex]
                VerticalAlign.BOTTOM -> BOTTOM[horizontalIndex]
                else -> TextAlignment(horizontal, vertical)
            }
        }
    }
}

inline class VerticalAlign(val ratio: Double) {
    val ratioFake get() = if (this == BASELINE) 1.0 else ratio

    companion object {
        val TOP = VerticalAlign(0.0)
        val MIDDLE = VerticalAlign(0.5)
        val BOTTOM = VerticalAlign(1.0)
        val BASELINE = VerticalAlign(Double.POSITIVE_INFINITY) // Special
        private val values = arrayOf(TOP, MIDDLE, BASELINE, BOTTOM)

        fun values() = values

        operator fun invoke(str: String): VerticalAlign = when (str) {
            "TOP" -> TOP
            "MIDDLE" -> MIDDLE
            "BOTTOM" -> BOTTOM
            "BASELINE" -> BASELINE
            else -> VerticalAlign(str.substringAfter('(').substringBefore(')').toDoubleOrNull() ?: 0.0)
        }
    }

    fun getOffsetY(height: Double, baseline: Double): Double = when (this) {
        BASELINE -> baseline
        else -> -height * ratio
    }

    fun getOffsetYRespectBaseline(glyph: GlyphMetrics, font: FontMetrics): Double = when (this) {
        TOP -> font.top
        BOTTOM -> font.bottom
        BASELINE -> 0.0
        else -> (font.top * ratio + font.bottom * (1.0 - ratio))
    }

    override fun toString(): String = when (this) {
        TOP -> "TOP"
        MIDDLE -> "MIDDLE"
        BOTTOM -> "BOTTOM"
        BASELINE -> "BASELINE"
        else -> "VerticalAlign($ratio)"
    }
}

inline class HorizontalAlign(val ratio: Double) {
    val ratioFake get() = if (this == JUSTIFY) 0.0 else ratio

    companion object {
        val JUSTIFY = HorizontalAlign(-0.00001)
        val LEFT = HorizontalAlign(0.0)
        val CENTER = HorizontalAlign(0.5)
        val RIGHT = HorizontalAlign(1.0)

        private val values = arrayOf(LEFT, CENTER, RIGHT, JUSTIFY)

        fun values() = values

        operator fun invoke(str: String): HorizontalAlign = when (str) {
            "LEFT" -> LEFT
            "CENTER" -> CENTER
            "RIGHT" -> RIGHT
            "JUSTIFY" -> JUSTIFY
            else -> HorizontalAlign(str.substringAfter('(').substringBefore(')').toDoubleOrNull() ?: 0.0)
        }
    }

    fun getOffsetX(width: Double): Double = when (this) {
        JUSTIFY -> 0.0
        else -> width * ratio
    }

    override fun toString(): String = when (this) {
        LEFT -> "LEFT"
        CENTER -> "CENTER"
        RIGHT -> "RIGHT"
        JUSTIFY -> "JUSTIFY"
        else -> "HorizontalAlign($ratio)"
    }
}
