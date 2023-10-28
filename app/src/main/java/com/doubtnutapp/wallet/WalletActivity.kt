package com.doubtnutapp.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.AddMoneyClicked
import com.doubtnutapp.paymentplan.ui.PaymentPlanActivity
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 20/11/20.
 * Revamp by Akshat Jindal on 24/02/21
 */
class WalletActivity : AppCompatActivity(), HasAndroidInjector, ActionPerformer {

    companion object {
        const val WALLET = "WALLET"
        private const val TAG = "WalletActivity"
        fun getStartIntent(context: Context) = Intent(context, WalletActivity::class.java)
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private var amount: Int = 0

    private var walletFragment: WalletFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.white_20)
        setContentView(R.layout.activity_wallet)

        walletFragment = WalletFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .apply {
                add(R.id.fragmentContainer, walletFragment!!)
                commit()
            }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is WalletFragment) {
            fragment.setActionListener(this)
        }
    }

    override fun performAction(action: Any) {
        if (action is AddMoneyClicked) {
            this.amount = action.amount
            showCheckoutFragment()
        }
    }

    private fun showCheckoutFragment() {
        PaymentPlanActivity.getStartIntent(
            this,
            CheckoutFragment.WALLET,
            "",
            "",
            amount.toString(),
            CheckoutFragment.WALLET
        ).apply {
            startActivity(this)
        }
    }

}
