package studio.forface.rxtmdbapi.models

import android.arch.persistence.room.ColumnInfo
import com.google.gson.annotations.SerializedName


data class Translations<S> (

    @SerializedName(Fields.TRANSLATIONS) @ColumnInfo(name = Fields.TRANSLATIONS)
    var translations: List<Translation<S>> = listOf()

)


data class Translation<S> (

    @SerializedName("iso_3166_1")       val iso31661: String,
    @SerializedName("iso_639_1")        val iso6391: String,
    @SerializedName("name") override    val name: String,
    @SerializedName("english_name")     val englishName: String,
    @SerializedName("data")             val data: S

) : NamedElement