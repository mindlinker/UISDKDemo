package com.mindlinker.mlsdk.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.mindlinker.mlsdk.R
import com.mindlinker.mlsdk.model.auth.AccessToken
import com.mindlinker.mlsdk.model.auth.DeviceAccessToken
import com.mindlinker.mlsdk.ui.home.meeting.MeetingActivity
import com.mindlinker.mlsdk.utils.NetworkUtil
import com.mindlinker.mlsdk.utils.ShapeSelector
import com.mindlinker.sdk.api.MLApi
import com.mindlinker.sdk.base.BaseActivity
import com.mindlinker.sdk.model.app.MLOptions
import com.mindlinker.sdk.model.dialog.DialogInfo
import com.mindlinker.sdk.model.meeting.MLRoomInfo
import com.mindlinker.sdk.model.user.LeaveType
import com.mindlinker.sdk.ui.dialog.customdialog.OneButtonDialog
import com.mindlinker.sdk.ui.dialog.customdialog.TwoButtonDialog
import com.mindlinker.sdk.utils.* // ktlint-disable no-wildcard-imports
import kotlinx.android.synthetic.main.activity_main.*
import java.util.* // ktlint-disable no-wildcard-imports

class HomeActivity : BaseActivity(), IHomeView {

    private val ACC_ID = "ACC_ID"
    private var mPresenter: IHomePresenter? = null
    private var mUserId: String = ""
    private var sAccessToken = ""
    private var isLogin = false
    private var isInit = false
    private var isVertify = false
    private var mOneButtonDialog: OneButtonDialog? = null
    private var mTwoButtonDialog: TwoButtonDialog? = null
    private var meetingNo: String = ""
    private var accId: String = ""

