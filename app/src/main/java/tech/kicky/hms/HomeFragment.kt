package tech.kicky.hms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

/**
 * Home Fragment
 * author: yidong
 * 2021-07-10
 */
class HomeFragment : Fragment() {
    private val menu = arrayOf(
        "ScanKit" to HomeFragmentDirections.actionHomeFragmentToScanKitFragment(),
        "Color Filter" to HomeFragmentDirections.actionHomeFragmentToColorFilterFragment()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Menu()
            }
        }
    }

    @Preview
    @Composable
    fun PreviewMenu() {
        MaterialTheme {
            Menu()
        }
    }

    @Composable
    private fun Menu() {
        LazyColumn {
            items(menu.size) { index ->
                Text(
                    text = menu[index].first,
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp, 16.dp)
                        .fillParentMaxWidth(1.0F)
                        .clickable {
                            doRouter(menu[index].second)
                        }
                )
                Divider(modifier = Modifier.padding(8.dp))
            }
        }
    }

    private fun doRouter(second: NavDirections) {
        findNavController().navigate(second)
    }

}