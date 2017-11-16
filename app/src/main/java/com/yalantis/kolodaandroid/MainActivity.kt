package com.yalantis.kolodaandroid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.yalantis.library.KolodaListener
import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.SuppressLint
import android.view.ViewTreeObserver


class MainActivity : AppCompatActivity() {

    private var adapter: KolodaSampleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeDeck()
        fillData()
    }

    override fun onStart() {
        super.onStart()
        activityMain.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @SuppressLint("NewApi")
            override fun onGlobalLayout() {
                //now we can retrieve the width and height
                val width = activityMain.width
                val height = activityMain.height
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
                Toast.makeText(this@MainActivity, "On new top card", Toast.LENGTH_LONG).show()
            }

            override fun onCardSwipedLeft(position: Int) {
                Toast.makeText(this@MainActivity, "On card swiped left", Toast.LENGTH_LONG).show()
            }

            override fun onCardSwipedRight(position: Int) {
                Toast.makeText(this@MainActivity, "On card swiped right", Toast.LENGTH_LONG).show()
            }

            override fun onEmptyDeck() {
                Toast.makeText(this@MainActivity, "On empty deck", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Fills deck with data
     */
    private fun fillData() {
        val data = arrayOf("https://source.unsplash.com/Xq1ntWruZQI/600x800",
                "https://source.unsplash.com/NYyCqdBOKwc/600x800",
                "https://source.unsplash.com/buF62ewDLcQ/600x800",
                "https://source.unsplash.com/THozNzxEP3g/600x800",
                "https://source.unsplash.com/USrZRcRS2Lw/600x800",
                "https://source.unsplash.com/PeFk7fzxTdk/600x800",
                "https://source.unsplash.com/LrMWHKqilUw/600x800")
        adapter = KolodaSampleAdapter(this, data.toList())
        koloda.adapter = adapter
    }
}
