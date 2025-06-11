package pt.techcare.app.data.api

import okhttp3.MultipartBody
import pt.techcare.app.data.model.AtribuirTecnicoRequest
import pt.techcare.app.data.model.Avaria
import pt.techcare.app.data.model.AvariaRequest
import pt.techcare.app.data.model.AvariaUpdateRequest
import pt.techcare.app.data.model.Comentario
import pt.techcare.app.data.model.ComentarioRequest
import pt.techcare.app.data.model.LoginRequest
import pt.techcare.app.data.model.LoginResponse
import pt.techcare.app.data.model.Notificacao
import pt.techcare.app.data.model.NotificacaoRequest
import pt.techcare.app.data.model.NotificacaoResponse
import pt.techcare.app.data.model.RegisterRequest
import pt.techcare.app.data.model.RelatorioEstatisticas
import pt.techcare.app.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    @GET("api/avarias")
    suspend fun getAvarias(): Response<List<Avaria>>

    @POST("api/avarias")
    suspend fun registarAvaria(@Body avaria: AvariaRequest): Response<Avaria>

    @GET("api/avarias/{id}")
    suspend fun getAvariaById(@Path("id") idAvaria: Int): Response<Avaria>

    @GET("api/avarias/{id}/comentarios")
    suspend fun getComentarios(@Path("id") idAvaria: Int): Response<List<Comentario>>

    @POST("api/avarias/{id}/comentarios")
    suspend fun enviarComentario(
        @Path("id") idAvaria: Int,
        @Body request: ComentarioRequest
    ): Response<Void>

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

    @GET("api/tecnicos")
    suspend fun getTecnicos(): Response<List<User>>

    @FormUrlEncoded
    @POST("api/tecnicos")
    suspend fun registrarTecnico(
        @Field("nome") nome: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<User>

    @GET("api/relatorios/estatisticas")
    suspend fun getEstatisticas(
        @Query("datas") datas: String? = null,
        @Query("tipoEquipamento") tipoEquipamento: String? = null,
        @Query("localizacao") localizacao: String? = null
    ): Response<RelatorioEstatisticas>


    @PATCH("api/avarias/{id}")
    suspend fun atualizarAvaria(
        @Path("id") idAvaria: Int,
        @Body request: AvariaUpdateRequest
    ): Response<Avaria>

    @GET("api/user/{id}/notificacoes")
    suspend fun getNotificacoes(@Path("id") id: Int): Response<List<Notificacao>>

}