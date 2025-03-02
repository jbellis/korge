package com.soywiz.korma.geom

import kotlin.test.*

class LineTest {
    val tolerance = 0.0001

    @Test
    fun testIntersecting() {
        val line1 = Line(Point(284, 158), Point(246, 158))
        val line2 = Line(Point(303.89273932825165, 198.88732201874024), Point(257.05152496464524, 155.2765362319343))
        assertEquals(true, line1.intersectsSegment(line2))
        assertEquals(38.0, line1.length)
        assertEquals(64.0, line2.length, absoluteTolerance = tolerance)
        assertEquals(180.0, line1.angle.degrees, absoluteTolerance = tolerance)
        assertEquals(222.95459151111274, line2.angle.degrees, absoluteTolerance = tolerance)
        assertEquals(Point(260.0, 158.0), line1.getSegmentIntersectionPoint(line2)?.round())
    }

    @Test
    fun testProjectedPoint() {
        assertEquals(Point(0, 50), Line(Point(0, 0), Point(0, 100)).projectedPoint(Point(50, 50)))
        assertEquals(Point(50, 50), Line(Point(0, 0), Point(100, 100)).projectedPoint(Point(100, 0)))

        // On line
        assertEquals(Point(0, 0), Line(Point(0, 0), Point(0, 100)).projectedPoint(Point(0, 0)))
        assertEquals(Point(0, 50), Line(Point(0, 0), Point(0, 100)).projectedPoint(Point(0, 50)))
        assertEquals(Point(0, 100), Line(Point(0, 0), Point(0, 100)).projectedPoint(Point(0, 100)))
        assertEquals(Point(0, 150), Line(Point(0, 0), Point(0, 100)).projectedPoint(Point(0, 150)))
    }

    @Test
    fun testLineData() {
        val gen = { Line(Point(0, 0), Point(100, 100)) }
        assertEquals(gen(), gen())
        assertEquals(gen().hashCode(), gen().hashCode())
    }
}
