package com.yalantis.library

import android.view.View


/**
 * Created by anna on 11/10/17.
 */
interface CardCallback {

    fun onCardActionDown(adapterPosition: Int, card: View)

    fun onCardDrag(adapterPosition: Int, card: View, sideProgress: Float)

    fun onCardOffset(adapterPosition: Int, card: View, offsetProgress: Float)

    fun onCardActionUp(adapterPosition: Int, card: View, isCardRemove: Boolean)

    fun onCardSwipedLeft(adapterPosition: Int, card: View, notify: Boolean)

    fun onCardSwipedRight(adapterPosition: Int, card: View, notify: Boolean)

    fun onCardOffScreen(adapterPosition: Int, card: View)

    fun onCardSingleTap(adapterPosition: Int, card: View)

    fun onCardDoubleTap(adapterPosition: Int, card: View)

    fun onCardLongPress(adapterPosition: Int, card: View)

}