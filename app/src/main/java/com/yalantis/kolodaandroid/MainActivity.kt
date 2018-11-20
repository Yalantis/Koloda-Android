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

    override fun onStart() {
        super.onStart()
        activityMain.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @SuppressLint("NewApi")
            override fun onGlobalLayout() {
                //now we can retrieve the width and height
                //...
                //do whatever you want with them
                //...
                //this is an important step not to keep receiving callbacks:
                //we should remove this listener
                //I use the function to remove it based on the api level!


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    activityMain.viewTreeObserver.removeOnGlobalLayoutListener(this)
                else
                    activityMain.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
    }

    /**
     * Initialize Deck and Adapter for filling Deck
     * Also implemented listener for caching requests
     */
    private fun initializeDeck() {
        koloda.kolodaListener = object : KolodaListener {

            internal var cardsSwiped = 0

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
            actionReload -> { koloda.reloadAdapterData() }
        }
        return super.onOptionsItemSelected(item)
    }
}
