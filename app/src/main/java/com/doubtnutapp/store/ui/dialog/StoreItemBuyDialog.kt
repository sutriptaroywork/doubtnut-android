package com.doubtnutapp.store.ui.dialog

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R

import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.store.viewmodel.StoreItemBuyViewModel
import com.doubtnutapp.toBundle
import dagger.android.support.DaggerDialogFragment
import io.reactivex.annotations.NonNull
import javax.inject.Inject


class StoreItemBuyDialog : DaggerDialogFragment() {

    private var itemImageUrl: String? = null
    private var redeemStatus: Int = -999
    private var resourceId: Int = -999
    private var resourceType: String = ""
    private var isLast: Int = 0
    private var title: String = ""

    private lateinit var itemImage: ImageView
    private lateinit var openButton: AppCompatButton

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: StoreItemBuyViewModel

    @Inject
    lateinit var screenNavigator: Navigator

    companion object {
        const val TAG = "StoreItemDialogScreen"
        const val RESOURCE_ID = "resource_id"
        const val RESOURCE_TYPE = "resource_type"
        const val TITLE = "title"
        const val IMG_URL = "img_url"
        const val REDEEM_STATUS = "redeem_status"
        const val ITEM_ID = "item_id"
        const val PRICE = "price"
        const val IS_LAST = "is_last"

        fun newInstance(resourceId: Int, resourceType: String, title: String,
                        itemImageUrl: String = "", redeemStatus: Int, itemId: Int, price: Int, isLast: Int): StoreItemBuyDialog =
            StoreItemBuyDialog().apply {
                val bundle = Bundle()
                bundle.putInt(RESOURCE_ID, resourceId)
                bundle.putString(RESOURCE_TYPE, resourceType)
                bundle.putString(TITLE, title)
                bundle.putString(IMG_URL, itemImageUrl)
                bundle.putInt(REDEEM_STATUS, redeemStatus)
                bundle.putInt(ITEM_ID, itemId)
                bundle.putInt(PRICE, price)
                bundle.putInt(IS_LAST, isLast)
                arguments = bundle
            }
    }

    private fun updateUi() {
        Glide.with(activity!!)
                .load(itemImageUrl)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_profilefragment_profileplaceholder).error(R.drawable.ic_store_item_cash))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false

                    }
                })
                .into(itemImage)
    }

    private fun extractIntentParams() {
        arguments?.let {
            itemImageUrl = it.getString(IMG_URL, "")
            redeemStatus = it.getInt(REDEEM_STATUS, -999)
            title = it.getString(TITLE, "")
            resourceId = it.getInt(RESOURCE_ID, -999)
            resourceType = it.getString(RESOURCE_TYPE, "")
            isLast = it.getInt(IS_LAST, -999)
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_store_item_buy, null)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StoreItemBuyViewModel::class.java)

        itemImage = view.findViewById(R.id.itemImage)
        openButton = view.findViewById(R.id.openButton)

        extractIntentParams()
        updateUi()

        builder.setView(view)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)

        viewModel.publishTabSelectedEvent(EventConstants.EVENT_NAME_BUY_STORE_ITEM + title + "CongratsPopupView")

        setUpObservers()

        return builder.create()
    }

    private fun setUpObservers() {
        viewModel.navigateLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { navigationData ->
                navigate(navigationData)
            }
        })
    }

    private fun navigate(navigationData: NavigationModel) {
        val screen = navigationData.screen
        val arg = navigationData.hashMap?.toBundle()
        screenNavigator.startActivityFromActivity(activity!!, screen, arg)
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<Button>(R.id.openButton).setOnClickListener {
            viewModel.publishTabSelectedEvent(EventConstants.EVENT_NAME_OPEN_STORE_ITEM + title + "OpenNow")
            viewModel.handleStoreItemClick(getAction(resourceType, isLast, resourceId, title))
            dialog?.dismiss()
        }

        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun getAction(resourceType: String, isLast: Int, resourceId: Int, title: String) : Any {
        return when(resourceType.toLowerCase()) {

            "playlist" -> {
                if(isLast == 0)
                    OpenLibraryPlayListActivity(resourceId.toString(), title)
                else
                    OpenLibraryVideoPlayListScreen(resourceId.toString(), title!!)
            }

            else -> throw IllegalArgumentException("Wrong type for search playlist")
        }
    }
}
