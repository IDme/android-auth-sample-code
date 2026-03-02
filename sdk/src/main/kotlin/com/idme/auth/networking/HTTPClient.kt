package com.idme.auth.networking

import com.idme.auth.errors.IDmeAuthError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/** HTTP response container. */
data class HTTPResponse(
    val statusCode: Int,
    val body: String
)

/** Abstraction over HTTP networking for testability. */
interface HTTPClient {
    suspend fun get(url: String, headers: Map<String, String>): HTTPResponse
    suspend fun postForm(url: String, body: Map<String, String>): HTTPResponse
}

/** Production HTTP client backed by HttpURLConnection. */
class DefaultHTTPClient : HTTPClient {

    override suspend fun get(url: String, headers: Map<String, String>): HTTPResponse =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
                readResponse(connection)
            } catch (e: IDmeAuthError) {
                throw e
            } catch (e: Exception) {
                throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
            } finally {
                connection.disconnect()
            }
        }

    override suspend fun postForm(url: String, body: Map<String, String>): HTTPResponse =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val bodyString = body.entries.joinToString("&") { (key, value) ->
                    "${java.net.URLEncoder.encode(key, "UTF-8")}=${java.net.URLEncoder.encode(value, "UTF-8")}"
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(bodyString)
                    writer.flush()
                }

                readResponse(connection)
            } catch (e: IDmeAuthError) {
                throw e
            } catch (e: Exception) {
                throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
            } finally {
                connection.disconnect()
            }
        }

    private fun readResponse(connection: HttpURLConnection): HTTPResponse {
        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val body = BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }

        return HTTPResponse(statusCode, body)
    }
}
