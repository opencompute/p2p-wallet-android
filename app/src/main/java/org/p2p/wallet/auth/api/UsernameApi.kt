package org.p2p.wallet.auth.api

import org.json.JSONObject
import org.p2p.wallet.auth.model.NameRegisterBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsernameApi {

    @GET("name_register/{username}")
    suspend fun checkUsername(@Path("username") username: String): CheckUsernameResponse

    @GET("name_register/auth/gt/register")
    suspend fun checkCaptcha(): JSONObject

    @POST("name_register/{username}")
    suspend fun registerUsername(
        @Path("username") username: String,
        @Body body: NameRegisterBody
    ): RegisterUsernameResponse

    @GET("name_register/lookup/{owner}")
    suspend fun lookup(@Path("owner") owner: String): ArrayList<LookupUsernameResponse>
}