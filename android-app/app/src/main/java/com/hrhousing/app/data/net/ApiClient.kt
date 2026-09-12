package com.hrhousing.app.data.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Minimal REST client for the web companion's API (see web/server.js). Deliberately hand-rolled
 * with `java.net.HttpURLConnection` + `org.json` — both already part of the Android/JVM standard
 * library — so talking to the server needed zero new Gradle dependencies.
 *
 * Every call is a plain suspend function; callers decide what "offline" means for their own
 * repository (usually: catch the exception and fall back to the Room cache).
 */
object ApiClient {
    private const val TIMEOUT_MS = 15_000

    class ApiException(message: String) : IOException(message)

    private fun normalizeBase(baseUrl: String): String = baseUrl.trimEnd('/')

    private suspend fun request(
        baseUrl: String,
        path: String,
        method: String,
        jsonBody: String? = null,
        rawBody: ByteArray? = null,
        extraHeaders: Map<String, String> = emptyMap(),
    ): String = withContext(Dispatchers.IO) {
        val url = URL("${normalizeBase(baseUrl)}$path")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doInput = true
            if (jsonBody != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                writeBody(connection.outputStream, jsonBody.toByteArray(StandardCharsets.UTF_8))
            } else if (rawBody != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                writeBody(connection.outputStream, rawBody)
            }
            for ((key, value) in extraHeaders) connection.setRequestProperty(key, value)

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
            if (code !in 200..299) throw ApiException("HTTP $code: $text")
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun writeBody(out: OutputStream, bytes: ByteArray) {
        out.use { it.write(bytes) }
    }

    suspend fun getArray(baseUrl: String, path: String): JSONArray = JSONArray(request(baseUrl, path, "GET"))
    suspend fun getObject(baseUrl: String, path: String): JSONObject = JSONObject(request(baseUrl, path, "GET"))

    suspend fun postObject(baseUrl: String, path: String, body: JSONObject): JSONObject =
        JSONObject(request(baseUrl, path, "POST", jsonBody = body.toString()))

    suspend fun putObject(baseUrl: String, path: String, body: JSONObject): JSONObject =
        JSONObject(request(baseUrl, path, "PUT", jsonBody = body.toString()))

    suspend fun delete(baseUrl: String, path: String) {
        request(baseUrl, path, "DELETE")
    }

    suspend fun post(baseUrl: String, path: String) {
        request(baseUrl, path, "POST", jsonBody = "{}")
    }

    suspend fun postForArray(baseUrl: String, path: String): JSONArray =
        JSONArray(request(baseUrl, path, "POST", jsonBody = "{}"))

    suspend fun uploadFile(baseUrl: String, fileName: String, bytes: ByteArray): JSONObject {
        val encodedName = java.net.URLEncoder.encode(fileName, "UTF-8")
        return JSONObject(
            request(
                baseUrl, "/api/upload", "POST", rawBody = bytes,
                extraHeaders = mapOf("X-Original-Filename" to encodedName),
            ),
        )
    }

    suspend fun checkHealth(baseUrl: String): Boolean =
        try {
            getObject(baseUrl, "/api/health").optBoolean("ok", false)
        } catch (e: Exception) {
            false
        }
}
