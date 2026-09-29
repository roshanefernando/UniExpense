package com.example.uniexpense.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/** Simple vertical bar chart for "expense per month". No external dependency. */
class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Bar(val label: String, val value: Double)

    private var bars: List<Bar> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#22A876") }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#667085")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    fun setData(data: List<Bar>) {
        bars = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) return

        val maxValue = bars.maxOf { it.value }.coerceAtLeast(1.0)
        val chartBottom = height - 50f
        val chartTop = 20f
        val chartHeight = chartBottom - chartTop
        val slotWidth = width.toFloat() / bars.size
        val barWidth = slotWidth * 0.5f

        bars.forEachIndexed { index, bar ->
            val barHeight = (bar.value / maxValue * chartHeight).toFloat()
            val left = index * slotWidth + (slotWidth - barWidth) / 2f
            val top = chartBottom - barHeight
            val right = left + barWidth
            canvas.drawRoundRect(left, top, right, chartBottom, 8f, 8f, barPaint)
            canvas.drawText(bar.label, left + barWidth / 2f, height - 15f, labelPaint)
        }
    }
}
