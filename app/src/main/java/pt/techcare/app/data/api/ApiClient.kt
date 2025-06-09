package pt.techcare.app.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"
    private const val TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOnRydWUsImlhdCI6MTc0ODk2NzI4MCwiZXhwIjoxNzUxNTU5MjgwfQ.vp8wLl8of_9mm0TlBOFCuKe9b2fWNDQGT1Irao840FQ"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Authorization", "Bearer $TOKEN")
            chain.proceed(requestBuilder.build())
        }
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
