package com.mindlinker.mlsdk.ui.home.meeting

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Looper
import androidx.fragment.app.Fragment
import com.mindlinker.mlsdk.R
import com.mindlinker.sdk.api.MLApi
import com.mindlinker.sdk.base.BaseActivity
import com.mindlinker.sdk.ui.fragment.MLFragment
import com.mindlinker.sdk.ui.meeting.video.IActivityDelegate
import io.reactivex.android.schedulers.AndroidSchedulers

class MeetingActivity : BaseActivity() {

    companion object {
        fun startMeetingActivity(context: Context) {
            val intent = Intent(context, MeetingActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mActivityDelegate: IActivityDelegate = object : IActivityDelegate {
        override fun finishActivity() {
            finish()
        }

        override fun applyPublishPermission() {
            val packageName = this@MeetingActivity.packageName
            MLApi.getProjectionIntent(
                this@MeetingActivity,
                this@MeetingActivity.packageManager.getApplicationInfo(packageName, 0).uid,
                packageName
            )
        }
    }

    private var fragment: Fragment? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_meeting
    }

    override fun initData() {
        fragment = MLFragment.newInstance()
        (fragment as MLFragment).setActivityDelegate(mActivityDelegate)
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment!!, "MLFragment").commit()
//        initAutoSizeConstant()
    }

//    private fun initAutoSizeConstant() {
//
//        if (MLActualPX.mActualPx24 <= 0) {
//            MLActualPX.mActualPx24 = AutoSizeUtils.dp2px(this@MeetingActivity, PXConstant.PX_24).toFloat()
//        }
//
//        if (MLActualPX.mActualPx274 <= 0) {
//            MLActualPX.mActualPx274 = AutoSizeUtils.dp2px(this@MeetingActivity, PXConstant.PX_274).toFloat()
//        }
//
//        if (MLActualPX.mActualPx366 <= 0) {
//            MLActualPX.mActualPx366 = AutoSizeUtils.dp2px(this@MeetingActivity, PXConstant.PX_366).toFloat()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MLApi.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun getResources(): Resources {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            MLApi.setAutoSize(super.getResources())
        } else {
            AndroidSchedulers.mainThread().scheduleDirect {
                MLApi.setAutoSize(super.getResources())
            }
        }
        return super.getResources()
    }

    override fun onBackPressed() {
        MLApi.onBackPressed()
    }
}
