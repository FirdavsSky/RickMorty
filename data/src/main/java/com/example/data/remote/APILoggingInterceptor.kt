package com.example.data.remote

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.IOException
import java.util.Locale

class APILoggingInterceptor : Interceptor {
    private val TAG: String = "RickMorty.REST"

    @SuppressLint("LongLogTag")
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val time: Long = System.currentTimeMillis()
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        val rawJson: String?
        if (response.body?.contentType().toString().contains("json")) {
            rawJson = try {
                response.body?.string()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else rawJson = null

        Log.d(TAG, "-----------------------------START-------------------------------")
        Log.d(
            TAG, response.request.method + " " + response.request.url + " " + bodyToString(
                request
            )
        )
        Log.d(
            TAG, String.format(
                Locale.getDefault(), "Response by %dms: code:%s, body=%s", (System.currentTimeMillis() - time), response.code, rawJson
            )
        )
        Log.d(TAG, "------------------------------END-------------------------------")
        rawJson?.let {
            return response.newBuilder().body(ResponseBody.create(response.body?.contentType(), it)).build()
        }
        return response
    }

    private fun bodyToString(request: Request): String {
        try {
            val copy: Request = request.newBuilder().build();
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            return buffer.readUtf8()
        } catch (e: IOException) {
            e.message?.let {
                Log.e(TAG, it)
            }
            return ""
        }
    }
}