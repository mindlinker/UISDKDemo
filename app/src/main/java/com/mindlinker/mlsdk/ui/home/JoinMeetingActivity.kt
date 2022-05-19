package com.mindlinker.mlsdk.ui.home

import android.Manifest
import android.graphics.Color
import android.view.View
import androidx.core.app.ActivityCompat
import com.maxhub.logger.Logger
import com.mindlinker.mlsdk.R
import com.mindlinker.mlsdk.ui.home.meeting.MeetingActivity
import com.mindlinker.mlsdk.utils.NetworkUtil
import com.mindlinker.mlsdk.utils.ShapeSelector
import com.mindlinker.sdk.api.MLApi
import com.mindlinker.sdk.api.callback.MeetingCallback
import com.mindlinker.sdk.base.BaseActivity
import com.mindlinker.sdk.constants.JOIN_PASSWORD_ERROR
import com.mindlinker.sdk.constants.MAXME_REJOIN_MEETING
import com.mindlinker.sdk.engine.MLEngine
import com.mindlinker.sdk.model.dialog.DialogInfo
import com.mindlinker.sdk.model.meeting.MLRoomInfo
import com.mindlinker.sdk.model.user.LeaveType
import com.mindlinker.sdk.ui.dialog.customdialog.OneButtonDialog
import com.mindlinker.sdk.ui.dialog.customdialog.OneEditTwoButtonDialog
import com.mindlinker.sdk.ui.dialog.customdialog.TwoButtonDialog
import com.mindlinker.sdk.utils.CustomToast
import com.mindlinker.sdk.utils.KeyboardUtils
import com.mindlinker.sdk.utils.PermissionUtil
import kotlinx.android.synthetic.main.activity_join_meeting.*
import kotlinx.android.synthetic.main.activity_join_meeting.flLoading
import kotlinx.android.synthetic.main.activity_join_meeting.joinMeeting
import kotlinx.android.synthetic.main.activity_main.*

class JoinMeetingActivity : BaseActivity() {

