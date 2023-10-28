package com.doubtnutapp.dummy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnutapp.R

class DummyActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DummyActivity"
        fun getStartIntent(context: Context) = Intent(context, DummyActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
    }
}
