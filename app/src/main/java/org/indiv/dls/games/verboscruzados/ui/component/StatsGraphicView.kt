package org.indiv.dls.games.verboscruzados.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import org.indiv.dls.games.verboscruzados.R
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * View for displaying game statistics.
 */
open class StatsGraphicView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null,
                                                      defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var rowCount: Int = 1
    private var columnCount: Int = 1
    private var statsMap: Map<Pair<Int, Int>, Int> = emptyMap()

    private var currentWidth = 0
    private var currentHeight = 0
    private var cellWidth = 0f
    private var cellHeight = 0f

    private val borderPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
    private val borderPixelsOneSide = borderPixels / 2

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        greenPaint.style = Paint.Style.FILL
        greenPaint.color = ResourcesCompat.getColor(resources, R.color.stats_green, null)
    }

    //endregion

    //region OVERRIDE FUNCTIONS --------------------------------------------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        statsMap.entries.forEach {
            drawStat(canvas, it.key, it.value)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentWidth = w - borderPixels
        currentHeight = h - borderPixels
        updateCellDimensions()
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun setStats(rowCount: Int, columnCount: Int, statsMap: Map<Pair<Int, Int>, Int>) {
        this.rowCount = rowCount.coerceAtLeast(1)
        this.columnCount = columnCount.coerceAtLeast(1)
        updateCellDimensions()
        this.statsMap = statsMap
        invalidate()
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun drawStat(canvas: Canvas?, statsPosition: Pair<Int, Int>, statsValue: Int) {
        val rect = calculateRect(statsPosition)
        greenPaint.alpha = calculateAlpha(statsValue)
        canvas?.drawRect(rect, greenPaint)
    }

    private fun calculateAlpha(statsValue: Int): Int {
        val fullAlpha = (0xff).toDouble()
        val factor = sqrt(statsValue.toDouble()).coerceAtMost(20.0) / 20.0
        return (fullAlpha * factor).roundToInt()
    }

    private fun calculateRect(statsPosition: Pair<Int, Int>): Rect {
        val x = statsPosition.first.coerceIn(0 until columnCount)
        val y = statsPosition.second.coerceIn(0 until rowCount)
        val left = (x * cellWidth + borderPixelsOneSide).roundToInt()
        val right = ((x + 1) * cellWidth + borderPixelsOneSide).roundToInt()
        val top = (y * cellHeight + borderPixelsOneSide).roundToInt()
        val bottom = ((y + 1) * cellHeight + borderPixelsOneSide).roundToInt()
        return Rect(left, top, right, bottom)
    }

    private fun updateCellDimensions() {
        cellWidth = currentWidth.toFloat() / columnCount
        cellHeight = currentHeight.toFloat() / rowCount
    }

    //endregion

}
