package com.example.tsoonamiii

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat

//class MainActivity : AppCompatActivity() {
//    val LOG_TAG=MainActivity::class.java.simpleName
//    val USGS_REQUEST_URL=
//        "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-12-01&minmagnitude=7"
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        tsunamiAsyncTask().execute()
//    }
//    private inner class tsunamiAsyncTask : AsyncTask<URL,Void, Event>(){
//        override fun doInBackground(vararg params: URL?): Event? {
//            val url =createUrl(USGS_REQUEST_URL)
//            var jsonResponse=""
//            try{
//                jsonResponse=makeHttpRequest(url)
//            }catch (e: IOException){
//                 e.printStackTrace()
//            }
//            return extractFeatureFromJson(jsonResponse)
//        }
//
//        override fun onPostExecute(earthQuake: Event?) {
//            if(earthQuake==null)return
//            updateUri(earthQuake)
//        }
//        private fun createUrl(stringUrl:String):URL? {
//            return try{
//                URL(stringUrl)
//            }catch (ex: MalformedURLException){
//                Log.e(LOG_TAG, "Error with creating URL", ex)
//                null
//            }
//        }
//        private fun makeHttpRequest(url: URL?):String{
//            var jsonResponce=""
//            if(url==null)return jsonResponce
//            var urlConnection: HttpURLConnection? = null
//            var inputStream: InputStream? =null
//            try{
//                urlConnection= url.openConnection() as HttpURLConnection
//                urlConnection.requestMethod="GET"
//                urlConnection.connectTimeout=15000
//                urlConnection.readTimeout=10000
//                urlConnection.connect()
//                // if the request is successful ,then only we read the input stream and parse the response
//                // here we are using getResponseCode() method of urlConnection to get status code
//                if(urlConnection.responseCode==200) {
//                    inputStream = urlConnection.inputStream
//                    jsonResponce = readFromStream(inputStream)
//                }
//            }finally {
//                urlConnection?.disconnect()
//                inputStream?.close()
//            }
//            return jsonResponce
//        }
//        private fun readFromStream(inputStream: InputStream?):String{
//            val output=StringBuilder()
//            if(inputStream!=null){
//                val inputStreamReader=InputStreamReader(inputStream, Charset.forName("UTF-8"))
//                val reader=BufferedReader(inputStreamReader)
//                var line=reader.readLine()
//                while(line!=null){
//                    output.append(line)
//                    line=reader.readLine()
//                }
//            }
//            return output.toString()
//        }
//        //@RequiresApi(Build.VERSION_CODES.S)
//        private fun extractFeatureFromJson(earthQuakeJson:String):Event?{
//            if(TextUtils.isEmpty(earthQuakeJson)){
//                return null
//            }
//            return try{
//                val baseJsonResponse=JSONObject(earthQuakeJson)
//                val featureArray=baseJsonResponse.getJSONArray("features")
//                if(featureArray.length()>0){
//                    val firstFeature=featureArray.getJSONObject(0)
//                    val properties=firstFeature.getJSONObject("properties")
//
//                    val time=properties.optLong("time")
//                    val title=properties.optString("title")
//                    val tsunamiAlert=properties.optInt("tsunami")
//                    Event(title, time, tsunamiAlert)
//                }else{
//                    null
//                }
//            }catch (e: JSONException) {
//                Log.e(LOG_TAG, "problem parsing the earthquake JSON result", e)
//                null
//            }
//        }
//    }
//
//
//    private fun updateUri(earthQuake: Event) {
//        val titleTextView=findViewById<TextView>(R.id.title)
//        titleTextView.text=earthQuake.title
//        findViewById<TextView>(R.id.date).text= getDateString(earthQuake.time)
//        findViewById<TextView>(R.id.tsunami_alert).text=getTsunamiAlertString(earthQuake.tsunamiAlert)
//
//    }
//
//    private fun getTsunamiAlertString(tsunamiAlert: Int): String? {
//        return when(tsunamiAlert){
//            0-> getString(R.string.alert_no)
//            1->getString(R.string.alert_yes)
//            else-> getString(R.string.alert_not_available)
//        }
//    }
//
//    private fun getDateString(time: Long): CharSequence? {
//        return SimpleDateFormat("EEE, d, MMM yyyy 'at' HH:mm:ss z").format(time)
//
//    }
//}

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = MainActivity::class.java.simpleName
    private val USGS_REQUEST_URL =
        "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-12-01&minmagnitude=7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            try {
                val jsonResponse = makeHttpRequest(URL(USGS_REQUEST_URL))
                if (jsonResponse.isNotEmpty()) {
                    val earthquake = extractFeatureFromJson(jsonResponse)
                    earthquake?.let { updateUi(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun makeHttpRequest(url: URL): String {
        var jsonResponse = ""
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            withContext(Dispatchers.IO) {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection?.requestMethod = "GET"
                urlConnection?.connectTimeout = 15000
                urlConnection?.readTimeout = 10000
                urlConnection?.connect()

                if (urlConnection?.responseCode == 200) {
                    inputStream = urlConnection?.inputStream
                    jsonResponse = readFromStream(inputStream)
                }
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error making HTTP request", e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    private fun extractFeatureFromJson(earthquakeJson: String): Event? {
        if (TextUtils.isEmpty(earthquakeJson)) {
            return null
        }
        return try {
            val baseJsonResponse = JSONObject(earthquakeJson)
            val featureArray = baseJsonResponse.getJSONArray("features")
            if (featureArray.length() > 0) {
                val firstFeature = featureArray.getJSONObject(0)
                val properties = firstFeature.getJSONObject("properties")

                val time = properties.optLong("time")
                val title = properties.optString("title")
                val tsunamiAlert = properties.optInt("tsunami")
                Event(title, time, tsunamiAlert)
            } else {
                null
            }
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "problem parsing the earthquake JSON result", e)
            null
        }
    }

    private fun updateUi(earthquake: Event) {
        findViewById<TextView>(R.id.title).text = earthquake.title
        findViewById<TextView>(R.id.date).text = getDateString(earthquake.time)
        findViewById<TextView>(R.id.tsunami_alert).text = getTsunamiAlertString(earthquake.tsunamiAlert)
    }

    private fun getTsunamiAlertString(tsunamiAlert: Int): String {
        return when (tsunamiAlert) {
            0 -> getString(R.string.alert_no)
            1 -> getString(R.string.alert_yes)
            else -> getString(R.string.alert_not_available)
        }
    }

    private fun getDateString(time: Long): CharSequence {
        return SimpleDateFormat("EEE, d, MMM yyyy 'at' HH:mm:ss z").format(time)
    }
}
