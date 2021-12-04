package tech.kicky.hms.colorfilter

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVision.VisionCallBack
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.secure.android.common.util.LogsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import tech.kicky.hms.MainActivity.Companion.TAG
import tech.kicky.hms.helper.GlideEngine
import tech.kicky.hms.scan.BuildConfig
import java.util.*

/**
 * Color Filter 滤镜
 * author: yidong
 * 2021-07-10
 */
class ColorFilterFragment : Fragment() {

    private val viewModel: ColorFilterViewModel by viewModels()

    private var imageVisionFilterAPI: ImageVisionImpl? = null

    private var isInit = false

    @Composable
    private fun ColorFilterScreen() {
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        MaterialTheme {
            Scaffold(
                scaffoldState = scaffoldState,
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(onClick = { pickImage() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = viewModel.filter?.name ?: ""
                            )
                        },
                        navigationIcon = {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = null,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        scaffoldState.drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                })
                        }
                    )
                },
                drawerContent = {
                    LazyColumn {
                        items(LocalFilters.size) { index ->
                            Column(modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        scaffoldState.drawerState.apply {
                                            if (isOpen) close()
                                        }
                                    }
                                    viewModel.filter = LocalFilters[index]
                                    doFilter()
                                }) {
                                Text(
                                    text = LocalFilters[index].name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(8.dp, 16.dp)
                                        .fillParentMaxWidth(1.0F)
                                )
                                Divider(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            ) {
                Image(
                    painter = rememberImagePainter(viewModel.showBitmap),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(1.0f)
                        .padding(12.dp),
                    contentScale = ContentScale.Inside
                )
            }
        }
    }

    @Composable
    @Preview
    private fun PreviewColorFilterScreen() {
        MaterialTheme {
            ColorFilterScreen()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ColorFilterScreen()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initService()
    }

    private fun initService() {
        imageVisionFilterAPI = ImageVision.getInstance(requireActivity())
        imageVisionFilterAPI?.setVisionCallBack(object : VisionCallBack {
            override fun onSuccess(successCode: Int) {
                val initCode =
                    imageVisionFilterAPI?.init(requireActivity(), LocalAuthJson)
                isInit = initCode == 0
                Toast.makeText(
                    requireContext(),
                    if (initCode == 0) "ColorFilter Init Successfully" else "ColorFilter Init Failed\n, initCode: $initCode",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(errorCode: Int) {
                Toast.makeText(
                    requireContext(),
                    "setVisionCallBack Failed",
                    Toast.LENGTH_SHORT
                ).show()
                LogsUtil.e(
                    TAG,
                    "ImageVisionAPI fail, errorCode: $errorCode"
                )
            }
        })
    }

    private fun doFilter() {
        if (viewModel.sourceBitmap == null) {
            Toast.makeText(requireContext(), "请选择图片", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewModel.filter == null) {
            Toast.makeText(requireContext(), "请选择滤镜效果", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            try {
                // 滤镜强度，取值范围[0,1.0]，默认为1.0。
                taskJson.put("intensity", "1")
                // 颜色映射的图片索引，索引范围[0,24]（0为原图）
                taskJson.put("filterType", viewModel.filter?.code)
                // 压缩率，取值范围（0,1.0]，默认为1.0。
                taskJson.put("compressRate", "1")

                // 业务提供的请求ID(可选)
                jsonObject.put("requestId", "1")
                jsonObject.put("taskJson", taskJson)
                // 鉴权参数,与初始化过程中使用的相同
                jsonObject.put("authJson", LocalAuthJson)
                val visionResult = imageVisionFilterAPI?.getColorFilter(
                    jsonObject,
                    viewModel.sourceBitmap
                )
                withContext(Dispatchers.Main) {
                    if (visionResult?.resultCode == 0) {
                        viewModel.showBitmap = visionResult.image
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "resultCode: ${visionResult?.resultCode}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                LogsUtil.e(TAG, "visionResult: $visionResult")
            } catch (e: JSONException) {
                LogsUtil.e(TAG, "JSONException: " + e.message)
            }
        }
    }

    private fun pickImage() {
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
                        viewModel.sourceBitmap = bitmap
                        if (viewModel.filter != null) {
                            doFilter()
                        } else {
                            viewModel.showBitmap = bitmap
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

    private fun stopService() {
        imageVisionFilterAPI?.stop()
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }
}