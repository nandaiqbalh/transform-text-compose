package com.nandaiqbalh.transfromtext.presentation

import androidx.lifecycle.ViewModel
import com.nandaiqbalh.transfromtext.data.model.Transformation
import com.nandaiqbalh.transfromtext.data.util.KeyboardTransformer

class TransformationViewModel : ViewModel() {
	private val transformer = KeyboardTransformer()

	fun transformText(transformationContent: String, textContent: String): String {
		val transformations = parseTransformations(transformationContent)
		return transformer.applyTransformations(transformations, textContent)
	}

	private fun parseTransformations(content: String): List<Transformation> {
		val transforms = mutableListOf<Transformation>()
		content.split(",").forEach { transform ->
			when {
				transform.startsWith("H") -> transforms.add(Transformation("H"))
				transform.startsWith("V") -> transforms.add(Transformation("V"))
				transform.matches(Regex("^-?\\d+$")) -> transforms.add(Transformation("S", transform.toInt()))
			}
		}
		return transforms
	}
}
