package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.CloseEvent
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.R
import com.doubtnutapp.sticker.BaseActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_bundle.*

class BundleActivity : BaseActivity() {
    companion object {

        fun getStartIntent(context: Context, id: String, source: String?) =
            Intent(context, BundleActivity::class.java).apply {
                putExtra(Constants.ASSORTMENT_ID, id)
                putExtra(Constants.SOURCE, source)
            }
    }

    private var fragment: BundleFragmentV2? = null
    private var vipObserver: Disposable? = null
    private var closeObserver: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bundle)
        val id = intent.getStringExtra(Constants.ASSORTMENT_ID).orEmpty()
        val source = intent.getStringExtra(Constants.SOURCE)
        fragment = BundleFragmentV2.newInstance(id, source)
        fragment?.show(supportFragmentManager, "")
        supportFragmentManager.executePendingTransactions()
        fragment?.dialog?.setOnCancelListener {
            finish()
        }
        fragment?.dialog?.setOnDismissListener {
            finish()
        }
        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    finish()
                }
            }
        }

        closeObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is CloseEvent) {
                finish()
            }
        }

        parentContainer.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
        closeObserver?.dispose()
    }

}