    private var mOneButtonDialog: OneButtonDialog? = null
    private var mTwoButtonDialog: TwoButtonDialog? = null
    private var mOneEditTwoButtonDialog: OneEditTwoButtonDialog? = null
    private var isAudioOpen = false
    private var isVideoOpen = false
    private var isJoining = false
    private var edUserName = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_join_meeting
    }

    override fun initData() {
        edUserName = intent.getStringExtra("NAME")
        val shapeSelector = ShapeSelector()
        val bg = shapeSelector.setDefaultBgColor(Color.parseColor("#0B7BFF"))
            .setCornerRadius(8).create()
        joinMeeting.background = bg
        edMeeting.postDelayed({
            KeyboardUtils.showKeyboard(edMeeting)
        }, 50)
        audioOptionSwitchButton.setSwitchButtonChangedListener(object : ISwitchButtonChangedListener {
            override fun onCheckChanged(isChecked: Boolean) {
                isAudioOpen = isChecked
            }
        })
        videoOptionSwitchButton.setSwitchButtonChangedListener(object : ISwitchButtonChangedListener {
            override fun onCheckChanged(isChecked: Boolean) {
                isVideoOpen = isChecked
            }
        })

        joinMeeting.setOnClickListener {
            val meetingNo = edMeeting.text.toString().trim()
            if (meetingNo.isNullOrEmpty()) {
                showToast("请输入房间号")
                return@setOnClickListener
            }
            if (!NetworkUtil.isConnected(this)) {
                showToast(resources.getString(R.string.ml_network_fail))
                return@setOnClickListener
            }
            val permissions = ArrayList<String>()
            if (isAudioOpen) {
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }
            if (isVideoOpen) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (permissions.size > 0) {
                requestAppPermissions(permissions.toTypedArray())
            } else {
                joinMeeting("")
            }
            KeyboardUtils.hideKeyboard(edMeeting)
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun requestAppPermissions(permissions: Array<String>) {
        if (!PermissionUtil.hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            joinMeeting("")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.RECORD_AUDIO) {
                    isAudioOpen = grantResults[i] == 0
                }
                if (permissions[i] == Manifest.permission.CAMERA) {
                    isVideoOpen = grantResults[i] == 0
                }
            }
            joinMeeting("")
        }
    }

    private fun joinMeeting(pwd: String) {
        if (isJoining) {
            return
        }
        isJoining = true
        val nickname = if (edName.text.toString().trim().isNotEmpty()) {
            edName.text.toString().trim()
        } else {
            edUserName
        }
        if (nickname.isNotEmpty()) {
            MLEngine.getMLEngine().getMeetingEngine().setNickname(nickname, "") {
                Logger.i("join meeting and nickname = $nickname result: $it")
            }
        }
        showLoading(true)
        val meetingNo = edMeeting.text.toString().trim()
        MLApi.joinMeeting(
            meetingNo = meetingNo,
            isMuteVideo = !isVideoOpen,
            isMuteAudio = !isAudioOpen,
            pwd = pwd,
            nickName = nickname,
            avatar = "",
            callback = object : MeetingCallback {
                override fun onMeetingExist(roomId: String, sessionId: String) {
                    isJoining = false
                    showToast("你之前有创建的房间未结束\n房间号：$roomId")
                    showLoading(false)
                }

                override fun onSuccess(data: MLRoomInfo) {
                    isJoining = false
                    Logger.d("on joinMeeting success data: $data")
                    createAndJoinMeetingSuccess()
                    showLoading(false)
                }

                override fun onError(code: Int, msg: String) {
                    isJoining = false
                    Logger.d("on joinMeeting failure $code")
                    runOnUiThread() {
                        when (code) {
                            MAXME_REJOIN_MEETING -> {
                                showOneButtonDialog(resources.getString(R.string.ml_toast_can_not_join_meeting))
                            }
                            JOIN_PASSWORD_ERROR -> {
                                if (pwd.isNotEmpty()) {
                                    showToast(msg)
                                }
                                showPwdDialog()
                            }
                            else -> {
                                showToast(msg)
                            }
                        }
                        showLoading(false)
                    }
                }

                override fun alreadyInMeeting() {
                    isJoining = false
                    runOnUiThread {
                        showToast("你已经在房间中了")
                        showLoading(false)
                    }
                }
            }
        )
        audioOptionSwitchButton.setChecked(isAudioOpen)
        videoOptionSwitchButton.setChecked(isVideoOpen)
    }

    private fun createAndJoinMeetingSuccess() {
        MLApi.setLeaveCallBack {
            when (it) {
                LeaveType.TYPE_KICK_OUT -> { // 成员被主持人提出会议回调
                    showOneButtonDialog(getString(R.string.remove_by_host))
//                    CustomToast.show(this, getString(R.string.remove_by_host), CustomToast.ToastPosition.CENTER)
                }
                LeaveType.TYPE_DISCONNECT -> { // 成员掉线回调
                    showTwoButtonDialog(getString(R.string.video_login_out))
//                    CustomToast.show(this, getString(R.string.video_login_out), CustomToast.ToastPosition.CENTER)
                }
            }
        }
        runOnUiThread {
            MeetingActivity.startMeetingActivity(this)
            clearDialog()
        }
    }

    private fun showOneButtonDialog(subText: String) {
        runOnUiThread() {
//            dialogInfo.hideDialog = DialogInfo.DialogType.UNSPECIFIED
            mOneButtonDialog?.dismiss()
            mOneButtonDialog = OneButtonDialog(
                this,
                subText
            )
            mOneButtonDialog?.updateSubtext(subText)
            mOneButtonDialog?.show()
        }
    }

    private fun showTwoButtonDialog(subText: String) {
        runOnUiThread() {
//            dialogInfo.hideDialog = DialogInfo.DialogType.UNSPECIFIED
            mTwoButtonDialog?.dismiss()
            mTwoButtonDialog = TwoButtonDialog(
                this,
                "",
                subText,
                DialogInfo.DialogType.OPEN_MIC,
                confirmButtonText = getString(R.string.confirm_rejoin),
                cancelButtonText = getString(R.string.cancel_rejoin),
                onConfirmCallback = {
                    joinMeeting("")
                    mTwoButtonDialog?.dismiss()
                },
                onCancelCallback = {
                    mTwoButtonDialog?.dismiss()
                }
            )
            mTwoButtonDialog?.show()
        }
    }

    private fun showToast(s: String) {
        CustomToast.show(this, s, CustomToast.ToastPosition.CENTER)
    }

    private fun showLoading(isShow: Boolean) {
        runOnUiThread {
            flLoading.visibility = if (isShow) View.VISIBLE else View.GONE
        }
    }

    private fun showPwdDialog() {
        if (null == mOneEditTwoButtonDialog) {
            mOneEditTwoButtonDialog = OneEditTwoButtonDialog(
                this,
                DialogInfo.DialogType.JOIN_MEETING,
                onCancelCallback = {
                    mOneEditTwoButtonDialog?.dismiss()
                    mOneEditTwoButtonDialog = null
                },
                onConfirmCallback = { pwd ->
                    mOneEditTwoButtonDialog?.dismiss()
                    mOneEditTwoButtonDialog = null
                    joinMeeting(pwd)
                }
            )
        }
        mOneEditTwoButtonDialog?.show()
    }

    override fun onDestroy() {
        clearDialog()
        super.onDestroy()
    }

    private fun clearDialog() {
        mOneEditTwoButtonDialog?.dismiss()
        mOneButtonDialog?.dismiss()
        mTwoButtonDialog?.dismiss()
        mOneButtonDialog = null
        mTwoButtonDialog = null
        mOneEditTwoButtonDialog = null
    }
}
