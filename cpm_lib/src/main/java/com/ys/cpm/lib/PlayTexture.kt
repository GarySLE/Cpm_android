package com.ys.cpm.lib

import android.content.Context
import android.util.Log
import android.view.TextureView
import android.view.View

/**
 * Created by Ys on 2017/8/16.
 */
class PlayTexture(context: Context) : TextureView(context) {

    companion object {
        private val TAG = PlayTexture::class.java.simpleName
        // 智能全屏(相对父布局)
        const val DISPLAY_MODE_SMART_PARENT = 1
        // 原始尺寸
        const val DISPLAY_MODE_ORIGIN_SIZE = 2
        // 16:9
        const val DISPLAY_MODE_16_9 = 3
        // 4:3
        const val DISPLAY_MODE_4_3 = 4
    }

    var displayMode = DISPLAY_MODE_SMART_PARENT
    set(value) {
        if (value == field) return
        field = value
        requestLayout()
    }
    private var contentWidth: Int = 0
    private var contentHeight: Int = 0

    fun fitSize(contentWidth: Int, contentHeight: Int) {
        if (this.contentWidth != contentWidth && this.contentHeight != contentHeight) {
            this.contentWidth = contentWidth
            this.contentHeight = contentHeight
            requestLayout()
        }
    }

    override fun setRotation(newRotation: Float) {
        if (newRotation != rotation) {
            super.setRotation(newRotation)
            requestLayout()
        }
    }

    override fun onMeasure(wms: Int, hms: Int) {
        Log.i(TAG, "onMeasure " + " [" + this.hashCode() + "] ")

        var widthMeasureSpec = wms
        var heightMeasureSpec = hms
        val viewRotation = rotation

        Log.i(TAG, "videoWidth = ${this.contentWidth}, videoHeight = ${this.contentHeight}")
        Log.i(TAG, "viewRotation = " + rotation)

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90f || viewRotation == 270f) {
            val tempMeasureSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempMeasureSpec
        }

        Log.i(TAG, "widthMeasureSpec  [" + View.MeasureSpec.toString(widthMeasureSpec) + "]")
        Log.i(TAG, "heightMeasureSpec [" + View.MeasureSpec.toString(heightMeasureSpec) + "]")

        var width = getDefaultSize(this.contentWidth, widthMeasureSpec)
        var height = getDefaultSize(this.contentHeight, heightMeasureSpec)
        if (this.contentWidth > 0 && this.contentHeight > 0) {
            when (this.displayMode) {
                DISPLAY_MODE_ORIGIN_SIZE -> {
                    // 原始尺寸
                    width = this.contentWidth
                    height = this.contentHeight
                }
                DISPLAY_MODE_16_9 -> {
                    // 16:9
                    if (width * 9 < height * 16) {
                        height = width * 9 / 16
                    } else if (width * 9 > height * 16) {
                        width = height * 16 / 9
                    }
                }
                DISPLAY_MODE_4_3 -> {
                    // 4:3
                    if (width * 3 < height * 4) {
                        height = width * 3 / 4
                    } else if (width * 3 > height * 4) {
                        width = height * 4 / 3
                    }
                }
                else -> {
                    val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
                    val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
                    val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
                    val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

                    if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                        // the size is fixed
                        width = widthSpecSize
                        height = heightSpecSize
                        // for compatibility, we adjust size based on aspect ratio
                        if (this.contentWidth * height < width * this.contentHeight) {
                            width = height * this.contentWidth / this.contentHeight
                        } else if (this.contentWidth * height > width * this.contentHeight) {
                            height = width * this.contentHeight / this.contentWidth
                        }
                    } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                        // only the width is fixed, adjust the height to match aspect ratio if possible
                        width = widthSpecSize
                        height = width * this.contentHeight / this.contentWidth
                        if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                            // couldn't match aspect ratio within the constraints
                            height = heightSpecSize
                            width = height * this.contentWidth / this.contentHeight
                        }
                    } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                        // only the height is fixed, adjust the width to match aspect ratio if possible
                        height = heightSpecSize
                        width = height * this.contentWidth / this.contentHeight
                        if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                            // couldn't match aspect ratio within the constraints
                            width = widthSpecSize
                            height = width * this.contentHeight / this.contentWidth
                        }
                    } else {
                        // neither the width nor the height are fixed, try to use actual video size
                        width = this.contentWidth
                        height = this.contentHeight
                        if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                            // too tall, decrease both width and height
                            height = heightSpecSize
                            width = height * this.contentWidth / this.contentHeight
                        }
                        if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                            // too wide, decrease both width and height
                            width = widthSpecSize
                            height = width * this.contentHeight / this.contentWidth
                        }
                    }
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        Log.d(TAG, "onMeasure: measure size(${this.contentWidth}x${this.contentHeight})")
        setMeasuredDimension(width, height)
    }

}