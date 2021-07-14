package tech.kicky.hms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.kicky.hms.scan.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "HmsSample"
    }

    private val mBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

    }


}