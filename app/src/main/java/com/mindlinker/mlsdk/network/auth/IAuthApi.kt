package com.mindlinker.mlsdk.network.auth

import androidx.annotation.Keep
import com.mindlinker.mlsdk.model.auth.AccessToken
import com.mindlinker.mlsdk.model.auth.ClientToken
import com.mindlinker.mlsdk.model.auth.DeviceAccessToken
import io.reactivex.Observable
import retrofit2.http.*

interface IAuthApi {

    @Headers("Accept: application/json;version=1")
    @POST("passport/api/token?grant_type=client_credentials")
    fun getClientToken(@Header("Authorization") auth: String, @Body state: BodyState = BodyState("xyz")): Observable<ClientToken>

    @Headers("Accept: application/json;version=2")
    @POST("passport/api/token?grant_type=password")
    fun accountLogin(@Header("Authorization") auth: String, @Body body: LoginBody): Observable<AccessToken>

    @Headers("Accept: application/json;version=2")
    @POST("passport/api/v1/authorization/third-party/implicit")
    fun deviceLogin(@Header("Authorization") auth: String, @Body body: DeviceBody): Observable<DeviceAccessToken>
}

@Keep
data class BodyState(
    val state: String
)

@Keep
data class LoginBody(
    val username: String,
    val password: String,
    val state: String = "xyz",
    val scope: String = "basic"
)

@Keep
data class DeviceBody(
    val accid: String, // 企业内部账号系统对应的账号id，主要用于关联两边系统的用户
    val nickname: String = "", // 用户昵称，不为空则会在生成令牌同时更新用户的昵称
    val avatar: String = "" // 用户头像的url，不为空则会在生成令牌同时更新用户的头像
)
