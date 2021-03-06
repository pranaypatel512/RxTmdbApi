package studio.forface.rxtmdbapi.models

import com.google.gson.annotations.SerializedName


data class Account(

    @SerializedName("avatar")           val avatar: Avatar,
    @SerializedName("id") override      val id: Int,
    @SerializedName("iso_639_1")        val iso6391: String,
    @SerializedName("iso_3166_1")       val iso31661: String,
    @SerializedName("name") override    val name: String,
    @SerializedName("include_adult")    val includeAdult: Boolean,
    @SerializedName("username")         val username: String

) : NamedIdElement