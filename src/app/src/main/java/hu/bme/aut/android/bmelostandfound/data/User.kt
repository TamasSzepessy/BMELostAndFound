package hu.bme.aut.android.bmelostandfound.data

import java.io.Serializable

class User(
        var uid: String? = null,
        var usermail: String? = null,
        var username: String? = null,
        var phone: String? = null,
        var imageUrl: String? = null
) : Serializable