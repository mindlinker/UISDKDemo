package com.mindlinker.mlsdk.ui.home

import com.mindlinker.mlsdk.model.auth.AccessToken
import com.mindlinker.mlsdk.model.auth.DeviceAccessToken
import com.mindlinker.sdk.model.meeting.MLRoomInfo

interface IHomeView {

    fun showToast(string: String)
    fun loginResult(accessToken: AccessToken?)
    fun deviceLoginResult(accessToken: DeviceAccessToken?)
    fun authenticateSuccess()
    fun callComming(isCall: Boolean, name: String, meetingId: String)
    fun joinMeeting(meetingId: String)
    fun createAndJoinMeetingSuccess(info: MLRoomInfo)
    fun showLoading(isShow: Boolean)
    fun showOneButtonDialog(subText: String)
    fun showTwoButtonDialog(subText: String, roomCode: String = "", sessionId: String = "")
}
