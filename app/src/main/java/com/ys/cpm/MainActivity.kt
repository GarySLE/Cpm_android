package com.ys.cpm

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ys.cpm.databinding.ActivityMainBinding
import com.ys.cpm.vm.MainViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmField
        val REQUEST_MEDIA_FILE = 10
        @JvmField
        val DEFAULT_PLAYLIST = "default_playlist"
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.viewModel = MainViewModel(this, binding)
        binding.vm = this.viewModel
        setSupportActionBar(binding.toolbar)

        this.viewModel.initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            when (requestCode) {
                REQUEST_MEDIA_FILE -> {
                    this.viewModel.addMediaFile(data)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
