package org.d3if3137.wizznews.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.d3if3137.wizznews.model.News
import org.d3if3137.wizznews.model.OpStatus
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

private const val BASE_URL = "https://rapidly-engaged-mule.ngrok-free.app/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
    .baseUrl(BASE_URL)
    .build()

interface NewsApiService {
    @GET("belajarRestApiWeb/files/wijdan/view.php")
    suspend fun getNews(): List<News>

    @Multipart
    @POST("belajarRestApiWeb/files/wijdan/AddNews.php")
    suspend fun postNews(
        @Header("Authorization") userId: String,
        @Part("judul") judul: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

}

object NewsApi {
    val service: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }

    fun getNewsUrl(imageId: String): String {
        return "${BASE_URL}belajarRestApiWeb/files/wijdan/image.php?imageId=$imageId"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }