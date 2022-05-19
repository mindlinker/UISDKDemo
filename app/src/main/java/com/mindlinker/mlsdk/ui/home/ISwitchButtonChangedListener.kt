package com.mindlinker.mlsdk.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.maxhub.logger.Logger
import com.mindlinker.mlsdk.R
import com.mindlinker.sdk.utils.dimenToFloat
import kotlinx.android.synthetic.main.layout_switch_button.view.*

interface ISwitchButtonChangedListener {
    fun onCheckChanged(isChecked: Boolean)
}
/**
 * Created by aaron on 2/25/21
 */
class SwitchButton : LinearLayout {

    companion object {
        private const val TAG = "SwitchButton"
        const val ANIMATION_DURATION = 150L
    }

    private var switchChangedListener: ISwitchButtonChangedListener? = null
    private var mSwitchCloseAnimatorSet: AnimatorSet? = null
    private var mSwitchOpenAnimatorSet: AnimatorSet? = null
    private var isChecked = false
    private var isTouchable = true

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.layout_switch_button, this, true)
        initSwitchAnimation(shareSwitchButton)
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton)
            val isChecked = typedArray.getBoolean(R.styleable.SwitchButton_checked, false)
            isTouchable = typedArray.getBoolean(R.styleable.SwitchButton_touchable, true)
            typedArray.recycle()
            Logger.i(TAG, "initView isChecked: $isChecked ")
            setChecked(isChecked, false)
        }
        setOnClickListener { toggleCheck() }
    }

    private fun initSwitchAnimation(switchButton: View) {
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }
        val switchOpenAnimation = ObjectAnimator.ofFloat(switchButton, "translationX", context.dimenToFloat(R.dimen.ml_px_2), context.dimenToFloat(R.dimen.ml_px_34))
        switchOpenAnimation?.addListener(listener)
        switchOpenAnimation?.interpolator = AccelerateDecelerateInterpolator()
        switchOpenAnimation?.duration = ANIMATION_DURATION

        val switchCloseAnimation = ObjectAnimator.ofFloat(switchButton, "translationX", context.dimenToFloat(R.dimen.ml_px_34), context.dimenToFloat(R.dimen.ml_px_2))
        switchCloseAnimation?.addListener(listener)
        switchCloseAnimation?.interpolator = AccelerateDecelerateInterpolator()
        switchCloseAnimation?.duration = ANIMATION_DURATION

        val switchOpenBgAnimation = ObjectAnimator.ofFloat(shareSwitchOpenBg, "alpha", 0f, 1f)
        switchOpenBgAnimation?.duration = ANIMATION_DURATION

        val switchCloseBgAnimation = ObjectAnimator.ofFloat(shareSwitchOpenBg, "alpha", 1f, 0f)
        switchCloseBgAnimation?.duration = ANIMATION_DURATION

        mSwitchCloseAnimatorSet = AnimatorSet()
        mSwitchCloseAnimatorSet?.playTogether(switchCloseAnimation, switchCloseBgAnimation)

        mSwitchOpenAnimatorSet = AnimatorSet()
        mSwitchOpenAnimatorSet?.playTogether(switchOpenAnimation, switchOpenBgAnimation)
    }

    fun setSwitchButtonChangedListener(listener: ISwitchButtonChangedListener) {
        switchChangedListener = listener
    }

    fun setChecked(isChecked: Boolean, withAnimator: Boolean = false): Boolean {
        // 若初始化时从外部调用setChecked方法设置初始值，则不要在xml设置初始值，否则会出现一直为xml设置的默认值，外部设置无效
        if (mSwitchCloseAnimatorSet!!.isRunning || mSwitchOpenAnimatorSet!!.isRunning || this.isChecked == isChecked) {
            return false
        }
        this.isChecked = isChecked
        if (withAnimator) {
            mSwitchOpenAnimatorSet?.duration = ANIMATION_DURATION
            mSwitchCloseAnimatorSet?.duration = ANIMATION_DURATION
        } else {
            mSwitchOpenAnimatorSet?.duration = 0
            mSwitchCloseAnimatorSet?.duration = 0
        }
        if (isChecked) {
            mSwitchOpenAnimatorSet?.start()
        } else {
            mSwitchCloseAnimatorSet?.start()
        }
        return true
    }

    fun toggleCheck() {
        Logger.i(TAG, "toggleCheck current isChecked: ${this.isChecked}")
        val changedChecked = !this.isChecked
        val result = setChecked(changedChecked, true)
        if (result) {
            switchChangedListener?.onCheckChanged(isChecked)
        }
    }

    fun isChecked(): Boolean {
        return this.isChecked
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return if (isTouchable) {
            super.dispatchTouchEvent(event)
        } else {
            false
        }
    }
}
