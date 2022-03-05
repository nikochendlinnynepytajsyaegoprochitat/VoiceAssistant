package com.example.voiceassistent

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.example.voiceassistent.Advice.adv
import retrofit2.http.GET
import com.example.voiceassistent.Advice
import com.example.voiceassistent.AdviceAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.voiceassistent.AdviceService
import com.example.voiceassistent.AI
import kotlin.Throws
import com.example.voiceassistent.NumberToString
import com.example.voiceassistent.ForecastToString
import android.os.AsyncTask
import com.example.voiceassistent.ParsingHtmlService
import com.example.voiceassistent.MainActivity
import android.annotation.SuppressLint
import android.database.sqlite.SQLiteOpenHelper
import com.example.voiceassistent.DBHelper
import android.database.sqlite.SQLiteDatabase
import com.example.voiceassistent.Forecast.Weather
import com.example.voiceassistent.Forecast
import com.example.voiceassistent.ForecastAPI
import com.example.voiceassistent.ForecastService
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.speech.tts.TextToSpeech
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistent.MessageListAdapter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.voiceassistent.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voiceassistent.MessageEntity
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.ContentValues
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import com.example.voiceassistent.MessageViewHolder
import android.widget.TextView
import com.example.voiceassistent.NumberAPI
import com.example.voiceassistent.NumberService
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.http.Query

interface NumberAPI {
    @GET("/json/convert/num2str")
    fun getNumber(@Query("num") number: String?): Call<Number?>?
}