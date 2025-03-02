package com.soywiz.korio.async

import com.soywiz.klock.DateTime
import com.soywiz.klock.milliseconds
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertTrue

class SleepTest {
	// @TODO: Change once we don't wait all the delay time
	val time get() = DateTime.now()

	@Test
	fun name() = suspendTest {
		val start = time
		delay(10)
		delay(20)
		val end = time
		assertTrue((end - start) > 25.milliseconds)
	}
}
