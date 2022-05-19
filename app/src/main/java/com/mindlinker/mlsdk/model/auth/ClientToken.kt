package com.mindlinker.mlsdk.model.auth

import androidx.annotation.Keep

/**
 * @author Aaron
 * @data 2018/1/11
 */
@Keep
data class ClientToken(var access_token: String, var expires_in: Int, var token_type: String, var state: String)
