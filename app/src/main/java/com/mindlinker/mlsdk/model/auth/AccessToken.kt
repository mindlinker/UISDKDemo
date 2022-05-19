package com.mindlinker.mlsdk.model.auth

import androidx.annotation.Keep

/**
 * @author Aaron
 * @data 2018/1/11
 */
@Keep
data class AccessToken(
    var access_token: String,
    var refresh_token: String,
    var expires_in: Int,
    var token_type: String,
    var state: String,
    var userId: String,
    var deviceSerialNo: String,
    var deviceId: String
)

@Keep
data class DeviceAccessToken(
    var access_token: String,
    var expires_in: Int,
    var token_type: String,
    var userId: String,
    var accid: String
)
