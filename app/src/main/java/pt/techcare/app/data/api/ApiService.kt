package pt.techcare.app.data.api

import pt.techcare.app.data.model.LoginRequest
import pt.techcare.app.data.model.LoginResponse
import pt.techcare.app.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
}