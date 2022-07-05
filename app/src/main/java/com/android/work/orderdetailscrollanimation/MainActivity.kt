package com.android.work.orderdetailscrollanimation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.android.work.scroll_animation_layout.listener.ScrollAnimationLayoutListener
import com.android.work.scroll_animation_layout.view.ScrollAnimationLayout

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var frameLayout:FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.text).setOnClickListener {
            Toast.makeText(this,"text被点击了",0).show()
        }

        frameLayout = findViewById(R.id.frameLayout)

        findViewById<ScrollAnimationLayout>(R.id.scroll_animation_layout).setScrollAnimationLayoutListener(object:ScrollAnimationLayoutListener{
            override fun onScroll(marginScale: Float) {
                Log.d(TAG,"marginScale:$marginScale")
                frameLayout?.alpha = (1-marginScale)
            }

            override fun onScroll(curMarginTop: Int, sourceMarginTop: Int) {
            }

        })
    }
}