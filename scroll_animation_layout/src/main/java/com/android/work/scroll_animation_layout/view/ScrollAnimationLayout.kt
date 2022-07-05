package com.android.work.scroll_animation_layout.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.FrameLayout
import androidx.core.view.marginTop
import com.android.work.scroll_animation_layout.R
import com.android.work.scroll_animation_layout.ext.px
import com.android.work.scroll_animation_layout.listener.ScrollAnimationLayoutListener
import kotlin.math.abs

class ScrollAnimationLayout : FrameLayout {
    val TAG = "ScrollAnimationLayout"

    /**
     * 设置ScrollAnimation初始MarginTop值
     */
    private var sourceMarginTop: Int = 400.px()

    /**
     * 设置动画拖动的有效距离比例
     *
     * 拖动的有效距离 = sourceMarginTop / scale
     */
    private var dragScale: Int = 6

    /**
     * 动画执行的时间
     */
    private var execDuration: Long? = null

    /**
     * 滑动的相对距离
     */
    private var moveY: Float = 0f

    /**
     * 点击的位置坐标
     */
    private var dy: Float = 0f

    /**
     * 拉起动画
     */
    private var valueAnimation = ValueAnimator()

    /**
     * 收拾快速抬起->惯性
     */
    private var obtain: VelocityTracker = VelocityTracker.obtain()

    /**
     * 记录触摸时的marginTop距离
     */
    private var downMarginTop: Int = sourceMarginTop

    /**
     * 第一次绘制标识
     */
    private var isFirstDraw = true

    /**
     * 手势惯性灵敏度
     * 当超过这个值，才会自动滑动
     */
    private var velocitySensitivity: Int = 1200

    /**
     * 上一次移动的位置
     */
    private var lastDy: Float = 0f

