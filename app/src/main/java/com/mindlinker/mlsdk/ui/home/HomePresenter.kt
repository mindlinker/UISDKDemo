package com.mindlinker.mlsdk.ui.home

import android.content.Context
import android.util.Log
import com.mindlinker.mlsdk.R
import com.mindlinker.mlsdk.network.auth.AuthEngine
import com.mindlinker.mlsdk.network.auth.IAuthEngine
import com.mindlinker.sdk.api.MLApi
import com.mindlinker.sdk.api.callback.AuthCallback
import com.mindlinker.sdk.api.callback.DismissOtherMeetingCallback
import com.mindlinker.sdk.api.callback.MeetingCallback
import com.mindlinker.sdk.constants.MAXME_REJOIN_MEETING
import com.mindlinker.sdk.model.call.CallCancelList
import com.mindlinker.sdk.model.call.CallCheckResult
import com.mindlinker.sdk.model.call.CallResponseType
import com.mindlinker.sdk.model.call.CallSocketResponse
import com.mindlinker.sdk.model.meeting.MLRoomInfo
import com.mindlinker.sdk.socket.CallSocketCallback

class HomePresenter : IHomePresenter {

    companion object {
//        const val URL: String = "https://apis-pre-pro.mindlinker.cn"
//        const val URL: String = "http://apis-tang.gz.mindlinker.cn"
        const val URL = "http://47.97.111.94:8090"
        const val TAG = "HomePresenter"
    }

    private var mAuthEngine: IAuthEngine? = null
    private var mView: IHomeView? = null
    private var mCallId: String = ""
    private var mRoomId: String = ""
    private lateinit var mContext: Context

    override fun init(context: Context) {
        mContext = context
        initAuthor()
    }

    override fun initCall(mUserId: String, accessToken: String) {
        MLApi.connectCallSocket(
            mUserId,
            object : CallSocketCallback {
                override fun receiverCall(call: CallCheckResult) {
                }

                override fun cancelCall() {
                }

                override fun callResponse(response: CallSocketResponse) {
                }
            }
        )
    }

    override fun cancelCallRequest() {
        MLApi.cancelCallRequest(CallCancelList(arrayOf(mCallId))) {
            Log.i(TAG, "cancelCallRequest result: $it")
        }
    }

    override fun responseCall() {
        MLApi.responseCall(mCallId, CallResponseType.ACCEPT.value) {
            if (it) {
                mView?.joinMeeting(mRoomId)
            } else {
                mView?.showToast("????????????")
                cancelCallRequest()
            }
        }
    }

    override fun authenticate(token: String, nickName: String) {
        MLApi.authenticate(
            URL,
            token, nickName, "",
            object : AuthCallback {
                override fun onSuccess() {
                    Log.i(TAG, "MaxME authenticate success.")
                    mView?.authenticateSuccess()
                }

                override fun onError(code: Int, msg: String) {
                    Log.w(TAG, "MaxME authenticate failed, error : $code msg: $msg")
                }
            }
        )
    }

    override fun initAuthor() {
        if (null == mAuthEngine) {
            mAuthEngine = AuthEngine()
        }
        mAuthEngine?.init(URL)
    }

    override fun accountLogin(userName: String, password: String) {
        mAuthEngine?.let {
            it.accountLogin(userName, password) { accessToken ->
                Log.i("loginAccount", "accessToken == $accessToken")
                if (accessToken == null) {
                    mAuthEngine?.getClientToken()
                }
                mView?.loginResult(accessToken)
            }
        }
    }

    override fun deviceLogin(deviceNo: String) {
        mAuthEngine?.let {
            it.deviceLogin(deviceNo) { accessToken ->
                Log.i("loginAccount", "accessToken == $accessToken")
                if (accessToken == null) {
                    mAuthEngine?.getClientToken()
                }
                mView?.deviceLoginResult(accessToken)
            }
        }
    }

    override fun setView(view: IHomeView) {
        mView = view
    }

    override fun createMeeting(edUserName: String, isMuteVideo: Boolean, isMuteAudio: Boolean) {
        MLApi.createMeeting(
            isMuteVideo = isMuteVideo, isMuteAudio = isMuteAudio, nickName = "", avatar = "",
            callback = object : MeetingCallback {
                override fun onMeetingExist(roomId: String, sessionId: String) {
                    mView?.showTwoButtonDialog("???????????????????????????????????? \n????????????$roomId", roomId, sessionId)
                    mView?.showLoading(false)
                }

                override fun onSuccess(data: MLRoomInfo) {
                    Log.d(TAG, "on meeting create success data: $data")
                    mView?.createAndJoinMeetingSuccess(data)
                    mView?.showLoading(false)
                }

                override fun onError(code: Int, msg: String) {
                    Log.d(TAG, "on meeting create failure $code")
                    mView?.showToast(msg)
                    mView?.showLoading(false)
                }

                override fun alreadyInMeeting() {
                    mView?.showToast("????????????????????????")
                    mView?.showLoading(false)
                }
            }
        )
    }

    override fun joinMeeting(meetingNUmber: String, isMuteVideo: Boolean, isMuteAudio: Boolean, password: String) {
        MLApi.joinMeeting(
            meetingNo = meetingNUmber,
            isMuteVideo = isMuteVideo,
            isMuteAudio = isMuteAudio,
            pwd = password,
            nickName = "",
            avatar = "",
            callback = object : MeetingCallback {
                override fun onMeetingExist(roomId: String, sessionId: String) {
                    mView?.showTwoButtonDialog("???????????????????????????????????? \n????????????$roomId", roomId, sessionId)
                    mView?.showLoading(false)
                }

                override fun onSuccess(data: MLRoomInfo) {
                    Log.d(TAG, "on joinMeeting success data: $data")
                    mView?.createAndJoinMeetingSuccess(data)
                    mView?.showLoading(false)
                }

                override fun onError(code: Int, msg: String) {
                    Log.d(TAG, "on joinMeeting failure $code")
                    if (code == MAXME_REJOIN_MEETING) {
                        mView?.showOneButtonDialog(mContext.resources.getString(R.string.ml_toast_can_not_join_meeting))
                    } else {
                        mView?.showToast(msg)
                    }
                    mView?.showLoading(false)
                }

                override fun alreadyInMeeting() {
                    mView?.showToast("????????????????????????")
                    mView?.showLoading(false)
                }
            }
        )
    }

    override fun dismissMeeting(sessionId: String) {
        MLApi.dismissOtherMeeting(
            sessionId,
            callback = object : DismissOtherMeetingCallback {
                override fun onSuccess() {
                    mView?.showLoading(false)
                    mView?.showToast("??????????????????")
                }

                override fun onError(code: Int, msg: String) {
                    mView?.showLoading(false)
                    mView?.showToast("??????????????????")
                }
            }
        )
    }

    override fun release() {
        MLApi.stopCallSocket()
    }
}
