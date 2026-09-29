package com.example.uniexpense.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Lightweight donut chart drawn with Canvas. Avoids pulling in an external
 * charting dependency (fewer Gradle/version surprises for a student project).
 */
class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Slice(val label: String, val value: Double, val color: Int)

    private var slices: List<Slice> = emptyList()
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rect = RectF()
    private val holeFraction = 0.55f

    fun setData(data: List<Slice>) {
        slices = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val total = slices.sumOf { it.value }
        val size = minOf(width, height).toFloat()
        val padding = 8f
        rect.set(padding, padding, size - padding, size - padding)

        if (total <= 0.0 || slices.isEmpty()) {
            arcPaint.color = Color.parseColor("#E4E7EC")
            canvas.drawArc(rect, 0f, 360f, true, arcPaint)
        } else {
            var startAngle = -90f
            for (slice in slices) {
                val sweep = (slice.value / total * 360.0).toFloat()
                arcPaint.color = slice.color
                canvas.drawArc(rect, startAngle, sweep, true, arcPaint)
                startAngle += sweep
            }
        }

        // Punch the donut hole using the window background color.
        val holeRadius = (size / 2f) * holeFraction
        val cx = size / 2f
        val cy = size / 2f
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundResolvedColor()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, holeRadius, holePaint)
    }

    private fun backgroundResolvedColor(): Int {
        // Falls back to white; screens place this on a white card.
        return Color.WHITE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
    }
}
