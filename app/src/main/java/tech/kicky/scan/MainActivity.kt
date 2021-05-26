package tech.kicky.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.core.util.valueIterator
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import tech.kicky.scan.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "ScanKitSample"
    private val CAMERA_REQ_CODE = 1000
    private val REQUEST_CODE_SCAN_DEFAULT_MODE = 0X01
    private val REQUEST_CODE_SCAN_CUSTOMIZED_MODE = 0X02

    private val mBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        //CAMERA_REQ_CODE为用户自定义，用于接收权限校验结果
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
            CAMERA_REQ_CODE
        )
    }

    //实现“onRequestPermissionsResult”函数接收校验权限结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //判断“requestCode”是否为申请权限时设置请求码CAMERA_REQ_CODE，然后校验权限开启状态
        if (requestCode == CAMERA_REQ_CODE && grantResults.size == 2
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_SCAN_DEFAULT_MODE -> {
                val hmsScan: HmsScan? = data.getParcelableExtra(ScanUtil.RESULT)
                if (!TextUtils.isEmpty(hmsScan?.getOriginalValue())) {
                    mBinding.tvResult.text = hmsScan?.getOriginalValue()
                }
            }

            REQUEST_CODE_SCAN_CUSTOMIZED_MODE -> {
                val hmsScan: HmsScan? = data.getParcelableExtra(CustomizedModeActivity.SCAN_RESULT)
                if (!TextUtils.isEmpty(hmsScan?.getOriginalValue())) {
                    mBinding.tvResult.text = hmsScan?.getOriginalValue()
                }
            }
        }
    }

    fun startDefaultMode(view: View) {
        val options =
            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        ScanUtil.startScan(
            this, REQUEST_CODE_SCAN_DEFAULT_MODE,
            options
        )
    }

    fun startCustomizedMode(view: View) {
        this.startActivityForResult(
            Intent(this, CustomizedModeActivity::class.java), REQUEST_CODE_SCAN_CUSTOMIZED_MODE
        )
    }

    fun startBitmapMode(view: View) {
        EasyPhotos.createAlbum(
            this, false, false,
            GlideEngine.getInstance()
        )
            .setFileProviderAuthority(BuildConfig.APPLICATION_ID)
            .setCount(1)
            .start(object : SelectCallback() {
                override fun onResult(photos: ArrayList<Photo>?, isOriginal: Boolean) {
                    photos?.let {
                        val path = photos.first().path
                        if (TextUtils.isEmpty(path)) {
                            return
                        }
                        // Obtain the bitmap from the image path.
                        val bitmap = ScanUtil.compressBitmap(this@MainActivity, path)
                        // Call the decodeWithBitmap method to pass the bitmap.
                        val options =
                            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE)
                                .setPhotoMode(false)
                                .create()
                        val result = ScanUtil.decodeWithBitmap(
                            this@MainActivity,
                            bitmap,
                            options
                        )
                        // Obtain the scanning result.
                        if (result != null && result.isNotEmpty()) {
                            if (!TextUtils.isEmpty(result[0].getOriginalValue())) {
                                mBinding.tvResult.text = result[0].getOriginalValue()
                            }
                        }
                    }
                }

                override fun onCancel() {
                    Toast.makeText(
                        this@MainActivity,
                        "图片选取失败",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            })
    }

    fun startMultiProcessorMode(view: View) {
        EasyPhotos.createAlbum(
            this, false, false,
            GlideEngine.getInstance()
        )
            .setFileProviderAuthority(BuildConfig.APPLICATION_ID)
            .setCount(1)
            .start(object : SelectCallback() {
                override fun onResult(photos: ArrayList<Photo>?, isOriginal: Boolean) {
                    photos?.let {
                        val path = photos.first().path
                        if (TextUtils.isEmpty(path)) {
                            return
                        }
                        // Obtain the bitmap from the image path.
                        val bitmap = ScanUtil.compressBitmap(this@MainActivity, path)

                        val options =
                            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(
                                HmsScan.QRCODE_SCAN_TYPE,
                                HmsScan.DATAMATRIX_SCAN_TYPE
                            )
                                .create()
                        val scanAnalyzer = HmsScanAnalyzer(options)
                        val image = MLFrame.fromBitmap(bitmap)
                        // 同步模式
                        val result: SparseArray<HmsScan> = scanAnalyzer.analyseFrame(image)
                        scanAnalyzer.analyzInAsyn(image).addOnSuccessListener {
                            if (it != null && it.size > 0) {
                                var resultStr = ""
                                it.forEach { value ->
                                    resultStr = resultStr.plus(value.originalValue).plus("\n")
                                }
                                mBinding.tvResult.text = resultStr
                            }
                        }.addOnFailureListener {
                            it?.printStackTrace()
                            Log.d(TAG, it.message ?: "")
                        }
                    }
                }

                override fun onCancel() {
                    Toast.makeText(
                        this@MainActivity,
                        "图片选取失败",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            })
    }
}