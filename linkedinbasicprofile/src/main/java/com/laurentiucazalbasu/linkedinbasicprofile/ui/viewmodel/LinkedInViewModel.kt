package com.laurentiucazalbasu.linkedinbasicprofile.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laurentiucazalbasu.linkedinbasicprofile.models.BasicInformation
import com.laurentiucazalbasu.linkedinbasicprofile.models.Resource
import com.laurentiucazalbasu.linkedinbasicprofile.repository.InfoRepository

/**
 * Created by laurentiucazalbasu on 05/03/2019
 */

internal class LinkedInViewModel : ViewModel() {

    private val infoRepository = InfoRepository()
    private val _profileInformation = MutableLiveData<Resource<BasicInformation>>()
    val profileInformation: LiveData<Resource<BasicInformation>> = _profileInformation

    @Synchronized
    fun loadProfileInformation(
        code: String,
        redirectUri: String,
        clientId: String,
        clientSecret: String,
        permissions: String
    ) {
        _profileInformation.postValue(Resource.Loading())
        infoRepository.getAccessTokenThen(
            code, redirectUri, clientId, clientSecret, permissions
        ) { accessToken ->
            if (accessToken == null) {
                _profileInformation.postValue(Resource.Error("Access token is null"))
                return@getAccessTokenThen
            }


            var waitFor = 2
            val userProfile = BasicInformation()
            loadBasicProfileThen(accessToken.accessToken) { basicInformation ->
                synchronized(waitFor) {
                    waitFor--
                    userProfile.id = basicInformation.id
                    userProfile.firstName = basicInformation.firstName
                    userProfile.lastName = basicInformation.lastName
                    userProfile.profilePicture = basicInformation.profilePicture
                    if (waitFor == 0) {
                        _profileInformation.postValue(Resource.Success(userProfile))
                    }
                }
            }
            loadEmailAddressThen(accessToken.accessToken) { emailAddress ->
                synchronized(waitFor) {
                    waitFor--
                    userProfile.emailAddress = emailAddress
                    if (waitFor == 0) {
                        _profileInformation.postValue(Resource.Success(userProfile))
                    }
                }
            }
        }
    }

    private fun loadBasicProfileThen(accessToken: String, then: (basicInformation: BasicInformation) -> Unit) {
        infoRepository.getBasicInfoThen(accessToken) { basicInformation ->
            then.invoke(basicInformation)
        }
    }

    private fun loadEmailAddressThen(accessToken: String, then: (email: String?) -> Unit) {
        infoRepository.getEmailAddressThen(accessToken) { email ->
            then.invoke(email)
        }
    }
}
