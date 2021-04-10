package hu.bme.aut.android.bmelostandfound.data

import java.io.Serializable

class Post(
        var refid: String? = null,
        var uid: String? = null,
        var title: String? = null,
        var body: String? = null,
        var imageUrl: String? = null,
        var building: String? = null,
        var date: String? = null,
        var from: String? = null
) : Serializable