package com.laurentiucazalbasu.linkedinbasicprofile.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

/**
 * Created by laurentiucazalbasu on 05/03/2019
 */

class BasicInformation : Parcelable {

    var id: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var profilePicture: String? = null
    var emailAddress: String? = null

    constructor()

    constructor(parcel: Parcel) {
        id = parcel.readString()
        firstName = parcel.readString()
        lastName = parcel.readString()
        profilePicture = parcel.readString()
        emailAddress = parcel.readString()
    }

    constructor(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            this.id = jsonObject.optString("id")
            this.firstName = getFirstName(jsonObject)
            this.lastName = getLastName(jsonObject)
            this.profilePicture = getProfilePictureUrl(jsonObject)
        }
    }

    private fun getFirstName(jsonObject: JSONObject): String? {
        val obj = jsonObject.optJSONObject("firstName")
        val localeObj = obj?.optJSONObject("preferredLocale")
        val locale = localeObj?.optString("language") + "_" +
                localeObj?.optString("country")
        return obj?.optJSONObject("localized")?.getString(locale)
    }

    private fun getLastName(jsonObject: JSONObject): String? {
        val obj = jsonObject.optJSONObject("lastName")
        val localeObj = obj?.optJSONObject("preferredLocale")
        val locale = localeObj?.optString("language") + "_" +
                localeObj?.optString("country")
        return obj?.optJSONObject("localized")?.getString(locale)
    }

    private fun getProfilePictureUrl(jsonObject: JSONObject): String? {
        val obj = jsonObject.optJSONObject("profilePicture")
        val displayImage = obj?.optJSONObject("displayImage~")
        val elements = displayImage?.optJSONArray("elements")
        if (elements == null || elements.length() <= 0) {
            return null
        }

        val identifiers = elements.optJSONObject(elements.length() - 1)
            .optJSONArray("identifiers")
        if (identifiers == null || identifiers.length() <= 0) {
            return null
        }

        return identifiers.optJSONObject(0)
            .optString("identifier")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(profilePicture)
        parcel.writeString(emailAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BasicInformation> {
        override fun createFromParcel(parcel: Parcel): BasicInformation {
            return BasicInformation(parcel)
        }

        override fun newArray(size: Int): Array<BasicInformation?> {
            return arrayOfNulls(size)
        }
    }
}