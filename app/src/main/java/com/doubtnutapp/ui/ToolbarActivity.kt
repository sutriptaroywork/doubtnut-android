package com.doubtnutapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity

@SuppressLint("Registered")
abstract class ToolbarActivity<VM : BaseViewModel, VB : ViewBinding> :
    BaseBindingActivity<VM, VB>() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val subscribe = (application as DoubtnutApp)
            .bus()?.toObservable()?.subscribe { `object` ->
                if (`object` is ApplicationStateEvent) {
                    isAppInForeground = `object`.state
                }
            }
    }

    protected var isAppInForeground: Boolean = true

    protected fun setToolbar(title: String = "") {
        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            if (title.isNotEmpty()) setTitle(title)
        }
        requireNotNull(toolbar) { "Toolbar with id R.id.toolbar not found" }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}