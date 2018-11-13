package com.example.timber.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import butterknife.ButterKnife
import butterknife.OnClick
import com.example.timber.R
import timber.log.Timber

import android.widget.Toast.LENGTH_SHORT
import timber.log.info

class DemoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_activity)
        ButterKnife.bind(this)
        Timber.info { "LifeCycles" }
        Timber.info { "Activity Created" }
    }

    @OnClick(R.id.hello, R.id.hey, R.id.hi)
    fun greetingClicked(button: Button) {
        Timber.info { "A button with ID ${button.id} was clicked to say '${button.text}'." }
        Toast.makeText(this, "Check logcat for a greeting!", LENGTH_SHORT).show()
    }
}
