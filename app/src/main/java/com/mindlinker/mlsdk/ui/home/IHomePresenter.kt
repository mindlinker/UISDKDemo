package com.mindlinker.mlsdk.ui.home

import android.content.Context

interface IHomePresenter {

    fun init(context: Context)
    fun authenticate(token: String, nickName: String)
    fun initAuthor()
    fun accountLogin(userName: String, password: String)
    fun deviceLogin(deviceNo: String)
    fun setView(view: IHomeView)
    fun initCall(userID: String, accessToken: String)
    fun cancelCallRequest()
    fun responseCall()
    fun release()
    fun createMeeting(edUserName: String, isMuteVideo: Boolean, isMuteAudio: Boolean)
    fun joinMeeting(meetingNUmber: String, isMuteVideo: Boolean, isMuteAudio: Boolean, password: String = "")
    fun dismissMeeting(sessionId: String)
}
