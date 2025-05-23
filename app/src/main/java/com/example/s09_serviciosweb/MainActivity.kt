package com.example.s09_serviciosweb

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException

// Modelo de datos para Retrofit
data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

// Interfaz Retrofit
interface ApiService {
    @GET("todos/{id}")
    fun getTodo(@Path("id") id: Int): Call<Todo>
}

class MainActivity : ComponentActivity() {

    private lateinit var tvResult: TextView
    private val TAG = "TresClientesHTTP"

    // OkHttp client global
    private val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRetrofit = findViewById<Button>(R.id.btnRetrofit)
        val btnVolley = findViewById<Button>(R.id.btnVolley)
        val btnOkHttp = findViewById<Button>(R.id.btnOkHttp)
        tvResult = findViewById(R.id.tvResult)

        btnRetrofit.setOnClickListener { consumirConRetrofit() }
        btnVolley.setOnClickListener { consumirConVolley() }
        btnOkHttp.setOnClickListener { consumirConOkHttp() }
    }

    private fun consumirConRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.getTodo(1)

        call.enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful && response.body() != null) {
                    val todo = response.body()!!
                    val texto = "Retrofit:\nTitle: ${todo.title}\nCompleted: ${todo.completed}"
                    Log.d(TAG, texto)
                    runOnUiThread { tvResult.text = texto }
                } else {
                    runOnUiThread { tvResult.text = "Retrofit: Error en respuesta" }
                }
            }

            override fun onFailure(call: Call<Todo>, t: Throwable) {
                runOnUiThread { tvResult.text = "Retrofit: Error - ${t.message}" }
            }
        })
    }

    private fun consumirConVolley() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://jsonplaceholder.typicode.com/todos/1"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val title = response.getString("title")
                    val completed = response.getBoolean("completed")
                    val texto = "Volley:\nTitle: $title\nCompleted: $completed"
                    Log.d(TAG, texto)
                    tvResult.text = texto
                } catch (e: JSONException) {
                    tvResult.text = "Volley: Error parseando JSON"
                }
            },
            { error ->
                tvResult.text = "Volley: Error - ${error.message}"
            })

        queue.add(jsonObjectRequest)
    }

    private fun consumirConOkHttp() {
        val request = okhttp3.Request.Builder()
            .url("https://jsonplaceholder.typicode.com/todos/1")
            .build()

        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    tvResult.text = "OkHttp: Error - ${e.message}"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val texto = "OkHttp:\n$response\nBody: $body"
                    Log.d(TAG, texto)
                    runOnUiThread { tvResult.text = texto }
                } else {
                    runOnUiThread { tvResult.text = "OkHttp: Error en respuesta" }
                }
            }
        })

    }
}