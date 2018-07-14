package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import org.indiv.dls.games.verboscruzados.feature.R

/**
 * View for displaying game statistics.
 */
open class StatsGraphicView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null,
                                                      defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var statsMap: Map<Int, Int> = emptyMap()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentWidth = 0
    private var currentHeight = 0

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        paint.style = Paint.Style.FILL
    }

    //endregion

    //region OVERRIDE FUNCTIONS --------------------------------------------------------------------

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        val left = 10
        val top = 10
        val right = 40
        val bottom = 40
        paint.color = (0xffFFFFFF).toInt()
        val rect = Rect(left, top, right, bottom)
        canvas?.drawRect(rect, paint)

        paint.color = (0xff006F00).toInt()
        val rect2 = Rect(50, 50, 80, 80)
        canvas?.drawRect(rect2, paint)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentWidth = w
        currentHeight = h
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun setStats(statsMap: Map<Int, Int>) {
        this.statsMap = statsMap
        invalidate()
    }

    //endregion
}
