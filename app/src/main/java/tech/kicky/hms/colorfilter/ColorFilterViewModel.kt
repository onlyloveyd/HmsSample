package tech.kicky.hms.colorfilter

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ColorFilterViewModel : ViewModel() {
    var showBitmap by mutableStateOf<Bitmap?>(null)
    var sourceBitmap by mutableStateOf<Bitmap?>(null)
    var filter by mutableStateOf<Filter?>(null)
}