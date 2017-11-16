package com.yalantis.library

import android.animation.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.util.Log

/**
 * Created by anna on 11/10/17.
 */
class CardOperator(val koloda: Koloda, val cardView: View, val adapterPosition: Int, val cardCallback: CardCallback) {

    companion object {
        private const val DEFAULT_OFF_SCREEN_ANIMATION_DURATION = 600
        private val DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION = 150
        private val DEFAULT_RESET_ANIMATION_DURATION = 600
    }

    private val cardGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1?.x ?: 0f > e2?.x ?: 0f && cardBeyondLeftBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenLeft(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, true)
                return true
            } else if (e1?.x ?: 0f < e2?.x ?: 0f && cardBeyondRightBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenRight(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, true)
                return true
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            cardCallback.onCardSingleTap(adapterPosition, cardView)
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            cardCallback.onCardLongPress(adapterPosition, cardView)
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent?) {
            cardCallback.onCardDoubleTap(adapterPosition, cardView)
            super.onLongPress(e)
        }
    }

    private val gestureDetector = GestureDetector(cardView.context, cardGestureListener, null, true)
    private var isSwipedOffScreen = false
    private var currentCardAnimator: ObjectAnimator? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var activePointerId = 0
    private var translationX = 0f
    internal var isBeingDragged: Boolean = false
    private var initialCardPositionX: Float = 0.toFloat()
    private var initialCardPositionY: Float = 0.toFloat()

    init {
        cardView.setOnTouchListener { view, event -> onTouchView(view, event) }
    }

    fun animateOffScreenLeft(duration: Int, notify: Boolean) {
        var targetY = cardView.y

        if (initialCardPositionY > cardView.y) {
            targetY -= initialCardPositionY - cardView.y
        } else {
            targetY += (cardView.y - initialCardPositionY) * 3
        }

        val maxCardWidth = koloda.getMaxCardWidth(cardView)
        val transitionX = (koloda.parentWidth / 2 + cardView.width / 2) +
                cardView.x + Math.abs(maxCardWidth / 2)

        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x - transitionX)
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, targetY)
        animateCardOffScreen(duration, pvhX, pvhY)

        cardCallback.onCardSwipedLeft(adapterPosition, cardView, notify)
    }

    fun animateOffScreenRight(duration: Int, notify: Boolean) {
        var targetY = cardView.y

        if (initialCardPositionY > cardView.y) {
            targetY -= initialCardPositionY - cardView.y
        } else {
            targetY += (cardView.y - initialCardPositionY) * 3
        }

        val maxCardWidth = koloda.getMaxCardWidth(cardView)
        val transitionX = koloda.parentWidth - (koloda.parentWidth / 2 - cardView.width / 2) +
                cardView.x + Math.abs(maxCardWidth / 2)

        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x + transitionX)
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, targetY)
        animateCardOffScreen(duration, pvhX, pvhY)

        cardCallback.onCardSwipedRight(adapterPosition, cardView, notify)
    }

    private fun animateCardOffScreen(duration: Int, pvhX: PropertyValuesHolder, pvhY: PropertyValuesHolder) {
        swipedCardOffScreen()

        val valueAnimator = ValueAnimator()
        valueAnimator.setValues(pvhX, pvhY)
        valueAnimator.interpolator = TimeInterpolator { input -> input }
        valueAnimator.addUpdateListener {
            cardView.translationX = it.getAnimatedValue("x") as Float - initialCardPositionX
            cardView.translationY = it.getAnimatedValue("y") as Float - initialCardPositionY
            Log.i("Animation start", " wrong?")
            updateCardProgress(0f)
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                cardCallback.onCardOffScreen(adapterPosition, cardView)
            }
        })
        valueAnimator.start()
    }

    private fun checkCardPosition() {
        when {
            cardBeyondLeftBorder() -> animateOffScreenLeft(DEFAULT_OFF_SCREEN_ANIMATION_DURATION, true)
            cardBeyondRightBorder() -> animateOffScreenRight(DEFAULT_OFF_SCREEN_ANIMATION_DURATION, true)
            else -> resetCardPosition(DEFAULT_RESET_ANIMATION_DURATION)
        }
    }

    private fun resetCardPosition(duration: Int) {
        currentCardAnimator = ObjectAnimator.ofPropertyValuesHolder(cardView,
                PropertyValuesHolder.ofFloat(View.X, initialCardPositionX),
                PropertyValuesHolder.ofFloat(View.Y, initialCardPositionY),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f))
                .setDuration(duration.toLong())
        currentCardAnimator?.addUpdateListener { updateCardProgress() }
        currentCardAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isBeingDragged = false
            }
        })
        currentCardAnimator?.start()
    }

    private fun swipedCardOffScreen() {
        isSwipedOffScreen = true
        cardView.isEnabled = false
    }

    private fun updateCardProgress() {
        var sideProgress = (cardView.x + cardView.width / 2 - koloda.parentWidth / 2) / (koloda.parentWidth / 2)
        if (sideProgress > 1f) {
            sideProgress = 1f
        }
        if (sideProgress < -1f) {
            sideProgress = -1f
        }
        updateCardProgress(sideProgress)
    }

    private fun updateCardProgress(sideProgress: Float) {
        cardCallback.onCardDrag(adapterPosition, cardView, sideProgress)

        val cardOffsetProgress = Math.max(Math.abs(cardView.x / cardView.width),
                Math.abs(cardView.y / cardView.height))
        if (!(isSwipedOffScreen && cardOffsetProgress > 1)) {
            cardCallback.onCardOffset(adapterPosition, cardView, sideProgress)
        }
    }

    private fun cardBeyondLeftBorder(): Boolean {
        return cardView.x + cardView.width / 2 < koloda.parentWidth / 4f
    }

    private fun cardBeyondRightBorder(): Boolean {
        return cardView.x + cardView.width / 2 > koloda.parentWidth - koloda.parentWidth / 4f
    }

    private fun onTouchView(view: View, event: MotionEvent) : Boolean {
        if (!koloda.canSwipe(cardView)) {
            return false
        }
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {

                isBeingDragged = true
                cardCallback.onCardActionDown(adapterPosition, cardView)

                currentCardAnimator?.cancel()
                initialCardPositionX = cardView.x
                initialCardPositionY = cardView.y

                val pointerIndex = event.actionIndex
                initialTouchX = event.getX(pointerIndex)
                initialTouchY = event.getY(pointerIndex)
                activePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {

                val pointerIndex = event.findPointerIndex(activePointerId)

                val dx = event.getX(pointerIndex) - initialTouchX
                val dy = event.getY(pointerIndex) - initialTouchY

                val posX = cardView.x + dx
                val posY = cardView.y + dy

                cardView.x = posX
                cardView.y = posY

                updateCardProgress()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                activePointerId = MotionEvent.INVALID_POINTER_ID
                checkCardPosition()
                cardCallback.onCardActionUp(adapterPosition, cardView, (cardBeyondLeftBorder() || cardBeyondRightBorder()))
            }

            MotionEvent.ACTION_POINTER_UP -> {

                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)

                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    initialTouchX = event.getX(newPointerIndex)
                    initialTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

}