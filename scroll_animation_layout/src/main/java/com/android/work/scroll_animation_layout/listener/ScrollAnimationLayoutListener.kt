package com.android.work.scroll_animation_layout.listener

interface ScrollAnimationLayoutListener {
    /**
     * @param marginScale curMarginTop/sourceMarginTop
     */
    fun onScroll(marginScale:Float)

    /**
     * @param curMarginTop 当前marginTop
     * @param sourceMarginTop 起始marginTop
     */
    fun onScroll(curMarginTop:Int,sourceMarginTop:Int)
}