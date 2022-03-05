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
import androidx.core.util.Consumer
import com.example.voiceassistent.NumberAPI
import com.example.voiceassistent.NumberService
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object AI {
    var time = "!"
    var ans = ""
    var `is` = false
    var date = Date()
    var currentDate = Date()
    var QuestionAndAnswers: MutableMap<Array<String>, Array<String>> = HashMap()
    fun FillMap() {
        QuestionAndAnswers[arrayOf("здрав", "hi", "hello", "привет", "привки")] = arrayOf("Привет", "Здравствуйте")
        QuestionAndAnswers[arrayOf("занимаеш", "делае")] = arrayOf("ожидаю", "отвечаю на вопросы")
        QuestionAndAnswers[arrayOf("дела", "как вы")] = arrayOf("отлично", "нормально")
    }

    var events: MutableMap<String, Date> = HashMap()
    var callbacksQueue: Queue<String?> = LinkedList()
    var year: Int? = null
    var month: Int? = null
    var day: Int? = null
    @Throws(ParseException::class)
    fun getAnswer(question: String, callback: Consumer<String?>) {
        var question = question
        question = question.lowercase(Locale.getDefault())
        val random = Random()
        FillMap()
        for ((key1, value) in QuestionAndAnswers) {
            for (key in key1) {
                if (question.contains(key)) {
                    callback.accept(value[random.nextInt(value.size)])
                    return
                }
            }
        }
        if (question.contains("сколько дней до")) {
            val event = question.split(" ").toTypedArray()[question.split(" ").toTypedArray().size - 1]
            for ((key, value) in events) {
                if (event.contains(key)) {
                    val diffInMillies = value.time - Date().time
                    val diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
                    val day: String
                    if (diff < 0) {
                        callback.accept(
                            "$diff событие состоялось " + SimpleDateFormat("dd.MM.yyyy").format(
                                value
                            )
                        )
                        return
                    }
                    day = if (diff % 10 == 1L) {
                        "день"
                    } else {
                        if (diff % 10 > 1 && diff % 10 < 5) {
                            "дня"
                        } else {
                            "дней"
                        }
                    }
                    callback.accept(
                        """$diff $day
today: ${SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().time)}
date of event: ${SimpleDateFormat("dd.MM.yyyy").format(value)}"""
                    )
                    return
                }
            }
            callback.accept("Событие не обнаружено, чтобы добавить событие введите команду: добавить событие <название> <дата(dd.mm.yy)>")
            return
        }
        if (question.contains("добавить событие")) {
            val event = question.split(" ").toTypedArray()[question.split(" ").toTypedArray().size - 2]
            val dateString = question.split(" ").toTypedArray()[question.split(" ").toTypedArray().size - 1]
            val format: DateFormat = SimpleDateFormat("dd.MM.yy", Locale.ROOT)
            val date = format.parse(dateString)
            events[event] = date
            callback.accept("Событие добавлено")
            return
        }
        if (question.contains("который час") || question.contains("время")) {
            val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeText = timeFormat.format(currentDate)
            ans = timeText
            callback.accept(ans)
            return
        }
        val c = Calendar.getInstance()
        c.firstDayOfWeek = 2
        c.time = currentDate
        val dayOfWeek = c[Calendar.DAY_OF_WEEK]
        if (question.contains("какой день недели")) {
            val strDays = arrayOf("Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
            callback.accept(strDays[dayOfWeek - 1])
            return
        }
        if (question.contains("какой сегодня день")) {
            ans = Integer.toString(dayOfWeek - 1)
            val date1 = Date()
            callback.accept(ans + " " + date1.toString())
            return
        }
        if (question.contains("создатель")) {
            callback.accept("Andrew")
            return
        }
        val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        if (question.contains("в строку")) {
            val numberPattern = Pattern.compile("(\\p{Digit}+) в строку", Pattern.CASE_INSENSITIVE)
            val matcher = numberPattern.matcher(question)
            if (matcher.find()) {
                val number = matcher.group(1)
                NumberToString.getNumber(number) { s: String? ->
                    if (s != null) callback.accept(s) else callback.accept(
                        "Нельзя"
                    )
                }
                return
            }
        }
        if (question.contains("погода в городе")) {
            try {
                val cityPattern = Pattern.compile("погода в городе (\\p{L}+)", Pattern.CASE_INSENSITIVE)
                val matcher = cityPattern.matcher(question)
                if (matcher.find()) {
                    val cityName = matcher.group(1)
                    ForecastToString.getForecast(cityName) { s: String? ->
                        if (s != null) callback.accept(s) else callback.accept(
                            "Нет такого города"
                        )
                    }
                    return
                }
            } catch (e: Exception) {
                callback.accept("Не получается узнать :(")
                return
            }
        }
        if (question.contains("крипто")) {
            object : AsyncTask<String?, Int?, Void?>() {
                protected override fun doInBackground(vararg params: String?): Void? {
                    try {
                        val message = ParsingHtmlService.getCryptoCurrencyExchangeRate()
                        callbacksQueue.add(message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    callback.accept(callbacksQueue.poll().toString())
                }
            }.execute()
            return
        }
        if (question.contains("праздн")) {
            val date = getDate(question)
            object : AsyncTask<String?, Int?, Void?>() {
                protected override fun doInBackground(vararg strings: String?): Void? {
                    try {
                        val message = ParsingHtmlService.getHoliday(date)
                        callbacksQueue.add(message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    callback.accept(callbacksQueue.poll().toString())
                }
            }.execute(*date.split(",").toTypedArray())
            return
        }
        if (question.contains("новос")) {
            object : AsyncTask<String?, Int?, Void?>() {
                protected override fun doInBackground(vararg strings: String?): Void? {
                    try {
                        val message = ParsingHtmlService.getLastNews()
                        callbacksQueue.add(message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    callback.accept(callbacksQueue.poll().toString())
                }
            }.execute()
            return
        }
        if (question.contains("включить голос")) {
            MainActivity.Companion.isSpeach = true
            callback.accept("озвучка ответов включена")
            return
        }
        if (question.contains("отключить голос")) {
            MainActivity.Companion.isSpeach = false
            callback.accept("озвучка ответов отключена")
            return
        }
        val commands = arrayOf(
            "привет",
            "как дела",
            "чем занимаешься",
            "какой сегодня день",
            "который час",
            "время",
            "какой день недели",
            "создатель",
            "<число> в строку",
            "сколько дней до <событие>",
            "добавить событие <название> <дата(dd.mm.yy)> ",
            "погода в городе <город>",
            "праздник вчера/сегодня/завтра",
            "праздник <дата(dd.mm.yy)>",
            "курс крипто",
            "новости",
            "включить голос",
            "отключить голос"
        )
        callback.accept(
            """
                не знаю ответ на этот вопрос.
                доступные команды:
                ${java.lang.String.join("\n", *commands)}
                """.trimIndent()
        )
    }

    private fun timeToDate(date: String) {
        var date = date
        val newDate: Array<String>
        var temp = date.replace("сколько дней до ", "")
        temp = temp.replace(" ", "")
        temp = temp.replace("?", "")
        newDate = temp.split("\\.").toTypedArray()
        val calendar1: Calendar = GregorianCalendar()
        val calendar2: Calendar = GregorianCalendar(newDate[2].toInt(), newDate[1].toInt() - 1, newDate[0].toInt())
        val temp1 = calendar1.timeInMillis
        val temp2 = calendar2.timeInMillis
        val timeLeft: Long
        if (calendar1.before(calendar2)) {
            timeLeft = Math.abs(temp2 - temp1)
            TimeUnit.MILLISECONDS.toDays(timeLeft)
            time = (TimeUnit.MILLISECONDS.toDays(timeLeft) + 1).toString()
        } else time = "0"
        fll = 0
        fll2 = false
        j = 0
        date = ""
    }

    var fll = 0
    var addDateOrNo = ""
    lateinit var str: Array<String>
    var strr = ""
    var j = 0
    var k = 0
    var fll2 = false

    @SuppressLint("SimpleDateFormat") // @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(ParseException::class)
    fun getDate(text: String): String {
        val ldt: LocalDateTime
        val format1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
        val dayFormat = SimpleDateFormat("dd/MM/YYYY")
        return if (text.contains("вчера")) {
            ldt = LocalDateTime.now().minusDays(1)
            format1.format(ldt)
        } else {
            if (text.contains("завтра")) {
                ldt = LocalDateTime.now().plusDays(1)
                format1.format(ldt)
            } else {
                if (text.contains("сегодня")) dayFormat.format(Date()) else {
                    val formatter = SimpleDateFormat("праздник dd.MM.yyyy")
                    val date = formatter.parse(text)
                    dayFormat.format(date)
                }
            }
        }
    }

    fun getdate(date1: String): Calendar {
        val str = date1.split(" ").toTypedArray()
        year = str[str.size - 1].toInt()
        month = str[str.size - 2].toInt()
        day = str[str.size - 3].toInt()
        val t2 = Calendar.getInstance()
        t2[year!!, month!! - 1] = day!!
        return t2
    }
}