    /**
     * 布局滚动监听回调
     */
    private var mScrollAnimationLayoutListener: ScrollAnimationLayoutListener? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        isFirstDraw = true
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val typedValue = context.obtainStyledAttributes(attrs, R.styleable.ScrollAnimationLayout)
        sourceMarginTop = typedValue.getDimensionPixelSize(
            R.styleable.ScrollAnimationLayout_source_marginTop,
            400.px()
        )
        dragScale = typedValue.getInt(R.styleable.ScrollAnimationLayout_drag_scale, 6)
        velocitySensitivity =
            typedValue.getInt(R.styleable.ScrollAnimationLayout_velocity_sensitivity, 1200)
        handleExecDuration(typedValue)
        typedValue.recycle()
        this.isClickable = true
        setListener()
    }

    private fun handleExecDuration(typedValue: TypedArray) {
        val execDuration = typedValue.getInt(R.styleable.ScrollAnimationLayout_exec_duration, 0)
        val durationUnit = typedValue.getInt(R.styleable.ScrollAnimationLayout_duration_unit, 0)
        if (execDuration == 0) {
            this.execDuration = null
        } else {
            this.execDuration = when (durationUnit) {
                0 -> execDuration * 1L
                else -> execDuration * 1000L
            }
        }
    }

    /**
     * 监听事件
     */
    private fun setListener() {
        valueAnimation.addUpdateListener {
            val value = it.animatedValue
            if (value is Int) {
                Log.d(TAG, "执行动画当前的进度:$value")
                setLayoutParams(value)
            }
        }
    }

    /**
     * 滚动不同位置，处理是否拦截事件
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.d(TAG, "onInterceptTouchEvent action:${ev?.action}")
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                dy = ev.y
                downMarginTop = marginTop
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN dy--->$dy  downMarginTop--->$downMarginTop")
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY = ev.y - dy
                Log.d(
                    TAG,
                    "onInterceptTouchEvent ACTION_MOVE marginTop--->$marginTop   sourceMarginTop--->$sourceMarginTop   moveY--->$moveY   rawY--->${ev.y}"
                )
                if (marginTop <= sourceMarginTop && marginTop != 0) {
                    // 当初始位置时，检查滑动方向
                    if (moveY < 0) {
                        // 下滑拦截事件
                        Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE 初始位置上滑")
                        return true
                    }
                    return true
                }
                if (marginTop == 0) {
                    Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE 交由子view处理事件")
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * 当拦截事件，交由自己onTouchEvent处理
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        obtain.computeCurrentVelocity(200)
        obtain.addMovement(event)
        Log.d(
            TAG,
            "onTouchEvent marginTop--->$marginTop   sourceMarginTop--->$sourceMarginTop   action--->${event?.action}"
        )
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dy = event.y
                Log.d(TAG, "onTouchEvent ACTION_DOWN dy--->$dy")
            }
            MotionEvent.ACTION_MOVE -> {
                if (dy == 0f) {
                    dy = event.y
                }
                moveY = event.y - dy
                var curMarginTop = (marginTop + moveY).toInt()
                Log.d(
                    TAG,
                    "onTouchEvent ACTION_MOVE moveY--->$moveY   rawY--->${event.y}   dy--->$dy   curMarginTop--->$curMarginTop"
                )
                if (curMarginTop < 0) {
                    Log.d(TAG, "滚动到顶了")
                    curMarginTop = 0
                }
                if (curMarginTop > sourceMarginTop) {
                    Log.d(TAG, "滚动到底了")
                    curMarginTop = sourceMarginTop
                }
                setLayoutParams(curMarginTop)
                lastDy = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val velocityY = obtain.yVelocity
                val offset = sourceMarginTop / dragScale
                dy = 0f
                moveY = 0f
                Log.d(
                    TAG,
                    "onTouchEvent ACTION_UP 执行松手动画velocityY:$velocityY  offset:$offset   marginTop:$marginTop  downMarginTop:$downMarginTop  dragScale:$dragScale"
                )
                if (abs(velocityY) >= velocitySensitivity) {
                    // 执行动画
                    execAnimation(velocityY < 0)
                    return true
                }
                // 松手后进行阻尼调整
                if (abs(downMarginTop - marginTop) < offset) {
                    if (marginTop > sourceMarginTop / 2 && velocityY < 0) {
                        // 上滑距离不够，执行动画
                        Log.d(TAG, "上滑距离不够，执行动画")
                        execAnimation(marginTop, sourceMarginTop)
                    } else if (marginTop < sourceMarginTop / 2 && velocityY > 0) {
                        // 下滑距离不够，执行动画
                        Log.d(TAG, "下滑距离不够，执行动画")
                        execAnimation(marginTop, 0)
                    }
                } else {
                    // 滑动距离够，执行动画
                    execAnimation(velocityY < 0)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 子view通知
     */
    fun handleEvent(tempDy: Float, event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                moveY = event.y - tempDy
                var curMarginTop = (marginTop + moveY).toInt()
                Log.d(
                    TAG,
                    "onTouchEvent handleEvent ACTION_MOVE marginTop--->$marginTop  moveY--->$moveY   rawY--->${event.y}   dy--->$tempDy   curMarginTop--->$curMarginTop"
                )
                if (curMarginTop < 0) {
                    curMarginTop = 0
                }
                if (curMarginTop > sourceMarginTop) {
                    curMarginTop = sourceMarginTop
                }
                setLayoutParams(curMarginTop)
            }

            MotionEvent.ACTION_UP -> {
                val offset = sourceMarginTop / dragScale
                Log.d(
                    TAG,
                    "onTouchEvent ACTION_UP handleEvent offset:$offset  downMarginTop:$downMarginTop  marginTop:$marginTop  moveY:$moveY  sourceMarginTop:$sourceMarginTop"
                )
                // 松手后进行阻尼调整
                if (abs(downMarginTop - marginTop) < offset) {
                    if (marginTop > sourceMarginTop / 2 && moveY < 0) {
                        // 上滑距离不够，执行动画
                        Log.d(TAG, "上滑距离不够，执行动画")
                        execAnimation(marginTop, sourceMarginTop)
                    } else if (marginTop < sourceMarginTop / 2 && moveY > 0) {
                        // 下滑距离不够，执行动画
                        Log.d(TAG, "下滑距离不够，执行动画")
                        execAnimation(marginTop, 0)
                    }
                } else {
                    // 滑动距离够，执行动画
                    execAnimation(moveY < 0)
                }
            }
        }
    }

    private fun setLayoutParams(curMarginTop: Int) {
        val mLayoutParams = layoutParams as MarginLayoutParams
        mLayoutParams.setMargins(0, curMarginTop, 0, 0)
        this.layoutParams = mLayoutParams
        mScrollAnimationLayoutListener?.onScroll(curMarginTop * 1f / sourceMarginTop)
        mScrollAnimationLayoutListener?.onScroll(curMarginTop , sourceMarginTop)
    }

    /**
     * 执行动画
     */
    private fun execAnimation(isPullAnimation: Boolean) {
        valueAnimation.cancel()
        if (isPullAnimation) {
            Log.d(TAG, "execAnimation：上滑距离够，执行动画")
            valueAnimation.setIntValues(marginTop, 0)
            valueAnimation.duration = execDuration ?: 500
        } else {
            Log.d(TAG, "execAnimation：下滑距离够，执行动画")
            valueAnimation.setIntValues(marginTop, sourceMarginTop)
            valueAnimation.duration = execDuration ?: 500
        }
        valueAnimation.start()
    }

    /**
     * 根据指定起始终点偏移量进行动画执行
     */
    private fun execAnimation(startOffset: Int, endOffset: Int) {
        valueAnimation.cancel()
        valueAnimation.setIntValues(startOffset, endOffset)
        valueAnimation.duration = execDuration ?: 500
        valueAnimation.start()
    }

    /**
     * 设置布局滚动时的监听
     */
    fun setScrollAnimationLayoutListener(scrollAnimationLayoutListener: ScrollAnimationLayoutListener) {
        this.mScrollAnimationLayoutListener = scrollAnimationLayoutListener
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 第一次绘制时，将设置的sourceMarginTop设置给layout的marginTop上
        if (isFirstDraw) {
            setLayoutParams(sourceMarginTop)
            isFirstDraw = false
        }
    }

    override fun onDetachedFromWindow() {
        valueAnimation.cancel()
        super.onDetachedFromWindow()
    }


}