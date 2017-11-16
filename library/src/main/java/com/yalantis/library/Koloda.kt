package com.yalantis.library

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.widget.FrameLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.Adapter
import android.os.Build
import android.annotation.TargetApi
import android.util.Log

/**
 * Created by anna on 11/10/17.
 */
class Koloda : FrameLayout {

    companion object {
        private const val DEFAULT_MAX_VISIBLE_CARDS = 3
        private const val DEFAULT_ROTATION_ANGLE = 30f
        private const val DEFAULT_SCALE_DIFF = 0.04f
    }

    private var maxVisibleCards = DEFAULT_MAX_VISIBLE_CARDS
    private var cardPositionOffsetX = resources.getDimensionPixelSize(R.dimen.default_card_spacing)
    private var cardPositionOffsetY = resources.getDimensionPixelSize(R.dimen.default_card_spacing)
    private var cardRotationDegrees = DEFAULT_ROTATION_ANGLE
    private var dyingViews = hashSetOf<View>()
    private var activeViews = linkedSetOf<View>()
    private var dataSetObservable: DataSetObserver? = null
    var adapter: Adapter? = null
        set(value) {
            dataSetObservable?.let { field?.unregisterDataSetObserver(it) }
            removeAllViews()
            field = value
            field?.registerDataSetObserver(createDataSetObserver())
            dataSetObservable?.onChanged()
        }
    private var adapterPosition = 0
    private var deckMap = hashMapOf<View, CardOperator>()
    var kolodaListener: KolodaListener? = null
    internal var parentWidth = 0
    private var swipeEnabled = true

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Koloda)
        val cardLayoutId = a.getResourceId(R.styleable.Koloda_koloda_card_layout, -1)
        maxVisibleCards = a.getInt(R.styleable.Koloda_koloda_max_visible_cards, DEFAULT_MAX_VISIBLE_CARDS)
        cardPositionOffsetX = a.getDimensionPixelSize(R.styleable.Koloda_koloda_card_offsetX, resources.getDimensionPixelSize(R.dimen.default_card_spacing))
        cardPositionOffsetY = a.getDimensionPixelSize(R.styleable.Koloda_koloda_card_offsetY, resources.getDimensionPixelSize(R.dimen.default_card_spacing))
        cardRotationDegrees = a.getFloat(R.styleable.Koloda_koloda_card_rotate_angle, DEFAULT_ROTATION_ANGLE)
        a.recycle()

        if (isInEditMode) {
            LayoutInflater.from(context).inflate(cardLayoutId, this, true)
        }
    }

    /**
     * Add card to visible stack
     */

    private fun addCardToDeck() {
        if (adapterPosition < adapter?.count ?: 0) {
            val newCard = adapter?.getView(adapterPosition, null, this)
            initializeCardPosition(newCard)
            newCard?.let {
                addView(it, 0)
                deckMap.put(it, CardOperator(this, it, adapterPosition++, cardCallback))
            }
        }
    }

    /**
     * Set up card offset
     * @param view - new card
     */
    private fun initializeCardPosition(view: View?) {
        val childCount = childCount - dyingViews.size

        scaleView(view, 0f, childCount)
        view?.translationY = (cardPositionOffsetY * childCount).toFloat()
        Log.i("initializeCardPosition", view?.translationY.toString())
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

    fun getMaxCardWidth(cardView: View): Float = cardView.height * Math.tan(Math.toRadians(cardRotationDegrees.toDouble())).toFloat()

    fun canSwipe(card: View): Boolean {
        return (swipeEnabled && (activeViews.isEmpty() || activeViews.contains(card))
                && indexOfChild(card) >= childCount - 2)
    }

    private fun updateDeckCardsPosition(progress: Float) {
        val childCount = Math.min(childCount, maxVisibleCards + 1)
        var cardsWillBeMoved = 0

        var cardView: View
        (0 until childCount).map {
            cardView = getChildAt(it)
            if (deckMap.containsKey(cardView) && !(deckMap[cardView]?.isBeingDragged ?: false)) {
                cardsWillBeMoved++
            }
            scaleView(cardView, progress, DEFAULT_MAX_VISIBLE_CARDS - it)
        }
        for (i in 0 until cardsWillBeMoved) {
            cardView = getChildAt(i)
            cardView.translationY = (cardPositionOffsetY * (DEFAULT_MAX_VISIBLE_CARDS - i + 1)).toFloat()
        }
    }

    private fun scaleView(view: View?, progress: Float, childCount: Int) {
        Log.i("I ", childCount.toString())
        val currentScale = 1f - (childCount * DEFAULT_SCALE_DIFF)
        val nextScale = 1f - ((childCount - 1) * DEFAULT_SCALE_DIFF)
        val scale = currentScale + (nextScale - currentScale) * Math.abs(progress)
        Log.i("Scale", scale.toString())
        if (scale <= 1f) {
            view?.scaleX = scale
            view?.scaleY = scale
        }
    }

    private fun updateCardView(card: View, sideProgress: Float) {
        val rotation = cardRotationDegrees.toInt() * sideProgress
        card.rotation = rotation
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

        override fun onCardActionUp(adapterPosition: Int, card: View, isCardNeedRemove: Boolean) {
            Log.i("onCardActionUp", " card delete")
            if (isCardNeedRemove) {
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
    }

}