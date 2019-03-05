package com.laurentiucazalbasu.linkedinbasicprofile.repository

import android.util.Log
import com.laurentiucazalbasu.linkedinbasicprofile.models.BasicInformation
import com.laurentiucazalbasu.linkedinbasicprofile.models.Token
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by laurentiucazalbasu on 05/03/2019
 */

internal class InfoRepository {

    fun getAccessTokenThen(
        code: String,
        redirectUri: String,
        clientId: String,
        clientSecret: String,
        permissions: String,
        then: (accessToken: Token?) -> Unit
    ) {
        val authUrl = "https://www.linkedin.com/oauth/v2/accessToken?" +
                "grant_type=authorization_code" +
                "&code=$code" +
                "&redirect_uri=$redirectUri" +
                "&client_id=$clientId" +
                "&client_secret=$clientSecret" +
                "&permissions=$permissions"

        postDataTo(authUrl) { response ->
            if (response == null) {
                then.invoke(null)
                return@postDataTo
            }
            then.invoke(Token(response))
        }
    }

    fun getEmailAddressThen(accessToken: String, email: (email: String?) -> Unit) {
        val url = "https://api.linkedin.com/v2/emailAddress?" +
                "q=members" +
                "&projection=(elements*(handle~))"
        getResponseFrom(url, accessToken) { response ->
            Log.d("LINKED_IN", response.toString())
            if (response == null) {
                email.invoke(null)
                return@getResponseFrom
            }
            val elements = response.optJSONArray("elements")
            if (elements.length() <= 0) {
                email.invoke(null)
                return@getResponseFrom
            }

            val emailAddress = elements.optJSONObject(0)
                ?.optJSONObject("handle~")
                ?.optString("emailAddress")
            email.invoke(emailAddress)
        }
    }

    fun getBasicInfoThen(accessToken: String, info: (basicInformation: BasicInformation) -> Unit) {
        val url = "https://api.linkedin.com/v2/me?" +
                "projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))"
        getResponseFrom(url, accessToken) { response ->
            Log.d("LINKED_IN", response.toString())
            info.invoke(BasicInformation(response))
        }
    }

    private fun postDataTo(callURL: String, response: (response: JSONObject?) -> Unit) {
        object : Thread() {
            override fun run() {
                try {
                    val url = URL(callURL)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.requestMethod = "POST"
                    urlConnection.connect()

                    if (urlConnection.responseCode != 200) {
                        response.invoke(null)
                    } else {
                        val rsp = getServerResponse(urlConnection.inputStream)
                        response.invoke(rsp)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.invoke(null)
                }
            }
        }.start()
    }

    private fun getResponseFrom(callURL: String, accessToken: String, response: (response: JSONObject?) -> Unit) {
        object : Thread() {
            override fun run() {
                try {
                    val url = URL(callURL)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.requestMethod = "GET"
                    urlConnection.setRequestProperty("authorization", "Bearer $accessToken")
                    urlConnection.connect()

                    if (urlConnection.responseCode != 200) {
                        response.invoke(null)
                    } else {
                        val rsp = getServerResponse(urlConnection.inputStream)
                        response.invoke(rsp)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.invoke(null)
                }
            }
        }.start()
    }

    private fun getServerResponse(inputStream: InputStream?): JSONObject {
        val br = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        val sb = StringBuilder()

        line = br.readLine()
        while (line != null) {
            sb.append(line)
            sb.append("\n")
            line = br.readLine()
        }
        br.close()

        return JSONObject(sb.toString())
    }
}