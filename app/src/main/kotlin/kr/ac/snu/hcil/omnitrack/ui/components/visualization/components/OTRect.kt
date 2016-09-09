package kr.ac.snu.hcil.omnitrack.ui.components.visualization.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Created by younghokim on 16. 9. 8..
 */
class OTRect<T> : ADataEncodedDrawer<T> {

    var bound: RectF = RectF()
    var color: Int = Color.RED
    var paint: Paint

    constructor() {
        paint = Paint()

        paint.style = Paint.Style.FILL
    }

    constructor(paint: Paint) {
        this.paint = paint
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = color
        canvas.drawRect(bound, paint)
    }

}