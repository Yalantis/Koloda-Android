package com.yalantis.kolodaandroid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.yalantis.library.KolodaListener
import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import com.yalantis.kolodaandroid.R.id.actionReload


class MainActivity : AppCompatActivity() {

    private var adapter: KolodaSampleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeDeck()
        fillData()
        setUpCLickListeners()
    }

    /**
     * Initialize Deck and Adapter for filling Deck
     * Also implemented listener for caching requests
     */
    private fun initializeDeck() {
        koloda.kolodaListener = object : KolodaListener {

            override fun onNewTopCard(position: Int) {
                //todo realize your logic
            }

            override fun onCardSwipedLeft(position: Int) {
                //todo realize your logic
            }

            override fun onCardSwipedRight(position: Int) {
                //todo realize your logic
            }

            override fun onEmptyDeck() {
                //todo realize your logic
            }
        }
    }

    /**
     * Fills deck with data
     */
    private fun fillData() {
        val data = arrayOf(R.drawable.cupcacke,
            R.drawable.donut,
            R.drawable.eclair,
            R.drawable.froyo,
            R.drawable.gingerbread,
            R.drawable.honeycomb,
            R.drawable.ice_cream_sandwich,
            R.drawable.jelly_bean,
            R.drawable.kitkat,
            R.drawable.lollipop,
            R.drawable.marshmallow,
            R.drawable.nougat,
            R.drawable.oreo)
        adapter = KolodaSampleAdapter(this, data.toList())
        koloda.adapter = adapter
        koloda.isNeedCircleLoading = true
    }

    private fun setUpCLickListeners() {
        dislike.setOnClickListener { koloda.onClickLeft() }
        like.setOnClickListener { koloda.onClickRight() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_reload, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            actionReload -> { koloda.reloadPreviousCard() }
        }
        return super.onOptionsItemSelected(item)
    }
}
