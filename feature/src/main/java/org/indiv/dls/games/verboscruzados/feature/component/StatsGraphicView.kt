package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * View for displaying game statistics.
 */
open class StatsGraphicView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null,
                                                      defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val PAINT_GREEN = Paint(Paint.ANTI_ALIAS_FLAG)
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

    val borderPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
    val borderPixelsOneSide = borderPixels / 2

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        PAINT_GREEN.style = Paint.Style.FILL
        PAINT_GREEN.color = (0xff008F00).toInt()
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
        PAINT_GREEN.alpha = calculateAlpha(statsValue)
        canvas?.drawRect(rect, PAINT_GREEN)
    }

    private fun calculateAlpha(statsValue: Int): Int {

        // 1  2  4  8  16 32 64 128 256 512 1024 2048 4096 8182 16364 32728
        // 0  1  2  3  4  5  6  7   8   9   10   11   12   13   14    15

        val fullAlpha = (0xff).toDouble()
        val factor = Math.min(Math.log(statsValue.toDouble()), 16.0) / 16.0
        return Math.round(fullAlpha * factor).toInt()
    }

    private fun calculateRect(statsPosition: Pair<Int, Int>): Rect {
        val x = statsPosition.first.coerceIn(0 until columnCount)
        val y = statsPosition.second.coerceIn(0 until rowCount)
        val left = Math.round(x * cellWidth + borderPixelsOneSide)
        val right = Math.round(left + cellWidth)
        val top = Math.round(y * cellHeight + borderPixelsOneSide)
        val bottom = Math.round(top + cellHeight)
        return Rect(left, top, right, bottom)
    }

    private fun updateCellDimensions() {
        cellWidth = currentWidth.toFloat() / columnCount
        cellHeight = currentHeight.toFloat() / rowCount
    }

    //endregion

}
