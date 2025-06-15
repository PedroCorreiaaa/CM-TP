package pt.techcare.app.data.api

import okhttp3.MultipartBody
import pt.techcare.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/tecnicos")
    suspend fun registrarTecnico(@Body request: RegisterRequest): Response<User>

    @GET("api/tecnicos")
    suspend fun getTecnicos(): Response<List<User>>

    @GET("api/avarias")
    suspend fun getAvarias(): Response<List<Avaria>>

    @POST("api/avarias")
    suspend fun registarAvaria(@Body avaria: AvariaRequest): Response<Avaria>

    @GET("api/avarias/{id}")
    suspend fun getAvariaById(@Path("id") idAvaria: Int): Response<Avaria>

    @PATCH("api/avarias/{id}")
    suspend fun atualizarAvaria(
        @Path("id") idAvaria: Int,
        @Body request: AvariaUpdateRequest
    ): Response<Avaria>

    @POST("api/avarias/{id}/tecnico")
    suspend fun atribuirTecnico(
        @Path("id") idAvaria: Int,
        @Body request: AtribuirTecnicoRequest
    ): Response<Void>

    @Multipart
    @POST("api/avarias/{id}/imagem")
    suspend fun uploadImagem(
        @Path("id") idAvaria: Int,
        @Part imagem: MultipartBody.Part
    ): Response<Void>

    @GET("api/avarias/tecnico/{id}")
    suspend fun getAvariasTecnico(@Path("id") idTecnico: Int): Response<List<Avaria>>

    @GET("api/avarias/user/{id}")
    suspend fun getAvariasUtilizador(@Path("id") idUtilizador: Int): Response<List<Avaria>>

    @GET("api/user/{id}/notificacoes")
    suspend fun getNotificacoes(@Path("id") id: Int): Response<List<Notificacao>>

    @GET("api/relatorios/estatisticas")
    suspend fun getEstatisticas(
        @Query("datas") datas: String? = null,
        @Query("tipoEquipamento") tipoEquipamento: String? = null,
        @Query("localizacao") localizacao: String? = null
    ): Response<RelatorioEstatisticas>
}
