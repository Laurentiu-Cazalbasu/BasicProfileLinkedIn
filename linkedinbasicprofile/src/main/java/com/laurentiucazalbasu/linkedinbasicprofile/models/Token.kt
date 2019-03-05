package com.laurentiucazalbasu.linkedinbasicprofile.models

import org.json.JSONObject

/**
 * Created by laurentiucazalbasu on 05/03/2019
 */

internal class Token(json: JSONObject) {
    val accessToken: String = json.getString("access_token")
    val expiresIn: Long = json.getLong("expires_in")
}