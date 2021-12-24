package com.example.bookhub.activity

import android.app.DownloadManager
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.example.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDescription: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressbarLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar

    var bookId: String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDescription = findViewById(R.id.txtBookDescription)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressbar)
        progressBar.visibility = View.VISIBLE
        progressbarLayout = findViewById(R.id.progressbarLayout)
        progressbarLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"


        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(this, "Some unexpected error occurred!!!", Toast.LENGTH_SHORT).show()
        }
        if (bookId == "100") {
            finish()
            Toast.makeText(this, "Some unexpected error occurred!!!", Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)

        println(jsonParams)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    println("Response $it")
                    val success = it.getBoolean("success")
                    println("success is $success")
                    if (success) {
                        val bookJsonObject = it.getJSONObject("book_data")

                        progressbarLayout.visibility = View.GONE
                        val bookImageUrl = bookJsonObject.getString("image")
                        txtBookName.text = bookJsonObject.getString("name")
                        txtBookAuthor.text = bookJsonObject.getString("author")
                        txtBookPrice.text = bookJsonObject.getString("price")
                        txtBookRating.text = bookJsonObject.getString("rating")
                        txtBookDescription.text = bookJsonObject.getString("description")

                        Picasso.get().load(bookJsonObject.getString("image")).into(imgBookImage)

                        val bookEntity = BookEntity(
                            bookId?.toInt() as Int,
                            txtBookName.text.toString(),
                            txtBookAuthor.text.toString(),
                            txtBookPrice.text.toString(),
                            txtBookRating.text.toString(),
                            txtBookDescription.text.toString(),
                            bookImageUrl
                        )
                        val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                        val isFav = checkFav.get()
                        if (isFav) {
                            btnAddToFav.text = "Remove from favourites"
                            val favColor = ContextCompat.getColor(applicationContext, R.color.purple_700)
                            btnAddToFav.setBackgroundColor(favColor)
                        } else {
                            btnAddToFav.text = "Add to favourites"
                            val noFavColor = ContextCompat.getColor(applicationContext, R.color.primary)
                            btnAddToFav.setBackgroundColor(noFavColor)
                        }
                        btnAddToFav.setOnClickListener {
                            if (!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()) {
                                val async = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                val result = async.get()
                                if (result) {
                                    Toast.makeText(
                                        this,
                                        "Book added to favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    btnAddToFav.text = "Remove from favourites"
                                    val favColor = ContextCompat.getColor(applicationContext, R.color.purple_700)
                                    btnAddToFav.setBackgroundColor(
                                        favColor
                                    )
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Some error occurred!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                val async = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                val result = async.get()
                                if (result) {
                                    Toast.makeText(
                                        this,
                                        "Book removed from favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    btnAddToFav.text = "Add to favourites"
                                    val noFavColor = ContextCompat.getColor(applicationContext, R.color.primary)
                                    btnAddToFav.setBackgroundColor(
                                        noFavColor
                                    )
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Some error occurred!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Some error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: JSONException) {
                    Toast.makeText(
                        this,
                        "Some unexpected error occurred!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(
                    this,
                    "Volley Error",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "c6becc63c9d05d"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }

    class DBAsyncTask(context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()
        override fun doInBackground(vararg p0: Void?): Boolean {
            when (mode) {
                1 -> {
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }
}