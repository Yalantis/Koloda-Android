package com.yalantis.library.util

import android.animation.*
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.yalantis.library.CardLayout
import com.yalantis.library.Koloda
import com.yalantis.library.common.KolodaSwipeDirection

/**
 * Created by anna on 11/10/17.
 */
internal class CardOperator(
        val koloda: Koloda,
        val cardView: CardLayout,
        val adapterPosition: Int,
        val cardCallback: CardCallback
) {

    init {
        cardView.setOnTouchListener { view, event -> onTouchView(view, event) }
    }

    private val cardGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
        ): Boolean {
            return when (koloda.swipeDirection) {
                KolodaSwipeDirection.HORIZONTAL -> onFlingHorizontal(e1, e2)
                KolodaSwipeDirection.VERTICAL -> onFlingVertical(e1, e2)
            }
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            cardCallback.onCardDoubleTap(adapterPosition, cardView)
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            cardCallback.onCardSingleTap(adapterPosition, cardView)
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent?) {
            cardCallback.onCardLongPress(adapterPosition, cardView)
            super.onLongPress(e)
        }

        private fun onFlingHorizontal(e1: MotionEvent?,
                                      e2: MotionEvent?): Boolean {
            Log.e("===> ", "onFlingHorizontal work")
            if (e1?.x ?: 0f > e2?.x ?: 0f && cardBeyondLeftBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenLeft(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
                return true
            } else if (e1?.x ?: 0f < e2?.x ?: 0f && cardBeyondRightBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenRight(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
                return true
            }
            return false
        }

        private fun onFlingVertical(e1: MotionEvent?,
                                    e2: MotionEvent?): Boolean {
            if (e1?.y ?: 0f > e2?.y ?: 0f && cardBeyondTopBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenTop(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
                return true
            } else if (e1?.y ?: 0f < e2?.y ?: 0f && cardBeyondBottomBorder()) {
                cardCallback.onCardActionUp(adapterPosition, cardView, true)
                animateOffScreenBottom(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
                return true
            }
            return false
        }

    }

    private val gestureDetector = GestureDetector(cardView.context, cardGestureListener, null, true)
    private var isSwipedOffScreen = false
    private var currentCardAnimator: ObjectAnimator? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var activePointerId = 0
    internal var isBeingDragged: Boolean = false
    private var initialCardPositionX: Float = 0.toFloat()
    private var initialCardPositionY: Float = 0.toFloat()
    private var animationCycle: AnimationCycle = AnimationCycle.NO_ANIMATION
    private var velocityTracker: VelocityTracker? = null
    private val displayHeight = koloda.context.resources.displayMetrics.heightPixels

    fun animateOffScreenLeft(duration: Int, notify: Boolean, isClicked: Boolean) {

        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x - calculateTransition())
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, cardView.y * 2)
        animateCardOffScreen(duration, pvhX, pvhY)

        if (!isClicked) {
            cardCallback.onCardSwipedLeft(adapterPosition, cardView, notify)
        } else {
            cardCallback.onCardMovedOnClickLeft(adapterPosition, cardView, notify)
        }
    }

    fun animateOffScreenRight(duration: Int, notify: Boolean, isClicked: Boolean) {

        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x + calculateTransition())
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, cardView.y * 2)
        animateCardOffScreen(duration, pvhX, pvhY)

        if (!isClicked) {
            cardCallback.onCardSwipedRight(adapterPosition, cardView, notify)
        } else {
            cardCallback.onCardMovedOnClickRight(adapterPosition, cardView, notify)
        }
    }

    private fun animateOffScreenTop(duration: Int, notify: Boolean, isClicked: Boolean) {
        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x)
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, -cardView.height.toFloat())
        animateCardOffScreen(duration, pvhX, pvhY)
        if (!isClicked) {
            cardCallback.onCardSwipeUp(adapterPosition, cardView, notify)
        } else {
            cardCallback.onCardMovedOnClickUp(adapterPosition, cardView, notify)
        }
    }

    private fun animateOffScreenBottom(duration: Int, notify: Boolean, isClicked: Boolean) {
        val pvhX = PropertyValuesHolder.ofFloat("x", cardView.x, cardView.x)
        val pvhY = PropertyValuesHolder.ofFloat("y", cardView.y, displayHeight.toFloat())
        animateCardOffScreen(duration, pvhX, pvhY)
        if (!isClicked) {
            cardCallback.onCardSwipeDown(adapterPosition, cardView, notify)
        } else {
            cardCallback.onCardMovedOnClickDown(adapterPosition, cardView, notify)
        }
    }

    private fun calculateTransition(): Float {
        val maxCardWidth = koloda.getMaxCardWidth(cardView)
        return koloda.parentWidth - (koloda.parentWidth / 2 - cardView.width / 2) +
                cardView.x + Math.abs(maxCardWidth / 2)
    }

    private fun animateCardOffScreen(
            duration: Int,
            pvhX: PropertyValuesHolder,
            pvhY: PropertyValuesHolder
    ) {
        swipedCardOffScreen()

        val valueAnimator = ValueAnimator()
        valueAnimator.setValues(pvhX, pvhY)
        valueAnimator.addUpdateListener {
            cardView.translationX = it.getAnimatedValue("x") as Float - initialCardPositionX
            cardView.translationY = it.getAnimatedValue("y") as Float - initialCardPositionY
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
            koloda.swipeDirection == KolodaSwipeDirection.HORIZONTAL && cardBeyondLeftBorder() -> animateOffScreenLeft(
                    DEFAULT_OFF_SCREEN_ANIMATION_DURATION,
                    true,
                    false
            )
            koloda.swipeDirection == KolodaSwipeDirection.HORIZONTAL && cardBeyondRightBorder() -> animateOffScreenRight(
                    DEFAULT_OFF_SCREEN_ANIMATION_DURATION,
                    true,
                    false
            )
            koloda.swipeDirection == KolodaSwipeDirection.VERTICAL && cardBeyondTopBorder() -> {
                animateOffScreenTop(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
            }
            koloda.swipeDirection == KolodaSwipeDirection.VERTICAL && cardBeyondBottomBorder() -> {
                animateOffScreenBottom(DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION, notify = true, isClicked = false)
            }
            else -> resetCardPosition(DEFAULT_RESET_ANIMATION_DURATION)
        }
    }

    private fun resetCardPosition(duration: Int) {
        currentCardAnimator = ObjectAnimator.ofPropertyValuesHolder(
                cardView,
                PropertyValuesHolder.ofFloat(View.X, initialCardPositionX),
                PropertyValuesHolder.ofFloat(View.Y, initialCardPositionY),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f)
        )
                .setDuration(duration.toLong())
        currentCardAnimator?.duration = 200
        currentCardAnimator?.addUpdateListener { moveCard() }
        currentCardAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                animationCycle = AnimationCycle.ANIMATION_IN_PROGRESS
            }

            override fun onAnimationEnd(animation: Animator) {
                isBeingDragged = false
                animationCycle = AnimationCycle.NO_ANIMATION
                super.onAnimationEnd(animation)
            }
        })
        currentCardAnimator?.start()
    }

    private fun swipedCardOffScreen() {
        isSwipedOffScreen = true
        cardView.isEnabled = false
    }

    private fun updateCardProgress(sideProgress: Float) {
        cardCallback.onCardDrag(adapterPosition, cardView, sideProgress)
        changeOverlayAlpha(sideProgress)
        if (!(isSwipedOffScreen && getCardOffsetProgress() > 1)) {
            cardCallback.onCardOffset(adapterPosition, cardView, sideProgress)
        }
    }

    private fun getHorizontalSideProgress(): Float {
        var sideProgress =
                ((cardView.x + (cardView.width / 2)) - (koloda.parentWidth / 2)) / (koloda.parentWidth / 2)
        if (sideProgress > 1f) {
            sideProgress = 1f
        } else if (sideProgress < -1f) {
            sideProgress = -1f
        }
        return sideProgress
    }

    private fun changeOverlayAlpha(sideProgress: Float) {
        if (sideProgress > 0) {
            cardView.changeRightOverlayAlpha(sideProgress)
        } else {
            cardView.changeLeftOverlayAlpha(sideProgress)
        }
    }

    private fun getCardOffsetProgress(): Float = Math.max(
            Math.abs(cardView.x / cardView.width),
            Math.abs(cardView.y / cardView.height)
    )

    private fun getVerticalSideProgress(): Float {
        var sideProgress = (initialCardPositionY - cardView.y) / (cardView.height / 2)
        if (sideProgress > 1f) {
            sideProgress = 1f
        } else if (sideProgress < -1f) {
            sideProgress = -1f
        }
        return sideProgress
    }

    private fun cardBeyondLeftBorder(): Boolean {
        return cardView.x + cardView.width / 2 < koloda.parentWidth / 4f
    }

    private fun cardBeyondRightBorder(): Boolean {
        return cardView.x + cardView.width / 2 > koloda.parentWidth - koloda.parentWidth / 4f
    }

    private fun cardBeyondTopBorder(): Boolean {
        return initialCardPositionY - cardView.y > cardView.height / 6f
    }

    private fun cardBeyondBottomBorder(): Boolean {
        return cardView.y + cardView.height >= initialCardPositionY + cardView.height + (cardView.height / 6f)
    }

    private fun onTouchView(view: View, event: MotionEvent): Boolean {
        if (!koloda.canSwipe(cardView)) {
            return false
        }
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDownEventWork(event)
            MotionEvent.ACTION_MOVE -> onActionMoveEventWork(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onActionUpOrCancelEventWork()
            MotionEvent.ACTION_POINTER_UP -> onActionPointerUpEventWork(event)
        }
        return true
    }

    private fun onActionDownEventWork(event: MotionEvent) {
        updateVelocityTracker()
        if (animationCycle == AnimationCycle.NO_ANIMATION && !isBeingDragged) {
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
    }

    private fun updateVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
    }

    private fun onActionMoveEventWork(event: MotionEvent) {
        velocityTracker?.addMovement(event)

        val pointerId = event.getPointerId(event.actionIndex)
        velocityTracker?.computeCurrentVelocity(pointerId, 40f)

        val pointerIndex = event.findPointerIndex(activePointerId)

        if (pointerIndex != -1) {
            val dx = event.getX(pointerIndex) - initialTouchX
            val dy = event.getY(pointerIndex) - initialTouchY

            velocityTracker?.let {
                val posX = (cardView.x + dx) + Math.abs(it.getXVelocity(pointerId))
                val posY = (cardView.y + dy) + Math.abs(it.getYVelocity(pointerId))

                cardView.x = posX
                cardView.y = posY
            }

            moveCard()
        }
    }

    private fun moveCard() {
        val cardProgress = when (koloda.swipeDirection) {
            KolodaSwipeDirection.HORIZONTAL -> getHorizontalSideProgress()
            KolodaSwipeDirection.VERTICAL -> getVerticalSideProgress()
        }
        updateCardProgress(cardProgress)
    }

    private fun onActionUpOrCancelEventWork() {
        activePointerId = MotionEvent.INVALID_POINTER_ID
        checkCardPosition()
        cardCallback.onCardActionUp(adapterPosition, cardView, isCardRemoved())
    }

    private fun isCardRemoved(): Boolean = when (koloda.swipeDirection) {
        KolodaSwipeDirection.HORIZONTAL -> (cardBeyondLeftBorder() || cardBeyondRightBorder())
        KolodaSwipeDirection.VERTICAL -> (cardBeyondTopBorder() || cardBeyondBottomBorder())
    }

    private fun onActionPointerUpEventWork(event: MotionEvent) {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        if (pointerId == activePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            initialTouchX = event.getX(newPointerIndex)
            initialTouchY = event.getY(newPointerIndex)
            activePointerId = event.getPointerId(newPointerIndex)
        }
    }

    internal fun onClickRight() {
        initialCardPositionX = cardView.x
        initialCardPositionY = cardView.y
        animateOffScreenRight(DEFAULT_OFF_SCREEN_ANIMATION_DURATION, true, true)
    }

    internal fun onClickLeft() {
        initialCardPositionX = cardView.x
        initialCardPositionY = cardView.y
        animateOffScreenLeft(DEFAULT_OFF_SCREEN_ANIMATION_DURATION, true, true)
    }

    companion object {
        private const val DEFAULT_OFF_SCREEN_ANIMATION_DURATION = 600
        private const val DEFAULT_OFF_SCREEN_FLING_ANIMATION_DURATION = 150
        private const val DEFAULT_RESET_ANIMATION_DURATION = 600
    }

}
