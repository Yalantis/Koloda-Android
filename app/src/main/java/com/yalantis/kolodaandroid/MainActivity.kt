package com.yalantis.kolodaandroid

import android.os.Bundle
import com.yalantis.library.KolodaListener
import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.kolodaandroid.R.id.actionReload
import com.yalantis.kolodaandroid.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initializeDeck()
        fillData()
        setUpCLickListeners()
    }

    override fun onStart() {
        super.onStart()
        binding.activityMain.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
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
                    binding.activityMain.viewTreeObserver.removeOnGlobalLayoutListener(this)
                else
                    binding.activityMain.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
    }

    /**
     * Initialize Deck and Adapter for filling Deck
     * Also implemented listener for caching requests
     */
    private fun initializeDeck() {
        binding.koloda.kolodaListener = object : KolodaListener {

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
        val adapter = KolodaSampleAdapter(this, data.toList())
        binding.koloda.adapter = adapter
        binding.koloda.isNeedCircleLoading = true
    }

    private fun setUpCLickListeners() {
        binding.dislike.setOnClickListener { binding.koloda.onClickLeft() }
        binding.like.setOnClickListener { binding.koloda.onClickRight() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_reload, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            actionReload -> { binding.koloda.reloadAdapterData() }
        }
        return super.onOptionsItemSelected(item)
    }
}
