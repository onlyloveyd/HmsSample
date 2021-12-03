package tech.kicky.hms.scan

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import java.util.*

/**
 * Description
 * author: yidong
 * 2021-07-10
 */
class ScanKitFragment : Fragment() {

    private val sTag = "ScanKitSample"
    private val REQUEST_CODE_SCAN_DEFAULT_MODE = 0x01

    private val menu = arrayOf(
        "默认模式" to this::startDefaultMode,
        "自定义模式" to this::startCustomizedMode,
        "Bitmap模式" to this::startBitmapMode,
        "多线程模式" to this::startMultiProcessorMode
    )

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
        registerForActivityResult(CustomizedModeContract()) { hmsScan ->
            hmsScan?.getOriginalValue().let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ScanKitMenu()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
    }


    @Preview
    @Composable
    fun PreviewMenu() {
        MaterialTheme {
            ScanKitMenu()
        }
    }

    @Composable
    private fun ScanKitMenu() {
        LazyColumn {
            items(menu.size) { index ->
                Text(
                    text = menu[index].first,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(8.dp, 16.dp)
                        .fillParentMaxWidth(1.0F)
                        .clickable {
                            menu[index].second.invoke()
                        }
                )
                Divider(modifier = Modifier.padding(8.dp))
            }
        }
    }

    private fun requestPermission() {
        mPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_SCAN_DEFAULT_MODE -> {
                val hmsScan: HmsScan? = data.getParcelableExtra(ScanUtil.RESULT)
                hmsScan?.getOriginalValue().let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startDefaultMode() {
        val options =
            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        ScanUtil.startScan(
            requireActivity(), REQUEST_CODE_SCAN_DEFAULT_MODE,
            options
        )
    }

    private fun startCustomizedMode() {
        mCustomizedModeLauncher.launch(null)
    }

    private fun startBitmapMode() {
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
//                                mBinding.tvResult.text = result[0].getOriginalValue()
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

    private fun startMultiProcessorMode() {
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
//                                mBinding.tvResult.text = resultStr
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