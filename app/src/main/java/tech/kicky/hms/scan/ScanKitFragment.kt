package tech.kicky.hms.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.viewbinding.viewBinding
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
class ScanKitFragment : Fragment(R.layout.fragment_scan_kit) {

    private val sTag = "ScanKitSample"
    private val REQUEST_CODE_SCAN_DEFAULT_MODE = 0x01

    private val mBinding: FragmentScanKitBinding by viewBinding()
    private val mPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it?.let {
                if (it.containsValue(false)) {
                    Toast.makeText(context, "请给予相机和存储权限", Toast.LENGTH_SHORT).show()
                    childFragmentManager.popBackStack()
                }
            }
        }

    private val mCustomizedModeLauncher =
        registerForActivityResult(CustomizedModeContract()) {
            if (!TextUtils.isEmpty(it?.getOriginalValue())) {
                mBinding.tvResult.text = it?.getOriginalValue()
            }
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
        requestPermission()
    }

    private fun requestPermission() {
        mPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
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
        }
    }

    private fun startCustomizedMode() {
        mCustomizedModeLauncher.launch(null)
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
                                Log.d(sTag, it.message ?: "")
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
        }
    }
}