package com.yalantis.library

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Created by anna on 1/2/18.
 */
class CardLayout
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                          defStyleAttr: Int = 0, var cardParams: CardParams) : FrameLayout(context, attrs, defStyleAttr) {

    private var rightImageView: ImageView? = null
    private var leftImageView: ImageView? = null
    private var cardView: View? = null
    var layoutAlpha = 1f
        set(value) {
            field = value
            cardView?.alpha = value
        }


    fun addOverlays(cardView: View?, @DrawableRes rightOverlayImage: Int?,
                    @DrawableRes leftOverlayImage: Int?) {
        this.cardView = cardView
        addView(cardView)

        leftOverlayImage?.let {
            leftImageView = ImageView(context)
            leftImageView?.setImageDrawable(ContextCompat.getDrawable(context, it))
            leftImageView?.alpha = 0f
            addView(leftImageView)
        }

        rightOverlayImage?.let {
            rightImageView = ImageView(context)
            rightImageView?.setImageDrawable(ContextCompat.getDrawable(context, it))
            rightImageView?.alpha = 0f
            addView(rightImageView)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rightImageView?.translationZ = SMALL_ELEVATION
            leftImageView?.translationZ = SMALL_ELEVATION
            cardView?.translationZ = NO_ELEVATION
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val layoutParamsCardView = cardView?.layoutParams as FrameLayout.LayoutParams

        cardView?.alpha = layoutAlpha
        cardParams.onParamsMeasure(layoutParamsCardView)

        rightImageView?.layoutParams?.let {
            (rightImageView?.layoutParams as FrameLayout.LayoutParams).apply {
                width = layoutParamsCardView.width
                height = layoutParamsCardView.height
                gravity = layoutParamsCardView.gravity

                setMargins(layoutParamsCardView.leftMargin, layoutParamsCardView.topMargin,
                        layoutParamsCardView.rightMargin, layoutParamsCardView.bottomMargin)
            }
            rightImageView?.setPadding(cardView?.paddingLeft ?: 0, cardView?.paddingTop ?: 0,
                    cardView?.paddingRight ?: 0, cardView?.paddingBottom ?: 0)
        }

        leftImageView?.layoutParams?.let {
            (leftImageView?.layoutParams as FrameLayout.LayoutParams).apply {
                width = layoutParamsCardView.width
                height = layoutParamsCardView.height
                gravity = layoutParamsCardView.gravity
                setMargins(layoutParamsCardView.leftMargin, layoutParamsCardView.topMargin,
                        layoutParamsCardView.rightMargin, layoutParamsCardView.bottomMargin)
            }

            leftImageView?.setPadding(cardView?.paddingLeft ?: 0, cardView?.paddingTop ?: 0,
                    cardView?.paddingRight ?: 0, cardView?.paddingBottom ?: 0)
        }
    }

    internal fun changeRightOverlayAlpha(progress: Float) {
        rightImageView?.alpha = progress
    }

    internal fun changeLeftOverlayAlpha(progress: Float) {
        leftImageView?.alpha = Math.abs(progress)
    }

    interface CardParams {
        fun onParamsMeasure(layoutParamsCardView: LayoutParams)
    }

    companion object {
        private const val NO_ELEVATION = 0F
        private const val SMALL_ELEVATION = 10F
    }

}