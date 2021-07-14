package tech.kicky.hms.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import tech.kicky.hms.scan.databinding.FragmentScanKitBinding
import java.util.*

/**
 * Description
 * author: yidong
 * 2021-07-10
 */
class ScanKitFragment : Fragment() {

    private val TAG = "ScanKitSample"
    private val CAMERA_REQ_CODE = 1000
    private val REQUEST_CODE_SCAN_DEFAULT_MODE = 0X01
    private val REQUEST_CODE_SCAN_CUSTOMIZED_MODE = 0X02

    private val mBinding by lazy {
        FragmentScanKitBinding.inflate(layoutInflater)
    }

    private val viewModel: ScanKitViewModel by viewModels()

    private var doNext: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btDefaultMode.setOnClickListener {
            startDefaultMode()
        }
        mBinding.btCustomizedViewMode.setOnClickListener {
            startCustomizedMode()
        }
        mBinding.btMultiprocessorMode.setOnClickListener {
            startMultiProcessorMode()
        }
        mBinding.btBitmapMode.setOnClickListener {
            startBitmapMode()
        }
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        //CAMERA_REQ_CODE为用户自定义，用于接收权限校验结果
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
            CAMERA_REQ_CODE
        )
    }

    private fun hasCameraPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasStoragePermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    //实现“onRequestPermissionsResult”函数接收校验权限结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //判断“requestCode”是否为申请权限时设置请求码CAMERA_REQ_CODE，然后校验权限开启状态
        if (!(requestCode == CAMERA_REQ_CODE && grantResults.size == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
        ) {
            doNext?.invoke()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK || data == null) {
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

    private fun startDefaultMode() {
        if (hasCameraPermission() && hasStoragePermission()) {
            val options =
                HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
            ScanUtil.startScan(
                requireActivity(), REQUEST_CODE_SCAN_DEFAULT_MODE,
                options
            )
        } else {
            this.doNext = this::startDefaultMode
            requestCameraPermission()
        }
    }

    private fun startCustomizedMode() {
        this.startActivityForResult(
            Intent(requireContext(), CustomizedModeActivity::class.java),
            REQUEST_CODE_SCAN_CUSTOMIZED_MODE
        )
    }

    private fun startBitmapMode() {
        if (hasStoragePermission()) {
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
                            val bitmap = ScanUtil.compressBitmap(requireActivity(), path)
                            // Call the decodeWithBitmap method to pass the bitmap.
                            val options =
                                HmsScanAnalyzerOptions.Creator()
                                    .setHmsScanTypes(HmsScan.ALL_SCAN_TYPE)
                                    .setPhotoMode(false)
                                    .create()
                            val result = ScanUtil.decodeWithBitmap(
                                requireActivity(),
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
                            requireContext(),
                            "图片选取失败",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                })
        } else {
            this.doNext = this::startBitmapMode
            requestCameraPermission()
        }
    }

    private fun startMultiProcessorMode() {
        if (hasStoragePermission() && hasCameraPermission()) {
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
                            val bitmap = ScanUtil.compressBitmap(requireActivity(), path)

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
                            requireContext(),
                            "图片选取失败",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                })
        } else {
            this.doNext = this::startMultiProcessorMode
            requestCameraPermission()
        }
    }
}