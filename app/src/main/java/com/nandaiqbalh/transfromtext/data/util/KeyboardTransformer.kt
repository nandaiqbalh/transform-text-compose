package com.nandaiqbalh.transfromtext.data.util

import android.util.Log
import com.nandaiqbalh.transfromtext.data.model.Transformation

class KeyboardTransformer {
	private val originalKeyboard = arrayOf(
		"1234567890".toCharArray(),
		"qwertyuiop".toCharArray(),
		"asdfghjkl;".toCharArray(),
		"zxcvbnm,./".toCharArray()
	)
	private var keyboard = originalKeyboard.map { it.clone() }.toTypedArray()

	fun applyTransformations(transformations: List<Transformation>, text: String): String {
		transformations.forEach { transformation ->
			when (transformation.type) {
				"H" -> {
					keyboard = horizontalFlip(keyboard)
					Log.d("Transformation", "Applied Horizontal Flip")
				}
				"V" -> {
					keyboard = verticalFlip(keyboard)
					Log.d("Transformation", "Applied Vertical Flip")
				}
				"S" -> {
					keyboard = shift(keyboard, transformation.value)
					Log.d("Transformation", "Applied Shift by ${transformation.value}")
				}
			}
			Log.d("Transformation", "Transformed Keyboard: ${keyboard.joinToString("\n") { it.joinToString("") }}")
		}
		return transformText(text)
	}

	private fun horizontalFlip(keyboard: Array<CharArray>): Array<CharArray> {
		return keyboard.map { it.reversedArray() }.toTypedArray()
	}

	private fun verticalFlip(keyboard: Array<CharArray>): Array<CharArray> {
		return keyboard.reversedArray()
	}

	private fun shift(keyboard: Array<CharArray>, n: Int): Array<CharArray> {
		val flatKeyboard = keyboard.flatMap { it.asIterable() }
		val size = flatKeyboard.size
		val shiftedKeyboard = CharArray(size)
		for (i in flatKeyboard.indices) {
			val newIndex = (i + n).mod(size)
			shiftedKeyboard[newIndex] = flatKeyboard[i]
		}
		return arrayOf(
			shiftedKeyboard.copyOfRange(0, 10),
			shiftedKeyboard.copyOfRange(10, 20),
			shiftedKeyboard.copyOfRange(20, 30),
			shiftedKeyboard.copyOfRange(30, 40)
		)
	}

	private fun transformText(text: String): String {
		val charMap = generateCharMap()
		return text.map { charMap[it] ?: it }.joinToString("")
	}

	private fun generateCharMap(): Map<Char, Char> {
		val originalFlat = originalKeyboard.flatMap { it.asIterable() }
		val transformedFlat = keyboard.flatMap { it.asIterable() }
		return originalFlat.zip(transformedFlat).toMap()
	}
}
