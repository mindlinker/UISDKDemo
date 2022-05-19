package com.mindlinker.mlsdk.network.auth

import com.mindlinker.mlsdk.model.auth.AccessToken
import com.mindlinker.mlsdk.model.auth.DeviceAccessToken
import com.mindlinker.sdk.utils.Callback1

interface IAuthEngine {
    fun init(authUrl: String)

    fun getClientToken()

    fun accountLogin(userName: String, password: String, callback1: Callback1<AccessToken?>)

    fun deviceLogin(androidId: String, callback1: Callback1<DeviceAccessToken?>)
}
