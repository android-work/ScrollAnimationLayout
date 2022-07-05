package com.android.work.scroll_animation_layout.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.view.marginTop
import androidx.core.widget.NestedScrollView

class AnimationNestScrollView : NestedScrollView {
    val TAG = "AnimationNestScrollView"

    /**
     * 点击位置
     */
    private var dy: Float = 0f

    private var moveY: Float = 0f

    /**
     * 临时标志
     */
    private var tempFlag = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> {
                dy = ev.y
                Log.d(TAG,"onTouchEvent ACTION_DOWN dy--->$dy")
            }
            MotionEvent.ACTION_MOVE -> {
                if (dy == 0f){
                    dy = ev.y
                }
                moveY = ev.y - dy
                Log.d(TAG,"onTouchEvent ACTION_MOVE scrollY--->$scrollY   moveY--->$moveY  ev.y--->${ev.y}   dy--->$dy")
                if (moveY > 0){
                    if (scrollY == 0){
                        // 下滑且到顶,响应外部布局
                        if (parent is ScrollAnimationLayout){
                            if (tempFlag) {
                                dy = ev.y
                                tempFlag = false
                            }
                            val scrollAnimationLayout = (parent as ScrollAnimationLayout)
                            Log.d(TAG,"onTouchEvent 下拉通知父布局响应事件")
                            scrollAnimationLayout.handleEvent(dy,ev)
                        }
                        return true
                    }
//                    tempFlag = true
                    return super.onTouchEvent(ev)
                }else{
                    if (parent is ScrollAnimationLayout){
                        val scrollAnimationLayout = (parent as ScrollAnimationLayout)
                        if (scrollAnimationLayout.marginTop != 0){
                            if (tempFlag) {
                                dy = ev.y
                                tempFlag = false
                            }
                            Log.d(TAG,"onTouchEvent 上拉通知父布局响应事件")
                            scrollAnimationLayout.handleEvent(dy,ev)
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // 当子view通知父布局响应事件时，抬手处理
                Log.d(TAG,"onTouchEvent ACTION_UP")
                tempFlag = true
                if (scrollY == 0) {
                    if (parent is ScrollAnimationLayout) {
                        val scrollAnimationLayout = (parent as ScrollAnimationLayout)
                        if (scrollAnimationLayout.marginTop != 0) {
                            Log.d(TAG, "onTouchEvent 上拉通知父布局响应事件")
                            scrollAnimationLayout.handleEvent(dy, ev)
                            return true
                        }
                    }
                }
                dy = 0f
            }
        }
        return super.onTouchEvent(ev)
    }
}