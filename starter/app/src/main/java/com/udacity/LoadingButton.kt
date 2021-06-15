package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toRectF
import androidx.core.graphics.withTranslation
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)

    private var valueAnimator = ValueAnimator()
    private var circleValueAnimator = ValueAnimator()

    private val loadingRect = RectF()
    private var progress = 0f
    private var currentSweepAngle = 0

    private var bounds = Rect()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
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

    var text: String = "Download"

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
                    textColor = getColor(R.styleable.LoadingButton_textColor, Color.BLACK)
                    loadingBarColor = context.resources.getColor(R.color.colorPrimaryDark)
                } finally {
                    recycle()
                }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawColor(colorForBackground)
            drawLoadingRectangle(canvas)
            drawButtonText(canvas)
            drawLoadingCircle(canvas)
        }
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
        canvas.restore()
    }

    private fun drawButtonText(canvas: Canvas) {
        canvas.save()
        val xPos = (canvas.width / 2.0f)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.color = textColor
        paint.getTextBounds(text, 0,text.length, this.bounds)
        val yPos = (canvas.height / 2.0f) + this.bounds.height() /2f
        canvas.drawText(text, xPos, yPos, paint)
        canvas.restore()
    }

    private fun drawLoadingCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(canvas.width /2f + (bounds.right /2f), (canvas.height/2f) + (bounds.top/2f))
        paint.color = Color.YELLOW
        canvas.drawArc(RectF(0f,0f, 50f, 50f), 90f, currentSweepAngle - 0f, true, paint)
        canvas.restore()
    }

    private fun animateLoadingBar() {
        valueAnimator.cancel()
        valueAnimator = ValueAnimator.ofInt(0, width).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                progress = animatedFraction
                invalidate()
            }
        }
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.start()
    }

    private fun animateLoadingCircle() {
        circleValueAnimator.cancel()
        circleValueAnimator = ValueAnimator.ofInt(0, 360).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                currentSweepAngle = animatedValue as Int
                invalidate()
            }
        }
        circleValueAnimator.repeatCount = ValueAnimator.INFINITE
        circleValueAnimator.start()
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
        super.onTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchUp() {
        buttonState = ButtonState.Loading
        invalidate()
    }

    private fun startLoad() {
        text = "We are loading"
        animateLoadingBar()
        animateLoadingCircle()
        invalidate()
    }

    private fun endLoad() {
        text = "Download"
        circleValueAnimator.cancel()
        valueAnimator.cancel()
        progress = 0f
        currentSweepAngle = 0
        invalidate()
    }

    fun updateButtonState(state: ButtonState) {
        this.buttonState = state
    }
}