    private var isShow = false

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        if (!isTaskRoot) {
            if (intent != null) {
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                    finish()
                    return
                }
            }
        }
        initMaxME(this.application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAppPermissions()
        }
        mPresenter = HomePresenter()
        mPresenter?.init(this)
        mPresenter?.setView(this)
        val shapeSelector = ShapeSelector()
        val bg = shapeSelector.setDefaultBgColor(Color.parseColor("#0B7BFF"))
            .setCornerRadius(8).create()

        initSdkBtn.background = bg
        loginBtn.background = bg
        vertifyBtn.background = bg
        nextBtn.background = bg
        callBtn.background = bg
        acceptBtn.background = bg
        cancelBtn.background = bg

        edPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
        ivSee.setImageResource(R.drawable.ic_show_pwd_icon_hov)

        initSdkBtn.visibility = View.INVISIBLE

        initSdkBtn.setOnClickListener {
            mPresenter?.init(this)
            showToast("??????sdk??????")
            mPresenter?.initAuthor()
            isInit = true
        }
        loginBtn.setOnClickListener {
            val userName = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()
            if (userName.isEmpty()) {
                showToast("??????????????????")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showToast("???????????????")
                return@setOnClickListener
            }
            if (!NetworkUtil.isConnected(this)) {
                showToast(resources.getString(R.string.ml_network_fail))
                return@setOnClickListener
            }
            showLoading(true)
            mPresenter?.accountLogin(userName, password)
        }

        vertifyBtn.visibility = View.INVISIBLE
        vertifyBtn.setOnClickListener {
            if (!isLogin) {
                showToast("????????????")
                return@setOnClickListener
            }
            val nickname = if (edNickname.text.toString().isEmpty()) {
                resources.getString(R.string.app_name)
            } else {
                edNickname.text.toString()
            }
            mPresenter?.authenticate(sAccessToken, nickname)
            showToast("????????????")
        }

        nextBtn.visibility = View.INVISIBLE
        nextBtn.setOnClickListener {
            if (!isInit) {
                showToast("????????????sdk")
                return@setOnClickListener
            }
            if (!isLogin) {
                showToast("????????????")
                return@setOnClickListener
            }
            if (!isVertify) {
                showToast("????????????token")
                return@setOnClickListener
            }
            container?.isVisible = false
            containerSuccess?.isVisible = true
        }

        createMeeting.setOnClickListener {
            if (!NetworkUtil.isConnected(this)) {
                showToast(resources.getString(R.string.ml_network_fail))
                return@setOnClickListener
            }
            showLoading(true)
            mPresenter?.createMeeting(edUsername.text.toString().trim(), isMuteVideo = true, isMuteAudio = true)
        }
        joinMeeting.setOnClickListener {
            val intent = Intent(this, JoinMeetingActivity::class.java)
            intent.putExtra("NAME", accId)
            startActivity(intent)
        }

        acceptBtn.setOnClickListener {
            callConstraintLayout.isVisible = false
            mPresenter?.responseCall()
        }

        cancelBtn.setOnClickListener {
            callConstraintLayout.isVisible = false
            mPresenter?.cancelCallRequest()
        }

        ivSee.setOnClickListener {
            if (isShow) {
                edPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivSee.setImageResource(R.drawable.ic_show_pwd_icon_hov)
            } else {
                edPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivSee.setImageResource(R.drawable.ic_hide_pwd_icon_hov)
            }
            isShow = !isShow
        }

        tvAgain.background = bg
        tvAgain.setOnClickListener {
            llError.visibility = View.GONE
            flLoading.visibility = View.VISIBLE
            tvAgain.postDelayed({
                deviceLogin()
            }, 1000)
        }
        tvAgain.postDelayed({
            deviceLogin()
        }, 500)

        parseDateToYearMonthDayWeek()
    }

    private fun deviceLogin() {
        if (NetworkUtil.isConnected(this)) {
            llError.visibility = View.GONE
            flLoading.visibility = View.VISIBLE
            accId = PreferenceUtils.getString(ACC_ID, "")
            if (accId.isEmpty()) {
                accId =
                    Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
            }
            if (accId.isEmpty()) {
                // ????????????????????????????????????????????????????????????????????????????????????
                accId = System.currentTimeMillis().toString()
            }
            mPresenter?.deviceLogin(accId)
        } else {
            llError.visibility = View.VISIBLE
            text.text = "??????????????????"
            tvAgain.text = "????????????"
        }
    }

    override fun joinMeeting(meetingNo: String) {
    }

    override fun createAndJoinMeetingSuccess(info: MLRoomInfo) {
        meetingNo = info.roomNo
        MLApi.setLeaveCallBack {
            when (it) {
                LeaveType.TYPE_KICK_OUT -> {
                    showOneButtonDialog(getString(R.string.remove_by_host))
//                    CustomToast.show(this, getString(R.string.remove_by_host), CustomToast.ToastPosition.CENTER)
                }
                LeaveType.TYPE_DISCONNECT -> {
                    showTwoButtonDialog(getString(R.string.video_login_out))
//                    CustomToast.show(this, getString(R.string.video_login_out), CustomToast.ToastPosition.CENTER)
                }
            }
        }
        MeetingActivity.startMeetingActivity(this)
    }

    override fun showOneButtonDialog(subText: String) {
        runOnUiThread {
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

    override fun showTwoButtonDialog(subText: String, roomCode: String, sessionId: String) {
        runOnUiThread() {
//            dialogInfo.hideDialog = DialogInfo.DialogType.UNSPECIFIED
            mTwoButtonDialog?.dismiss()
            val cancelButtonText = if (sessionId.isEmpty()) {
                getString(R.string.cancel_rejoin)
            } else {
                getString(R.string.end_first)
            }
            mTwoButtonDialog = TwoButtonDialog(
                this,
                "",
                subText,
                DialogInfo.DialogType.OPEN_MIC,
                confirmButtonText = getString(R.string.confirm_rejoin),
                cancelButtonText = cancelButtonText,
                onConfirmCallback = {
                    showLoading(true)
                    if (sessionId.isEmpty()) {
                        mPresenter?.joinMeeting(
                            meetingNo, true,
                            isMuteAudio = true,
                            password = ""
                        )
                    } else {
                        mPresenter?.joinMeeting(
                            roomCode, true,
                            isMuteAudio = true,
                            password = ""
                        )
                    }
                    mTwoButtonDialog?.dismiss()
                    mTwoButtonDialog = null
                },
                onCancelCallback = {
                    showLoading(true)
                    if (sessionId.isNotEmpty()) {
                        mPresenter?.dismissMeeting(sessionId)
                    }
                    mTwoButtonDialog?.dismiss()
                    mTwoButtonDialog = null
                }
            )
            mTwoButtonDialog?.show()
        }
    }

    override fun showLoading(isShow: Boolean) {
        runOnUiThread {
            flLoading.visibility = if (isShow) View.VISIBLE else View.GONE
        }
    }

    private fun requestAppPermissions() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!PermissionUtil.hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    override fun showToast(string: String) {
        runOnUiThread {
            CustomToast.show(this, string, CustomToast.ToastPosition.CENTER)
        }
    }

    override fun loginResult(accessToken: AccessToken?) {
        showLoading(false)
        if (accessToken == null) {
            showToast(resources.getString(R.string.ml_request_fail))
            return
        }
        sAccessToken = accessToken.access_token
        mUserId = accessToken.userId
        isLogin = true

        mPresenter?.authenticate(sAccessToken, accId)

        KeyboardUtils.hideKeyboard(edNickname)
        container?.isVisible = false
        containerSuccess?.isVisible = true
    }

    override fun deviceLoginResult(accessToken: DeviceAccessToken?) {
        showLoading(false)
        if (accessToken == null) {
            llError.visibility = View.VISIBLE
            text.text = "????????????"
            tvAgain.text = "????????????"
            return
        }
        sAccessToken = accessToken.access_token
        mUserId = accessToken.userId
        PreferenceUtils.setString(ACC_ID, accessToken.accid)
        accId = accessToken.accid
        isLogin = true

        mPresenter?.authenticate(sAccessToken, accId)
        containerSuccess?.isVisible = true
        flLoading.setBackgroundColor(Color.parseColor("#66666666"))
    }

    override fun authenticateSuccess() {
        isVertify = true
    }

    override fun callComming(isCall: Boolean, name: String, meetingId: String) {
        runOnUiThread {
            callConstraintLayout.isVisible = isCall
            tvCall.text = "???????????? $name ???????????????????????? $meetingId"
        }
    }

    override fun onDestroy() {
        mPresenter?.release()
        clearDialog()
        super.onDestroy()
    }

    private fun clearDialog() {
        mOneButtonDialog?.dismiss()
        mOneButtonDialog = null
        mTwoButtonDialog?.dismiss()
        mTwoButtonDialog = null
    }

    private fun initMaxME(context: Application) {
        val options = MLOptions()
        options.logPath = "/sdcard/UI_SDK"
        options.enableLog = true
        options.enableConsoleLog = true
        MLApi.init(context, options)
    }

    /**???????????????????????????????????? */
    @SuppressLint("SetTextI18n")
    private fun parseDateToYearMonthDayWeek() {
        // ???????????????????????????????????????????????????????????????
        val calendar: Calendar = Calendar.getInstance() // ????????????
        val date = Date(System.currentTimeMillis())
        calendar.time = date // ??????????????????
        val yearStr: String = calendar.get(Calendar.YEAR).toString() + "" // ????????????
        val month: Int = calendar.get(Calendar.MONTH) + 1 // ????????????
        val monthStr = if (month < 10) "0$month" else month.toString() + ""
        val day: Int = calendar.get(Calendar.DATE) // ?????????
        val dayStr = if (day < 10) "0$day" else day.toString() + ""
        val week: Int = calendar.get(Calendar.DAY_OF_WEEK)
        var weekStr = ""
        when (week) {
            1 -> weekStr = "??????"
            2 -> weekStr = "??????"
            3 -> weekStr = "??????"
            4 -> weekStr = "??????"
            5 -> weekStr = "??????"
            6 -> weekStr = "??????"
            7 -> weekStr = "??????"
            else -> {
            }
        }
        tvYear.text = "${yearStr}???${monthStr}???"
        tvDay.text = dayStr
        tvWeek.text = weekStr
    }
}
