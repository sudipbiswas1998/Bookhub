package com.example.bookhub.activity

import android.app.DownloadManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
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
        progressBar = findViewById(R.id.progressBar)
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
        jsonParams.put("book_Id", bookId)

        println(jsonParams)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val success = it.getBoolean("success")
                    println("success is $success")
                    if (success) {
                        val bookJsonObject = it.getJSONObject("book_data")
                        progressbarLayout.visibility = View.GONE

                        txtBookName.text = bookJsonObject.getString("name")
                        txtBookAuthor.text = bookJsonObject.getString("author")
                        txtBookPrice.text = bookJsonObject.getString("price")
                        txtBookRating.text = bookJsonObject.getString("rating")
                        txtBookDescription.text = bookJsonObject.getString("description")

                        Picasso.get().load(bookJsonObject.getString("image")).into(imgBookImage)
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
}