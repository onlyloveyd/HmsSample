package tech.kicky.hms.ml

import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.rememberImagePainter
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput
import tech.kicky.hms.helper.GlideEngine
import tech.kicky.hms.scan.BuildConfig
import java.util.*

/**
 * 文档矫正
 * author: yidong
 * 2021-12-03
 */
class DocumentSkewCorrectionFragment : Fragment() {

    private val setting: MLDocumentSkewCorrectionAnalyzerSetting =
        MLDocumentSkewCorrectionAnalyzerSetting.Factory()
            .create()
    val analyzer: MLDocumentSkewCorrectionAnalyzer =
        MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
            .getDocumentSkewCorrectionAnalyzer(setting)

    private val viewModel: MLViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Screen()
            }
        }
    }

    @Composable
    private fun Screen() {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    pickImage()
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {
            Image(
                painter = rememberImagePainter(viewModel.pickBitmap),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(1.0f)
                    .padding(12.dp),
                contentScale = ContentScale.Inside
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewScreen() {

    }

    private fun pickImage() {
        EasyPhotos.createAlbum(
            this, true, false,
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
                        viewModel.pickBitmap = bitmap
                        val mlFrame = MLFrame.fromBitmap(bitmap)
                        val detectTask = analyzer.asyncDocumentSkewDetect(mlFrame)
                        detectTask.addOnSuccessListener { detectResult ->
                            val leftTop = detectResult.leftTopPosition
                            val rightTop = detectResult.rightTopPosition
                            val leftBottom = detectResult.leftBottomPosition
                            val rightBottom = detectResult.rightBottomPosition
                            val coordinates: MutableList<Point> = ArrayList()
                            coordinates.add(leftTop)
                            coordinates.add(rightTop)
                            coordinates.add(rightBottom)
                            coordinates.add(leftBottom)
                            val coordinateData =
                                MLDocumentSkewCorrectionCoordinateInput(coordinates)
                            val correctionTask =
                                analyzer.asyncDocumentSkewCorrect(mlFrame, coordinateData)
                            correctionTask.addOnSuccessListener {
                                //  校正成功
                                viewModel.pickBitmap = it.corrected
                            }.addOnFailureListener {
                                //  校正失败
                                Toast.makeText(context, "校正失败", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            // 检测失败。
                            Toast.makeText(context, "检测失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancel() {
                    Toast.makeText(
                        requireContext(),
                        "图片选取失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    override fun onDestroy() {
        super.onDestroy()
        analyzer.stop()
    }
}