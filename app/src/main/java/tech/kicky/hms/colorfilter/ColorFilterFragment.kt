package tech.kicky.hms.colorfilter

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
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
import tech.kicky.hms.helper.FilterAdapter
import tech.kicky.hms.scan.BuildConfig
import tech.kicky.hms.scan.GlideEngine
import tech.kicky.hms.scan.databinding.FragmentColorFilterBinding
import java.util.*

/**
 * Color Filter 滤镜
 * author: yidong
 * 2021-07-10
 */
class ColorFilterFragment : Fragment() {

    private val mBinding by lazy {
        FragmentColorFilterBinding.inflate(layoutInflater)
    }

    private var imageVisionFilterAPI: ImageVisionImpl? = null
    private lateinit var originBitmap: Bitmap
    private var filterIndex = 0

    private var isInit = false
        set(value) {
            field = value
            mBinding.get.isEnabled = true
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = mBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FilterAdapter {
            filterIndex = it
        }

        adapter.setData(LocalFilters)
        mBinding.filters.adapter = adapter
        mBinding.filters.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        mBinding.get.setOnClickListener {
            doFilter()
        }

        mBinding.picture.setOnClickListener {
            pickImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun stopService() {
        imageVisionFilterAPI?.stop()
    }

    private fun doFilter() {
        if (!this::originBitmap.isInitialized) {
            Toast.makeText(requireContext(), "Please Pick Image", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            try {
                taskJson.put("intensity", "1")
                taskJson.put("filterType", LocalFilters[filterIndex].code)
                taskJson.put("compressRate", "1")
                jsonObject.put("requestId", "1")
                jsonObject.put("taskJson", taskJson)
                jsonObject.put("authJson", LocalAuthJson)
                val visionResult = imageVisionFilterAPI?.getColorFilter(
                    jsonObject,
                    originBitmap
                )
                withContext(Dispatchers.Main) {
                    if (visionResult?.resultCode == 0) {
                        val image = visionResult.image
                        mBinding.picture.setImageBitmap(image)
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
                        mBinding.picture.setImageBitmap(bitmap)
                        originBitmap = Bitmap.createBitmap(bitmap)
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


    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}