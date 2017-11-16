package com.yalantis.library

import android.view.View

/**
 * This interface provides empty implementations of the methods.
 * Any custom listener that cares only about a subset of the methods of this listener can
 * simply implement the interface directly.
 */
interface KolodaListener {

    fun onNewTopCard(position: Int) {
        //TODO implement this method if you need
    }

    fun onCardDrag(position: Int, cardView: View, progress: Float) {
        //TODO implement this method if you need
    }

    fun onCardSwipedLeft(position: Int) {
        //TODO implement this method if you need
    }

    fun onCardSwipedRight(position: Int) {
        //TODO implement this method if you need
    }

    fun onCardSingleTap(position: Int) {
        //TODO implement this method if you need
    }

    fun onCardDoubleTap(position: Int) {
        //TODO implement this method if you need
    }

    fun onCardLongPress(position: Int) {
        //TODO implement this method if you need
    }

    fun onEmptyDeck() {
        //TODO implement this method if you need
    }

}