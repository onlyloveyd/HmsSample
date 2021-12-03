package tech.kicky.hms.ml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

/**
 * 机器学习
 * author: yidong
 * 2021-12-03
 */
class MLFragment : Fragment() {

    private val menu = arrayOf(
        "文档校正" to MLFragmentDirections.actionMlToDocumentSkewCorrection(),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MLKitMenu()
            }
        }
    }

    private fun doRouter(second: NavDirections) {
        findNavController().navigate(second)
    }

    @Composable
    private fun MLKitMenu() {
        LazyColumn {
            items(menu.size) { index ->
                Column(modifier = Modifier
                    .clickable {
                        doRouter(menu[index].second)
                    }) {
                    Text(
                        text = menu[index].first,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(8.dp, 8.dp)
                            .fillMaxWidth(1.0f)
                    )
                    Divider(modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    @Preview
    @Composable
    private fun PreviewMLKitMenu() {
        MaterialTheme {
            MLKitMenu()
        }
    }


}