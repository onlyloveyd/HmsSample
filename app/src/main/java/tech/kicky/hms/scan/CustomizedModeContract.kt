package tech.kicky.hms.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.huawei.hms.ml.scan.HmsScan

/**
 * 跳转到自定义相机协定
 * author: yidong
 * 2021-12-02
 */
class CustomizedModeContract : ActivityResultContract<String, HmsScan>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, CustomizedModeActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): HmsScan? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getParcelableExtra(EXTRA_OUT)
        } else null
    }

    companion object {
        const val EXTRA_IN = ""
        const val EXTRA_OUT = "scanResult"
    }
}