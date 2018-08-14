package com.yalantis.library

import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.os.Build
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Adapter
import android.widget.FrameLayout

/**
 * Created by anna on 11/10/17.
 */
class Koloda
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null,
            defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), CardLayout.CardParams {

    init {
        init(attrs)
    }

    private var maxVisibleCards = DEFAULT_MAX_VISIBLE_CARDS
    private var cardPositionOffsetY = resources.getDimensionPixelSize(R.dimen.default_card_spacing)
    private var cardRotationDegrees = DEFAULT_ROTATION_ANGLE
    private var scaleDiff = DEFAULT_SCALE_DIFF
    private var dyingViews = hashSetOf<View>()
    private var activeViews = linkedSetOf<View>()
    private var dataSetObservable: DataSetObserver? = null
    var isNeedCircleLoading = false
    @DrawableRes
    var rightOverlay: Int? = null
    @DrawableRes
    var leftOverlay: Int? = null
    var cardXPos = 0
    var cardYPos = 0
    var cardWidth = 0
    var cardHeight = 0
    private var alphaAnimation = false

    var adapter: Adapter? = null
        set(value) {
            dataSetObservable?.let { field?.unregisterDataSetObserver(it) }
            removeAllViews()
            field = value
            field?.registerDataSetObserver(createDataSetObserver())
            dataSetObservable?.onChanged()
        }

    private var adapterPosition = -1
    private var deckMap = hashMapOf<View, CardOperator>()
    var kolodaListener: KolodaListener? = null
    internal var parentWidth = 0
    private var swipeEnabled = true

    private fun init(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Koloda)
        val cardLayoutId = a.getResourceId(R.styleable.Koloda_koloda_card_layout, -1)
        maxVisibleCards = a.getInt(R.styleable.Koloda_koloda_max_visible_cards, DEFAULT_MAX_VISIBLE_CARDS)
        cardPositionOffsetY = a.getDimensionPixelSize(R.styleable.Koloda_koloda_card_offsetY, resources.getDimensionPixelSize(R.dimen.default_card_spacing))
        cardRotationDegrees = a.getFloat(R.styleable.Koloda_koloda_card_rotate_angle, DEFAULT_ROTATION_ANGLE)
        scaleDiff = a.getFloat(R.styleable.Koloda_koloda_card_scale_diff, DEFAULT_SCALE_DIFF)
        alphaAnimation = a.getBoolean(R.styleable.Koloda_koloda_card_animation_alpha, false)
        a.recycle()

        if (isInEditMode) {
            LayoutInflater.from(context).inflate(cardLayoutId, this, true)
        }
    }

    /**
     * Add card to visible stack
     */

    private fun addCardToDeck() {
        if (adapterPosition < adapter?.count?.minus(1) ?: 0) {

            val cardLayout = CardLayout(context, cardParams = this)

            initializeCardPosition(cardLayout)
            cardLayout.let {
                addView(it, 0)
                deckMap[it] = CardOperator(this, it, adapterPosition++, cardCallback)
                val newCard = adapter?.getView(adapterPosition, null, it)
                it.addOverlays(newCard, rightOverlay, leftOverlay)
            }
        } else if (isNeedCircleLoading) {
            adapterPosition = -1
            dataSetObservable?.onChanged()
        }
    }

    /**
     * Set up card offset
     * @param view - new card
     */
    private fun initializeCardPosition(view: View?) {
        val childCount = childCount - dyingViews.size

        scaleView(view, childCount = childCount)
        view?.translationY = (cardPositionOffsetY * childCount).toFloat()
        setZTranslations(childCount)
    }

    private fun checkTopCard() {
        val childCount = childCount
        setZTranslations(childCount)
        if (childCount - 1 - dyingViews.size < 0) {
            kolodaListener?.onEmptyDeck()
        } else {
            val topCard = getChildAt(childCount - 1 - dyingViews.size)
            if (deckMap.containsKey(topCard)) {
                deckMap[topCard]?.let { kolodaListener?.onNewTopCard(it.adapterPosition) }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setZTranslations(childCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (0 until childCount).map {
                getChildAt(it).translationZ = (it * 10).toFloat()
            }
        }
    }

    fun getMaxCardWidth(cardView: View): Float = cardView.height * Math.tan(Math
        .toRadians(cardRotationDegrees.toDouble())).toFloat()

    /**
     *  Checks capability of card view swiping
     *
     *  @param - card of Desk
     */
    fun canSwipe(card: View): Boolean {
        return (swipeEnabled && (activeViews.isEmpty() || activeViews.contains(card))
                && indexOfChild(card) >= childCount - 2)
    }

    private fun updateDeckCardsPosition(progress: Float) {
        val childCount = Math.min(childCount, maxVisibleCards)
        var cardsWillBeMoved = 0

        var cardView: View

        (0 until childCount).map {
            cardView = getChildAt(it)
            if (deckMap.containsKey(cardView) && deckMap[cardView]?.isBeingDragged != true) {
                cardsWillBeMoved++
            }
            scaleView(cardView, progress, childCount - it - 1)
        }
        if (progress != 0.0f) {
            for (i in 0 until cardsWillBeMoved) {
                cardView = getChildAt(i)
                cardView.translationY = (cardPositionOffsetY * Math
                    .min(cardsWillBeMoved, maxVisibleCards - i - 1) - cardPositionOffsetY * Math.abs(progress))
            }
        }
    }

    private fun scaleView(view: View?, progress: Float = 0f, childCount: Int) {
        val currentScale = 1f - (childCount * scaleDiff)
        val nextScale = 1f - ((childCount - 1) * scaleDiff)
        val scale = currentScale + (nextScale - currentScale) * Math.abs(progress)
        if (scale <= 1f) {
            view?.scaleX = scale
            view?.scaleY = scale
            if (alphaAnimation) {
                (view as CardLayout).layoutAlpha = scale
            }

        }
    }

    private fun updateCardView(card: View, sideProgress: Float) {
        val rotation = cardRotationDegrees.toInt() * sideProgress
        card.rotation = rotation
    }

    override fun onParamsMeasure(layoutParamsCardView: LayoutParams) {
        cardXPos = layoutParamsCardView.leftMargin
        cardYPos = layoutParamsCardView.topMargin + cardPositionOffsetY + cardPositionOffsetY
        cardWidth = layoutParamsCardView.width
        cardHeight = layoutParamsCardView.height
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            dataSetObservable?.onChanged()
        }
    }

    override fun addView(child: View, index: Int) {
        child.alpha = 0f
        super.addView(child, index)
        child.animate().alpha(1f).duration = 300
    }

    override fun removeAllViews() {
        super.removeAllViews()
        deckMap.clear()
        activeViews.clear()
        dyingViews.clear()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        parentWidth = (this.parent as View).measuredWidth
    }

    private fun createDataSetObserver(): DataSetObserver {
        dataSetObservable = object : DataSetObserver() {

            override fun onChanged() {
                super.onChanged()
                addCards()
            }

            override fun onInvalidated() {
                super.onInvalidated()
                adapterPosition = 0
                removeAllViews()
                addCards()
            }

            private fun addCards() {

                val childCount = childCount - dyingViews.size
                for (i in childCount until maxVisibleCards) {
                    addCardToDeck()
                }
                checkTopCard()
            }
        }
        return dataSetObservable as DataSetObserver
    }

    private var cardCallback: CardCallback = object : CardCallback {

        override fun onCardActionDown(adapterPosition: Int, card: View) {
            activeViews.add(card)
        }

        override fun onCardDrag(adapterPosition: Int, card: View, sideProgress: Float) {
            updateCardView(card, sideProgress)
            kolodaListener?.onCardDrag(adapterPosition, card, sideProgress)
        }

        override fun onCardOffset(adapterPosition: Int, card: View, offsetProgress: Float) {
            updateDeckCardsPosition(offsetProgress)
        }

        override fun onCardActionUp(adapterPosition: Int, card: View, isCardRemove: Boolean) {
            if (isCardRemove) {
                activeViews.remove(card)
            }
        }

        override fun onCardSwipedLeft(adapterPosition: Int, card: View, notify: Boolean) {
            dyingViews.add(card)
            dataSetObservable?.onChanged()
            if (notify) {
                kolodaListener?.onCardSwipedLeft(adapterPosition)
            }
        }

        override fun onCardSwipedRight(adapterPosition: Int, card: View, notify: Boolean) {
            dyingViews.add(card)
            dataSetObservable?.onChanged()
            if (notify) {
                kolodaListener?.onCardSwipedRight(adapterPosition)
            }
        }

        override fun onCardOffScreen(adapterPosition: Int, card: View) {
            dyingViews.remove(card)
            deckMap.remove(card)
            removeView(card)
        }

        override fun onCardSingleTap(adapterPosition: Int, card: View) {
            kolodaListener?.onCardSingleTap(adapterPosition)
        }

        override fun onCardDoubleTap(adapterPosition: Int, card: View) {
            kolodaListener?.onCardDoubleTap(adapterPosition)
        }

        override fun onCardLongPress(adapterPosition: Int, card: View) {
            kolodaListener?.onCardLongPress(adapterPosition)
        }

        override fun onCardMovedOnClickRight(adapterPosition: Int, card: View, notify: Boolean) {
            activeViews.remove(card)
            dyingViews.add(card)
            dataSetObservable?.onChanged()
            findPositionAfterClick()
            if (notify) {
                kolodaListener?.onClickRight(adapterPosition)
            }
        }

        override fun onCardMovedOnClickLeft(adapterPosition: Int, card: View, notify: Boolean) {
            activeViews.remove(card)
            dyingViews.add(card)
            dataSetObservable?.onChanged()
            findPositionAfterClick()
            if (notify) {
                kolodaListener?.onClickLeft(adapterPosition)
            }
        }
    }

    fun onClickRight() {
        onButtonClick(true)
    }

    fun onClickLeft() {
        onButtonClick(false)
    }

    /**
     *  Button right or left click remove card from desk
     *  @param isSwipeCardToRight - true for right click remove aimation false for left click
     *  remove animation
     */
    fun onButtonClick(isSwipeCardToRight: Boolean) {
        val childCount = childCount
        val topCard = getChildAt(childCount - 1 - dyingViews.size)
        topCard?.let {
            activeViews.add(it)
            val cardOperator: CardOperator? = deckMap[it]
            if (isSwipeCardToRight) {
                cardOperator?.onClickRight()
                it.rotation = 10f
            } else {
                cardOperator?.onClickLeft()
                it.rotation = -10f
            }
        }
    }

    private fun findPositionAfterClick() {
        val childCount = Math.min(childCount, maxVisibleCards)
        (0 until childCount).map {
            val view = getChildAt(it)
            scaleView(view, 0f, childCount - it - 1)
            view?.translationY = (cardPositionOffsetY * (childCount - it - 1)).toFloat()
        }
    }

    /**
     * Reload all data. Start show data from the beginning
     */
    fun reloadAdapterData() {
        removeAllViews()
        adapterPosition = 0
        dataSetObservable?.onChanged()
    }

    /**
     * Reload previous card after every call. If current card position in adapter == 0 this method do nothing
     */
    fun reloadPreviousCard() {
        if (isNeedCircleLoading) {
            var pos = adapterPosition - (DEFAULT_MAX_VISIBLE_CARDS)
            if (pos < 0)
                pos = adapter?.count?.plus(pos) ?: 0

            addCardOnTop(pos)

        } else {
            addCardOnTop(adapterPosition - (DEFAULT_MAX_VISIBLE_CARDS))
        }
    }

    private fun addCardOnTop(position: Int) {
        removeView(getChildAt(0))
        activeViews.clear()
        adapterPosition = position + 2
        adapter?.let {
            if (adapterPosition >= it.count) {
                adapterPosition -= it.count
            }
        }

        val cardLayout = CardLayout(context, cardParams = this)

        cardLayout.let {
            addView(it, Math.min(childCount, maxVisibleCards + 1))
            val newCard = adapter?.getView(position, null, it)
            deckMap[it] = CardOperator(this, it, position, cardCallback)
            it.addOverlays(newCard, rightOverlay, leftOverlay)
            animateWhenAddOnTop()
            dataSetObservable?.onChanged()
        }
    }

    private fun animateWhenAddOnTop() {
        val visibleChildCount = Math.min(childCount, maxVisibleCards + 1)
        val childCount = Math.min(childCount, maxVisibleCards)

        (0 until visibleChildCount).map {
            val view = getChildAt(it)
            val count = childCount - it - 1
            scaleView(view, childCount = count)
            view?.translationY = (cardPositionOffsetY * count).toFloat()
        }
    }

    companion object {
        private const val DEFAULT_MAX_VISIBLE_CARDS = 3
        private const val DEFAULT_ROTATION_ANGLE = 30f
        private const val DEFAULT_SCALE_DIFF = 0.04f
    }

}