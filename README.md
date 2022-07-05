# ScrollAnimationLayout
仿饿了么订单详情滑动动画

# 依赖引用
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  dependencies {
	        implementation 'com.github.android-work:ScrollAnimationLayout:-SNAPSHOT'
	}

#布局引用
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">
    
    # 一般是配合布局滑动时，顶部布局随滑动到距离进行透明度的变化，按需自定义布局
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:background="#000"
        android:id="@+id/frameLayout"/>

    # 滑动布局
    <com.android.work.scroll_animation_layout.view.ScrollAnimationLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/scroll_animation_layout"
        android:background="#ffaacc"
        android:paddingTop="40dp"
        app:source_marginTop="400dp"  # 初始位置marginTop，默认值400dp
        app:duration_unit="millisecond" # 动画执行时间单位，默认毫秒
        app:exec_duration="500" # 动画执行时常，默认500毫秒
        app:drag_scale="6" # 拖动的阻尼比值，越大阻你越小
        app:velocity_sensitivity="1200"> # 手指滑动的惯性速度

        # 自定义NestedScrollView,存在多个子view需要使用布局layout包裹
        <com.android.work.scroll_animation_layout.view.AnimationNestScrollView
            android:layout_margin="10dp"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#aaccdd">
            # 滚动的内容
            <TextView
                android:id="@+id/text"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/test"
                android:textSize="18sp"
                android:textColor="#000"/>
        </com.android.work.scroll_animation_layout.view.AnimationNestScrollView>
    </com.android.work.scroll_animation_layout.view.ScrollAnimationLayout>

</LinearLayout>



#代码使用
# 按需使用，一般是布局顶部标题栏，根据滑动距离进行更改布局透明度
frameLayout = findViewById(R.id.frameLayout)

# 设置对布局滚动的监听，从而对标题栏布局的透明度更改
findViewById<ScrollAnimationLayout>(R.id.scroll_animation_layout).setScrollAnimationLayoutListener(object:ScrollAnimationLayoutListener{
  override fun onScroll(marginScale: Float) {
      Log.d(TAG,"marginScale:$marginScale")
      frameLayout?.alpha = (1-marginScale)
  }

  override fun onScroll(curMarginTop: Int, sourceMarginTop: Int) {
  }

})
