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
import org.jsoup.parser.Parser
import java.io.IOException
import java.lang.StringBuilder
import java.util.ArrayList

object ParsingHtmlService {
    private const val URL = "http://mirkosmosa.ru/holiday/2021"
    private const val URL2 = "https://ru.investing.com/crypto/"
    @Throws(IOException::class)
    fun getHoliday(date: String): String {
        val sParts = date.split("[/.\\s+]").toTypedArray()
        val iParts = IntArray(sParts.size)
        for (i in sParts.indices) {
            iParts[i] = sParts[i].toInt()
        }
        val document = Jsoup.connect(URL).get()
        val div = document.select(
            "#holiday_calend > div:nth-child(" + iParts[1] + ")>div>div:nth-child("
                    + iParts[0] + ")" + " > div.month_cel_date + div.month_cel > ul.holiday_month_day_holiday > li > a[href]"
        )
        val str: MutableList<String> = ArrayList()
        for (e in div) {
            str.add(e.text())
        }
        return if (str.size != 0) {
            str.toString().replace("\\[|\\]".toRegex(), "").replace(",", "\n")
        } else {
            "Праздника нет"
        }
    }

    fun getCryptoCurrencyExchangeRate(): String? {
        val document = Jsoup.connect(URL2).get()
        val str = StringBuilder()
        val elements = document.select("tr[i]")
        for (element in elements) {
            str.append( /*element.getElementsByClass("left bold elp name cryptoName first js-currency-name")
                    .select("a[href]").text())
                    .append(" ")
                    .append*/
                """${
                    element.getElementsByClass("left noWrap elp symb js-currency-symbol").text()
                } - ${
                    element.getElementsByClass("price js-currency-price").select("a[href]").text()
                }$ изм (24ч) ${element.getElementsByClass("js-currency-change-24h").text()}
"""
            )
        }
        return str.toString()
    }

    fun getLastNews(): String? {
        val document = Jsoup.connect("").get()
        var out = ""
        val doc = Jsoup.parse(document.html(), "", Parser.xmlParser())
        var count = 0
        for (e in doc.select("title")) {
            out += """
                ${e.text()}
                
                """.trimIndent()
            count++
            if (count >= 5) return out
        }
        return out
    }
}