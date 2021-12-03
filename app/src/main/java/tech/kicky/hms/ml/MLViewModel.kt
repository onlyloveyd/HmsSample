package tech.kicky.hms.ml

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * MLKit ViewModel
 * author: yidong
 * 2021-12-03
 */
class MLViewModel : ViewModel() {
    var pickBitmap by mutableStateOf<Bitmap?>(null)
    var message by mutableStateOf<String>("")
}