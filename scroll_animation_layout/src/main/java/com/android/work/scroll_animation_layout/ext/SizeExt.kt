package com.android.work.scroll_animation_layout.ext

import com.android.work.scroll_animation_layout.AnimationProvider

/**
 * dp -> px -> int
 */
fun Int.px() = ((AnimationProvider.mContext?.resources?.displayMetrics?.density?:1f) * this).toInt()

/**
 * dp -> px -> float
 */
fun Float.px() = (AnimationProvider.mContext?.resources?.displayMetrics?.density?:1f) * this
