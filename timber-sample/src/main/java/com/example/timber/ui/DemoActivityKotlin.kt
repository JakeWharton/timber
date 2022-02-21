package com.example.timber.ui

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.timber.databinding.DemoActivityBinding
import timber.log.Timber
import timber.log.timberSource

class DemoActivityKotlin: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: DemoActivityBinding = DemoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //$timberSource to print clickable log location
        Timber.d("$timberSource Activity Created")

        binding.hello.setOnClickListener {
            Timber.d("$timberSource Click_Hello")
        }

    }

}