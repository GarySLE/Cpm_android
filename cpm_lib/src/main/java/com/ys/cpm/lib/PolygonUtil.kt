package com.ys.cpm.lib

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.support.annotation.FloatRange
import android.support.annotation.IntRange

import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.cos
import java.lang.Math.sin

/**
 * Created by Ys on 2017/8/6.
 * An efficient utility class for drawing regular polygons on a {@link Canvas}.
 */
class PolygonUtil {

    companion object {
        private val backingPath = Path()
        private val tempCornerArcBounds = RectF()

        /**
         * Draws a regular polygon.
         *
         * Note that this method is not thread safe. This is not an issue if (as is typical) all invocations are made on the
         * UI thread.
         *
         * @param canvas       the [Canvas] to draw on
         * @param sideCount    the number of sides of the polygon
         * @param centerX      the x-coordinate of the polygon center in pixels
         * @param centerY      the y-coordinate of the polygon center in pixels
         * @param outerRadius  the distance from the polygon center to any vertex (ignoring corner rounding) in pixels
         * @param cornerRadius the radius of the rounding applied to each corner of the polygon in pixels
         * @param rotation     the rotation of the polygon in degrees
         * @param paint        the [Paint] to draw with
         */
        @JvmStatic
        fun drawPolygon(
                canvas: Canvas,
                @IntRange(from = 3) sideCount: Int,
                centerX: Float,
                centerY: Float,
                @FloatRange(from = 0.0, fromInclusive = false) outerRadius: Float,
                @FloatRange(from = 0.0) cornerRadius: Float,
                rotation: Float,
                paint: Paint
        ) {
            val inRadius =
                    (outerRadius * Math.cos(toRadians(180.0 / sideCount))).toFloat()

            if (inRadius < cornerRadius) {
                /*
                 * If the supplied corner radius is too small, we default to drawing the "incircle".
                 *   - https://web.archive.org/web/20170415150442/https://en.wikipedia.org/wiki/Regular_polygon
                 *   - https://web.archive.org/web/20170415150415/http://www.mathopenref.com/polygonincircle.html
                 */
                canvas.drawCircle(centerX, centerY, inRadius, paint)
            } else {
                canvas.save()
                canvas.rotate(-rotation, centerX, centerY)
                backingPath.rewind()

                if (abs(cornerRadius) < 0.01) {
                    constructNonRoundedPolygonPath(
                            sideCount,
                            centerX,
                            centerY,
                            outerRadius
                    )
                } else {
                    constructRoundedPolygonPath(
                            sideCount,
                            centerX,
                            centerY,
                            outerRadius,
                            cornerRadius
                    )
                }

                canvas.drawPath(backingPath, paint)
                canvas.restore()
            }
        }

        private fun constructNonRoundedPolygonPath(
                @IntRange(from = 3) sideCount: Int,
                centerX: Float,
                centerY: Float,
                @FloatRange(from = 0.0, fromInclusive = false) outerRadius: Float
        ) {
            for (cornerNumber in 0 until sideCount) {
                val angleToCorner = cornerNumber * (360.0 / sideCount)
                val cornerX =
                        (centerX + outerRadius * cos(toRadians(angleToCorner))).toFloat()
                val cornerY =
                        (centerY + outerRadius * sin(toRadians(angleToCorner))).toFloat()

                if (cornerNumber == 0) {
                    backingPath.moveTo(cornerX, cornerY)
                } else {
                    backingPath.lineTo(cornerX, cornerY)
                }
            }

            // Draw the final straight edge.
            backingPath.close()
        }

        private fun constructRoundedPolygonPath(
                @IntRange(from = 3) sideCount: Int,
                centerX: Float,
                centerY: Float,
                @FloatRange(from = 0.0, fromInclusive = false) outerRadius: Float,
                @FloatRange(from = 0.0) cornerRadius: Float
        ) {
            val halfInteriorCornerAngle = 90 - 180.0 / sideCount
            val halfCornerArcSweepAngle = (90 - halfInteriorCornerAngle).toFloat()
            val distanceToCornerArcCenter =
                    outerRadius - cornerRadius / sin(toRadians(halfInteriorCornerAngle))

            for (cornerNumber in 0 until sideCount) {
                val angleToCorner = cornerNumber * (360.0 / sideCount)
                val cornerCenterX =
                        (centerX + distanceToCornerArcCenter *
                                cos(toRadians(angleToCorner))).toFloat()
                val cornerCenterY =
                        (centerY + distanceToCornerArcCenter *
                                sin(toRadians(angleToCorner))).toFloat()

                tempCornerArcBounds.set(
                        cornerCenterX - cornerRadius,
                        cornerCenterY - cornerRadius,
                        cornerCenterX + cornerRadius,
                        cornerCenterY + cornerRadius
                )

                /*
                 * Quoted from the arcTo documentation:
                 *
                 *   "Append the specified arc to the path as a new contour. If the start of the path is different from the
                 *    path's current last point, then an automatic lineTo() is added to connect the current contour to the
                 *    start of the arc. However, if the path is empty, then we call moveTo() with the first point of the
                 *    arc."
                 *
                 * We construct our polygon by sequentially drawing rounded corners using arcTo, and leverage the
                 * automatically-added moveTo/lineTo instructions to connect these corners with straight edges.
                 */
                backingPath.arcTo(
                        tempCornerArcBounds,
                        (angleToCorner - halfCornerArcSweepAngle).toFloat(),
                        2 * halfCornerArcSweepAngle
                )
            }

            // Draw the final straight edge.
            backingPath.close()
        }

        private fun toRadians(degrees: Double): Double = 2.0 * PI * degrees / 360
    }

}