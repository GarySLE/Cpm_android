package com.ys.cpm.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by Ys on 2017/8/5.
 */
class ChaosPolygonViewWrapper : FrameLayout {

    private val polygonFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val polygonStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokeWidth = resources.getDimension(R.dimen.stroke_width)

    var numberOfSides = 3
        set(value) {
            field = value
            invalidate()
        }
    var cornerRadius = 0f
        set(value) {
            field = value
            invalidate()
        }
    var polygonRotation = 0f
        set(value) {
            field = value
            invalidate()
        }
    var scale = 1f
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        val typedArray =
                context.obtainStyledAttributes(
                        attrs,
                        R.styleable.ChaosPolygonViewWrapper,
                        defStyleAttr,
                        0
                )
        this.numberOfSides =
                typedArray.getInt(
                        R.styleable.ChaosPolygonViewWrapper_polygonSides,
                        this.numberOfSides
                )
        this.cornerRadius =
                typedArray.getFloat(
                        R.styleable.ChaosPolygonViewWrapper_polygonCornerRadius,
                        this.cornerRadius
                )
        this.polygonRotation =
                typedArray.getFloat(
                        R.styleable.ChaosPolygonViewWrapper_polygonRotation,
                        this.polygonRotation
                )
        this.scale =
                typedArray.getFloat(
                        R.styleable.ChaosPolygonViewWrapper_polygonScale,
                        this.scale
                )

        val defaultFillColor =
                ContextCompat.getColor(context, R.color.colorAccentTranslucent)
        this.polygonFillPaint.color =
                typedArray.getColor(
                        R.styleable.ChaosPolygonViewWrapper_polygonFillColor,
                        defaultFillColor
                )
        this.polygonFillPaint.style = Paint.Style.FILL

        val defaultStrokeColor = ContextCompat.getColor(context, R.color.colorAccent)
        this.polygonStrokePaint.color =
                typedArray.getColor(
                        R.styleable.ChaosPolygonViewWrapper_polygonStrokeColor,
                        defaultStrokeColor
                )
        this.polygonStrokePaint.strokeWidth = this.strokeWidth
        this.polygonStrokePaint.style = Paint.Style.STROKE

        typedArray.recycle()

        /**
         * ViewGroup默认不绘制
         * 需要设置此项才会调用[onDraw]
         */
        this.setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        val radius = this.scale * (width / 2 - this.strokeWidth)
        // 绘制描边
        PolygonUtil.drawPolygon(
                canvas,
                this.numberOfSides,
                centerX,
                centerY,
                radius,
                this.cornerRadius,
                this.polygonRotation,
                this.polygonFillPaint
        )
        // 绘制填充色
        PolygonUtil.drawPolygon(
                canvas,
                this.numberOfSides,
                centerX,
                centerY,
                radius,
                this.cornerRadius,
                this.polygonRotation,
                this.polygonStrokePaint
        )
    }

}