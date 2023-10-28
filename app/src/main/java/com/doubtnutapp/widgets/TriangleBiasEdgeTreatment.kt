package com.doubtnutapp.widgets

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

/**
 * Created by devansh on 7/1/21.
 */

/**
 * Instantiates a triangle treatment of the given size, which faces inward or outward relative to
 * the shape.
 *
 * @param size the length in pixels that the triangle extends into or out of the shape. The length
 * of the side of the triangle coincident with the rest of the edge is 2 * size.
 * @param inside true if the triangle should be "cut out" of the shape (i.e. inward-facing); false
 * if the triangle should extend out of the shape.
 * @param bias ranges between [-1, 1] with -1 indicating the extreme left of the shape,
 * 1 indicating the extreme right and 0 indicating the center, i.e., no bias.
 */
class TriangleBiasEdgeTreatment(
        private val size: Float,
        private val inside: Boolean,
        private val bias: Float
) : EdgeTreatment() {

    override fun getEdgePath(
            length: Float, center: Float, interpolation: Float, shapePath: ShapePath) {
        val biasedCenter = (1 - bias) * center
        shapePath.lineTo(biasedCenter - (size * interpolation), 0f)
        shapePath.lineTo(biasedCenter, if (inside) size * interpolation else -size * interpolation)
        shapePath.lineTo(biasedCenter + (size * interpolation), 0f)
        shapePath.lineTo(length, 0f)
    }
}