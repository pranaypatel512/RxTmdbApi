@file:Suppress("unused")

package studio.forface.rxtmdbapi.tmdb

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import studio.forface.rxtmdbapi.models.Media
import studio.forface.rxtmdbapi.utils.MediaDeserializer
import studio.forface.rxtmdbapi.utils.MediaSerializer

/**
 * @author 4face Studio (Davide Giuseppe Farella).
 */

private const val TMDB_API_URL = "https://api.themoviedb.org/"
private const val TMDB_API_URL_V3 = "${TMDB_API_URL}3/"
private const val TMDB_API_URL_V4 = "${TMDB_API_URL}4/"

private const val PARAM_API_KEY = "api_key"
private const val HEADER_API_V4_ACCESS_READ_TOKEN = "Authorization"
private const val PARAM_SESSION_ID = "session_id"
private const val PARAM_GUEST_SESSION_ID = "guest_session_id"

internal const val HEADER_JSON = "Content-Type: application/json;charset=utf-8"

class TmdbApi(
        apiV3Key: String,
        apiV4accessToken: String? = null,
        sessionId: String? = null,
        guest: Boolean = false
) {

    private val interceptor = QueryInterceptor( mutableMapOf( PARAM_API_KEY to apiV3Key ) ).apply {
        apiV4accessToken?.let {
            if ( it.split('.' ).size == 3) {
                addHeaders(HEADER_API_V4_ACCESS_READ_TOKEN to "Bearer $it" )
            } else {
                throw IllegalArgumentException( "Api V4 access read token has a wrong format!" )
            }
        }
        sessionId?.let {
            val paramName = if ( guest ) PARAM_GUEST_SESSION_ID else PARAM_SESSION_ID
            addQueryParams(paramName to it )
        }
    }

    private val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor( interceptor )

    private val gson get() = GsonBuilder()
            .registerTypeAdapter( Media::class.java, MediaSerializer())
            .registerTypeAdapter( Media::class.java, MediaDeserializer())
            .create()

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl( TMDB_API_URL_V3 )
            .addCallAdapterFactory( RxJava2CallAdapterFactory.createAsync() )
            .addConverterFactory( GsonConverterFactory.create( gson ) )

    private inline fun <reified S> getService(): S = retrofitBuilder
            .client( httpClientBuilder.build() )
            .build()
            .create( S::class.java )

    val auth            by lazy { TmdbAuth( getService(), { setSession( it ) } ) }
    val authV4          by lazy { TmdbAuthV4( getService(), { setAccessToken( it ) } ) }
    val account         by lazy { getService<TmdbAccount>() }
    val accountV4       by lazy { getService<TmdbAccountV4>() }
    val certifications  by lazy { getService<TmdbCertifications>() }
    val changes         by lazy { getService<TmdbChanges>() }
    val collections     by lazy { getService<TmdbCollections>() }
    val config          by lazy { getService<TmdbConfig>() }
    val discover        by lazy { getService<TmdbDiscover>() }
    val keywords        by lazy { getService<TmdbKeywords>() }
    val movies          by lazy { getService<TmdbMovies>() }
    val networks        by lazy { getService<TmdbNetworks>() }
    val people          by lazy { getService<TmdbPeople>() }
    val reviews         by lazy { getService<TmdbReviews>() }
    val search          by lazy { getService<TmdbSearch>() }
    val tvShows         by lazy { getService<TmdbTvShows>() }
    val tvSeasons       by lazy { getService<TmdbTvSeasons>() }
    val tvEpisodes      by lazy { getService<TmdbTvEpisodes>() }


    private fun setSession( session: Session ) {
        interceptor.removeQueryParams( PARAM_SESSION_ID )
        interceptor.removeQueryParams( PARAM_GUEST_SESSION_ID )

        if ( session.success ) {
            val paramName = when( session.guest ) {
                false -> PARAM_SESSION_ID
                true -> PARAM_GUEST_SESSION_ID
            }
            interceptor.addQueryParams(paramName to session.sessionId)
        }
    }

    private fun setAccessToken( token: TokenV4 ) {
        if ( token.value.isNotBlank() ) {
            interceptor.addHeaders(HEADER_API_V4_ACCESS_READ_TOKEN to  "Bearer ${token.value}")
        } else {
            interceptor.removeHeaders( HEADER_API_V4_ACCESS_READ_TOKEN )
        }
    }

}


private class QueryInterceptor(private val params: MutableMap<String, String>) : Interceptor {

    private val headers = mutableMapOf<String, String>()

    internal fun addHeaders( vararg headers: Pair<String, String> ) {
        this.headers.putAll( headers )
    }

    internal fun removeHeaders( vararg names: String ) {
        names.forEach { headers.remove( it ) }
    }

    internal fun addQueryParams( vararg queryParams: Pair<String, String> ) {
        params.putAll( queryParams )
    }

    internal fun removeQueryParams( vararg keys: String ) {
        keys.forEach { params.remove( it ) }
    }

    override fun intercept( chain: Interceptor.Chain ): Response {

        return chain.request().let { request ->

            val url = request.url().newBuilder().apply {
                params.forEach { addQueryParameter( it.key, it.value ) }
            }.build()

            println( url.toString() )

            val newRequest = request.newBuilder()
                    .url( url )
                    .apply { headers.forEach { addHeader( it.key, it.value ) } }
                    .method( request.method(), request.body() )
                    .build()

            chain.proceed( newRequest )
        }

    }

}