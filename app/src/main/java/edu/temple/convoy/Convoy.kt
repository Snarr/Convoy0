package edu.temple.convoy

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Convoy {
    @POST("/lab/convoy/account.php")
    suspend fun signUp(@Body body: RequestBody) : Response<SignUpResponse>

    @POST("/lab/convoy/account.php")
    suspend fun login(@Body body: RequestBody) : Response<LoginResponse>
}