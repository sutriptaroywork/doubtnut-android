package com.doubtnutapp.doubtplan

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.DialogVipPackageBinding
import com.doubtnutapp.domain.payment.entities.DoubtPackageInfo
import com.doubtnutapp.domain.payment.entities.PackageDesc
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.hide
import com.doubtnutapp.payment.ui.ChoosePlanAdapter
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 17/10/20.
 */
class DoubtPackageDialog :
    BaseBindingDialogFragment<DummyViewModel, DialogVipPackageBinding>(),
    ActionPerformer {

    companion object {
        const val TAG: String = "DoubtPackageDialog"
        private const val IS_DIALOG_CANCELABLE: String = "is_dialog_cancelable"
        private const val DESCRIPTION_ONE: String = "description_one"
        private const val DESCRIPTION_TWO: String = "description_two"
        private const val SUBSCRIPTION_STATUS: String = "subscription_status"
        private const val PACKAGE_DESC: String = "package_desc"
        fun newInstance(
            isDialogCancelable: Boolean,
            descriptionOne: String?,
            descriptionTwo: String?,
            packageDesc: PackageDesc?,
            subscriptionStatus: Boolean
        ): DoubtPackageDialog {
            return DoubtPackageDialog().apply {
                val desc = packageDesc?.run {
                    PackageDescView(
                        title,
                        packageInfoList?.map {
                            DoubtPackageInfoView(
                                id = it.id,
                                originalAmount = it.originalAmount, offerAmount = it.offerAmount, duration = it.duration,
                                off = it.off, selected = it.selected, variantId = it.variantId
                            )
                        }.orEmpty()
                    )
                }
                arguments = Bundle().apply {
                    putBoolean(IS_DIALOG_CANCELABLE, isDialogCancelable)
                    putString(DESCRIPTION_ONE, descriptionOne)
                    putString(DESCRIPTION_TWO, descriptionTwo)
                    putBoolean(SUBSCRIPTION_STATUS, subscriptionStatus)
                    putParcelable(PACKAGE_DESC, desc)
                }
            }
        }
    }

    private var isDialogCancelable: Boolean = false
    private var subscriptionStatus: Boolean = false

    private var descriptionOne: String = ""
    private var descriptionTwo: String = ""

    private var packageDescView: PackageDescView? = null

    val choosePlanAdapter: ChoosePlanAdapter by lazy { ChoosePlanAdapter(this) }

    val layoutManagerDialog: LinearLayoutManager by lazy {
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUp()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun setUp() {
        packageDescView = arguments?.getParcelable(PACKAGE_DESC)
        descriptionOne = arguments?.getString(DESCRIPTION_ONE).orEmpty()
        descriptionTwo = arguments?.getString(DESCRIPTION_TWO).orEmpty()
        isDialogCancelable = arguments?.getBoolean(IS_DIALOG_CANCELABLE, false) ?: false
        subscriptionStatus = arguments?.getBoolean(SUBSCRIPTION_STATUS, false) ?: false

        mBinding?.imageViewClose?.setOnClickListener {
            dismiss()
        }

        if (descriptionOne.isBlank()) {
            mBinding?.textViewDescriptionOne?.hide()
        } else {
            mBinding?.textViewDescriptionOne?.show()
            mBinding?.textViewDescriptionOne?.text = descriptionOne
        }

        if (descriptionTwo.isBlank()) {
            mBinding?.textViewDescriptionTwo?.hide()
        } else {
            mBinding?.textViewDescriptionTwo?.show()
            mBinding?.textViewDescriptionTwo?.text = descriptionTwo
        }

        // imageView.loadImageEtx(imageUrl)

        if (!packageDescView?.packageInfoList.isNullOrEmpty()) {
            mBinding?.recyclerViewPaymentDialog?.layoutManager = layoutManagerDialog
            mBinding?.recyclerViewPaymentDialog?.adapter = choosePlanAdapter
            choosePlanAdapter.updateList(
                packageDescView?.packageInfoList?.map {
                    DoubtPackageInfo(it.id, it.originalAmount, it.offerAmount, it.duration, it.off, it.selected, it.variantId)
                }.orEmpty().toMutableList()
            )
        } else {
            mBinding?.recyclerViewPaymentDialog?.hide()
        }

        if (!packageDescView?.title.isNullOrBlank()) {
            mBinding?.textViewPaymentTitle?.text = packageDescView?.title.orEmpty()
        } else {
            mBinding?.textViewPaymentTitle?.hide()
        }

        mBinding?.buttonContinue?.setOnClickListener {
            startActivity(DoubtPackageActivity.getStartIntent(context.forceUnWrap()))
        }

        if (subscriptionStatus) {
            mBinding?.buttonContinue?.text = "RENEW NOW"
        } else {
            mBinding?.buttonContinue?.text = "BUY NOW"
        }

        mBinding?.imageViewClose?.setOnClickListener {
            if (isDialogCancelable) {
                dismiss()
            } else {
                activity?.finish()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isDialogCancelable) {
            activity?.finish()
        }
    }

    override fun performAction(action: Any) {
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogVipPackageBinding {
        return DialogVipPackageBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}

@Keep
@Parcelize
data class PackageDescView(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val packageInfoList: List<DoubtPackageInfoView>?
) : Parcelable

@Keep
@Parcelize
data class DoubtPackageInfoView(
    @SerializedName("id") val id: String?,
    @SerializedName("original_amount") val originalAmount: String?,
    @SerializedName("offer_amount") val offerAmount: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("off") val off: String?,
    @SerializedName("selected") var selected: Boolean?,
    @SerializedName("variant_id") val variantId: String?
) : Parcelable
