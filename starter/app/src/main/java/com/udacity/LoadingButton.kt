package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val STROKE_WIDTH = 1000f // has to be float
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)

    private var valueAnimator = ValueAnimator()

    private val defaultRect = RectF()
    private val loadingRect = RectF()
    private var progress = 0f

    var text: String = "Download"

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                animateLoadingBar()
            }
            ButtonState.Loading -> {
                startLoad()
            }
            ButtonState.Completed -> {
                endLoad()
            }
        }
    }

    private var colorForBackground: Int
    private var textColor: Int
    private var loadingBarColor: Int

    private val paint = Paint().apply {
        // Smooth out edges of what is drawn without affecting shape.
        color = drawColor
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.stroke_width)
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,0).apply {
                try {
                    colorForBackground = getColor(R.styleable.LoadingButton_backgroundColor, Color.GREEN)
                    textColor = getColor(R.styleable.LoadingButton_textColor, Color.GREEN)
                    loadingBarColor = context.resources.getColor(R.color.colorPrimaryDark)
                } finally {
                    recycle()
                }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
//            when (buttonState) {
//                ButtonState.Clicked -> {
//                    drawContainer(canvas)
//                    canvas.drawColor(colorForBackground)
//                    drawButtonText(canvas)
//                    animateLoadingBar(canvas)
//                }
//                else -> {
//                    drawContainer(canvas)
//                    canvas.drawColor(Color.RED)
//                    drawButtonText(canvas)
//                    drawLoadingCircle(canvas)
//                }
//            }
//            canvas.drawColor(colorForBackground)
            drawContainer(canvas)
            if (progress < 1f) {
                drawLoadingRectangle(canvas)
            }
            drawButtonText(canvas)
        }
    }

    private fun drawContainer(canvas: Canvas) {
        canvas.save()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = colorForBackground
        defaultRect.left = 0f
        defaultRect.top = 0f
        defaultRect.right = canvas.width - 0f
        defaultRect.bottom = canvas.height - 0f

        canvas.drawRect(defaultRect, paint)
//        canvas.drawRect(50f, 0f, canvas.width + 50f, canvas.height - 0f, paint)
        canvas.restore()
    }

    private fun drawLoadingRectangle(canvas: Canvas) {
        canvas.save()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = loadingBarColor
        loadingRect.left = 0f
        loadingRect.top = 0f
        loadingRect.right = progress * 1000
        loadingRect.bottom = canvas.height - 0f

        canvas.drawRect(loadingRect, paint)
//        canvas.drawRect(50f, 0f, canvas.width + 50f, canvas.height - 0f, paint)
        canvas.restore()
    }

    private fun drawButtonText(canvas: Canvas) {
        canvas.save()
        val xPos = (canvas.width / 2.0f)
//        val xPos = defaultRect.left + defaultRect.width() / 2
        val yPos = (canvas.height / 2.0f)
//        val yPos = defaultRect.top + defaultRect.height() / 2
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.color = textColor
        canvas.drawText(text, xPos, yPos, paint)
        canvas.restore()
    }

    private fun animateLoadingBar() {
        valueAnimator.cancel()
        Log.v("LoadingButton", "Start Loading animation")
        valueAnimator = ValueAnimator.ofInt(0, width).apply {
            duration = 500
            interpolator = LinearInterpolator()
            addUpdateListener {
                progress = animatedFraction
                Log.v("LoadingButton", "Progress Value: $progress")
                invalidate()
            }
        }
        valueAnimator.start()
    }

    private fun drawLoadingCircle(canvas: Canvas) {
        canvas.save()
        val radius = 50f
        val circleX = width - radius - 50f
        val circleY = defaultRect.top + defaultRect.height() / 2
        canvas.drawCircle(circleX, circleY, radius, paint)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchUp() {
        buttonState = ButtonState.Clicked
        invalidate()
    }

    private fun startLoad() {
        text = "Loading"
        invalidate()
    }

    private fun endLoad() {
        text = "Download"
        invalidate()
    }
}