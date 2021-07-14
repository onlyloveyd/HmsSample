package tech.kicky.hms.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import tech.kicky.hms.helper.TextAdapter
import tech.kicky.hms.scan.databinding.FragmentCameraKitBinding

/**
 * CameraKit
 * author: yidong
 * 2021-07-10
 */
class CameraKitFragment : Fragment() {

    private val viewModel: CameraKitViewModel by viewModels()

    private val mBinding by lazy {
        FragmentCameraKitBinding.inflate(layoutInflater)
    }

    private val menu = arrayOf(
        "HDR模式" to "",
        "超级夜景模式" to "",
        "大光圈模式" to "",
        "录像模式" to "",
        "人像模式" to "",
        "拍照模式" to "",
        "超级慢动作模式" to "",
        "慢动作模式" to "",
        "专业拍照模式" to "",
        "专业录像模式" to "",
        " 双景模式" to ""
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TextAdapter {
            doAction(it)
        }
        adapter.setData(menu.map {
            it.first
        })
        mBinding.list.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun doAction(index: Int) {
        Toast.makeText(requireContext(), "Todo", Toast.LENGTH_SHORT).show()
    }
}