package com.laurentiucazalbasu.linkedinbasicprofile.models

/**
 * Created by laurentiucazalbasu on 05/03/2019
 */
internal sealed class Resource<out T : Any> {
    class Loading<out T : Any>(val data: T? = null) : Resource<T>()
    class Success<out T : Any>(val data: T) : Resource<T>()
    class Error<out T : Any>(val error: String, val data: T? = null) : Resource<T>()
}