package com.example.tokidosapplication.view


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.withTranslation
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.tokidosapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch


class LoadingView @JvmOverloads constructor(
    context: Context,attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), DefaultLifecycleObserver {
    private var mWidth = 0f
    private var mHeight = 0f
    private var mRadius = 120f
    private var mSmallCircleRadius = 10f
    private var rotateDegree = 0 % 360
    private var rectF = RectF()
    private var padding = 10f
    private var totalCircles = 8
    private var job: Job? = null
    private var mValueAnimator = ValueAnimator()


    private val solidLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        isDither = true
        color = context.getColor(R.color.black)
    }
    private val circlePaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 5f
        isAntiAlias = true
        isDither = true
        alpha = 255
        shader = RadialGradient(0f, -mRadius-mSmallCircleRadius, mRadius*2+mSmallCircleRadius, context.getColor(R.color.navy_blue), context.getColor(R.color.white), Shader.TileMode.MIRROR)
    }
    init {
        rectF.apply {
            left = -mRadius
            top= -mRadius
            right= mRadius
            bottom= mRadius
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        if(mWidth > mHeight){
            mRadius= mHeight/2 - mSmallCircleRadius - padding
        }else{
            mRadius= mWidth/2 - mSmallCircleRadius - padding
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.withTranslation(mWidth/2, mHeight/2){
            //drawRect(rectF,solidLinePaint)
            //drawCircle(0f,0f,mRadius, solidLinePaint)
            drawSmallCircle(canvas)
        }
    }

    private fun drawSmallCircle(canvas: Canvas ){
        for (i in 0..<totalCircles){
            val angleBetweenCircle = 360f / totalCircles
            val angle = angleBetweenCircle * i
            val x1 = 0f + mRadius * Math.cos(angle * 3.14 / 180)
            val y1 = 0f + mRadius * Math.sin(angle * 3.14 / 180)
            val x2 = 0f + (mRadius+mSmallCircleRadius) * Math.cos(rotateDegree * 3.14 / 180)
            val y2 = 0f + (mRadius+mSmallCircleRadius) * Math.sin(rotateDegree* 3.14 / 180)
            val circlePaint = Paint().apply {
                style = Paint.Style.FILL
                strokeWidth = 5f
                isAntiAlias = true
                isDither = true
                shader = RadialGradient(x2.toFloat(), y2.toFloat(), (mRadius+mSmallCircleRadius)*2, context.getColor(R.color.navy_blue), context.getColor(R.color.white), Shader.TileMode.MIRROR)

            }
            canvas.apply {
                drawCircle(x1.toFloat(), y1.toFloat(),mSmallCircleRadius, circlePaint)
                drawCircle(x1.toFloat(), y1.toFloat(),mSmallCircleRadius, solidLinePaint)
            }
        }
    }
    fun startLoadingAnimate(){
        mValueAnimator.setIntValues(0, 360);
        mValueAnimator.setDuration(1000)
        mValueAnimator.interpolator = DecelerateInterpolator()
        mValueAnimator.addUpdateListener { animation ->
            val angle = animation.animatedValue.toString().toInt()
            rotateDegree = (rotateDegree + 45) % 360
            invalidate()
        }
        if (mValueAnimator.isRunning) {
            mValueAnimator.end();
        }
        mValueAnimator.start();
    }
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        job = CoroutineScope(Dispatchers.Main).launch{
            while (true){
                delay(100)
                rotateDegree = (rotateDegree + 45) % 360
                invalidate()
            }
        }
    }
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        job?.cancel()
    }

}