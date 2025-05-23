package pt.techcare.app.data.api

import pt.techcare.app.data.model.Avaria
import pt.techcare.app.data.model.AvariaRequest
import pt.techcare.app.data.model.Comentario
import pt.techcare.app.data.model.ComentarioRequest
import pt.techcare.app.data.model.LoginRequest
import pt.techcare.app.data.model.LoginResponse
import pt.techcare.app.data.model.RegisterRequest
import pt.techcare.app.data.model.RelatorioEstatisticas
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("avarias")
    suspend fun getAvarias(): Response<List<Avaria>>

    @POST("avarias")
    suspend fun registarAvaria(@Body avaria: AvariaRequest): Response<Void>

    @GET("avarias/{id}/comentarios")
    suspend fun getComentarios(@Path("id") idAvaria: Int): Response<List<Comentario>>

    @POST("avarias/{id}/comentarios")
    suspend fun enviarComentario(
        @Path("id") idAvaria: Int,
        @Body request: ComentarioRequest
    ): Response<Void>

    @GET("relatorios/estatisticas")
    suspend fun getEstatisticas(): Response<RelatorioEstatisticas>
}