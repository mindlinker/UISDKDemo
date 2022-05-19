package com.mindlinker.mlsdk.network.auth

import android.annotation.SuppressLint
import android.util.Base64
import com.maxhub.logger.Logger
import com.mindlinker.mlsdk.model.auth.AccessToken
import com.mindlinker.mlsdk.model.auth.ClientToken
import com.mindlinker.mlsdk.model.auth.DeviceAccessToken
import com.mindlinker.mlsdk.utils.SHAUtil
import com.mindlinker.sdk.network.BaseApi
import com.mindlinker.sdk.utils.Callback1
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.nio.charset.Charset

class AuthEngine : IAuthEngine {

    companion object {
        private const val TAG = "AuthEngine"
        // 预发环境
//        const val PRODUCT_CLIENT_ID = "f58de42b-75b2-46cf-b67a-f7395d8f6ed3"
//        const val PRODUCT_SECRET_CODE = "hw3bsCzMCAS7ywdM"
        // TANG环境
//        const val PRODUCT_CLIENT_ID = "476f1cbc-cb89-e5e0-e6d5-ea80dad35ed4"
//        const val PRODUCT_SECRET_CODE = "AeHtei2wO9j61YhW"
        // 私有化
        const val PRODUCT_CLIENT_ID = "f58de42b-75b2-46cf-b67a-f7395d8f6ed3"
        const val PRODUCT_SECRET_CODE = "hw3bsCzMCAS7ywdM"
    }
    private var mAuthUrl: String = ""

    private lateinit var mAuthApi: IAuthApi
    private var mClientToken: ClientToken? = null

    override fun init(authUrl: String) {
        mAuthUrl = authUrl
        mAuthApi = BaseApi(mAuthUrl).create(IAuthApi::class.java)
        getClientToken()
    }

    @SuppressLint("CheckResult")
    override fun getClientToken() {
        val auth =
            String(
                Base64.encode(("$PRODUCT_CLIENT_ID:$PRODUCT_SECRET_CODE").toByteArray(), Base64.NO_WRAP),
                Charset.forName("utf-8")
            )
        Logger.i(TAG, "getClientToken auth: $auth")
        mAuthApi.getClientToken(BaseApi.BASIC + auth)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    mClientToken = result
                    Logger.d(TAG, "getClientToken info = $result")
                },
                { error ->
                    Logger.i(TAG, "getClientToken error $error")
                }
            )
    }

    @SuppressLint("CheckResult")
    override fun accountLogin(userName: String, password: String, callback: Callback1<AccessToken?>) {
        if (mClientToken == null) {
            Logger.w(TAG, "accountLogin fail no client token ")
            callback.invoke(null)
            return
        }
        val password = SHAUtil.sha(password, SHAUtil.SHA256)
        val loginBody = LoginBody(userName, password)
        var accessToken: AccessToken
        Logger.i("userName: $userName password: $password auth: ${BaseApi.BEARER + mClientToken!!.access_token}")
        mAuthApi.accountLogin(BaseApi.BEARER + mClientToken!!.access_token, loginBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    accessToken = result
                    Logger.d(TAG, "accountLogin result = $result")
                    callback.invoke(accessToken)
                },
                { error ->
                    Logger.i(TAG, "accountLogin error $error")
                    callback.invoke(null)
                }
            )
    }

    @SuppressLint("CheckResult")
    override fun deviceLogin(androidId: String, callback: Callback1<DeviceAccessToken?>) {
        if (mClientToken == null) {
            Logger.w(TAG, "accountLogin fail no client token ")
            callback.invoke(null)
            return
        }
        var accessToken: DeviceAccessToken
        Logger.i("androidId: $androidId  auth: ${BaseApi.BEARER + mClientToken!!.access_token}")
        val deviceBody = DeviceBody(androidId)
        mAuthApi.deviceLogin(BaseApi.BEARER + mClientToken!!.access_token, deviceBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    accessToken = result
                    Logger.d(TAG, "accountLogin result = $result")
                    callback.invoke(accessToken)
                },
                { error ->
                    Logger.i(TAG, "accountLogin error $error")
                    callback.invoke(null)
                }
            )
    }
}
