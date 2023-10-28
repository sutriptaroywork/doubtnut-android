package com.doubtnutapp.shorts

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.utils.*
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.databinding.ItemBioClassFlexBinding
import com.doubtnutapp.databinding.ShortsCategoryBottomSheetBinding
import com.doubtnutapp.hide
import com.doubtnutapp.shorts.model.CategoryData
import com.doubtnutapp.shorts.model.ShortsCategoryData
import com.doubtnutapp.shorts.viewmodel.ShortsCategoryBottomSheetVM
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ShortsCategoryBottomSheet :
    BaseBindingBottomSheetDialogFragment<ShortsCategoryBottomSheetVM, ShortsCategoryBottomSheetBinding>() {

    private var inflatedListener: InflatedListener? = null

    companion object {
        const val TAG = "ShortsCategorySheet"

        fun newInstance(
        ) =
            ShortsCategoryBottomSheet()
    }

    private var minimumCategories = 3
    private var categoriesSelected: Int = 0
    private var selectedCategories = mutableListOf<CategoryData>()
    private var backPressed = 0

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ShortsCategoryBottomSheetBinding {
        return ShortsCategoryBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun providePageName() = TAG

    override fun provideViewModel(): ShortsCategoryBottomSheetVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val behaviour = BottomSheetBehavior.from(binding.root.parent as View)
        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        binding.root.minHeight = Resources.getSystem().displayMetrics.heightPixels

        viewModel.getCategoryBottomSheet()
        viewModel.categoryData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    toast(it.message.toString())
                    dialog?.dismiss()
                }
                is Resource.Success -> {
                    hideProgressBar()
                    val data = it.data
                    if (data != null) {
                        if (data.categories?.size!! <= 10) {
                            behaviour.isFitToContents = false
                            behaviour.expandedOffset = 900
                            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        minimumCategories = data.minimumCategories ?: minimumCategories
                        binding.title.text = data.title.orEmpty()
                        binding.title.applyTextColor(data.titleColor)
                        binding.title.applyTextSize(data.titleSize)

                        binding.subTitle.text = data.subtitle.orEmpty()
                        binding.subTitle.applyTextColor(data.subtitleColor)
                        binding.subTitle.applyTextSize(data.subtitleSize)

                        binding.ivClose.loadImage2(data.closeIconUrl)

                        displayCategories(data)

                        binding.ivClose.setOnClickListener {
                            dialog?.dismiss()
                        }
                        binding.btSave.setOnClickListener {
                            sendCategoryData()
                        }
                    }
                }
            }
        }
        viewModel.sendCategoryData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    toast(R.string.somethingWentWrong)
                    dialog?.dismiss()
                }
                is Resource.Success -> {
                    hideProgressBar()
                    dialog?.dismiss()
                }
            }
        }

        dialog?.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {
                if (backPressed == 1) {
                    viewModel.sendCategoriesData(emptyList())
                    activity?.finish()
                    return@setOnKeyListener true
                } else {
                    backPressed++
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }
    }

    private fun showProgressBar() {
        binding.progressBar.show()
    }

    private fun hideProgressBar() {
        binding.progressBar.hide()
    }

    private fun displayCategories(data: ShortsCategoryData) {
        binding.flexLayout.removeAllViews()
        val lastIndex = data.categories?.lastIndex
        data.categories?.forEachIndexed { index, category ->
            val itemBioClassFlexBinding =
                ItemBioClassFlexBinding.inflate(LayoutInflater.from(context))

            itemBioClassFlexBinding.apply {
                val layParam = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                root.layoutParams = layParam

                val layoutParams =
                    root.layoutParams as FlexboxLayout.LayoutParams
                layoutParams.setMargins(32, 40, 0, 0)

                root.layoutParams = layoutParams
                bioClassText.text = category.value.toString()
                bioClassText.applyTextColor(data.categoryColor)
                bioClassText.applyTextSize(data.categorySize)
                bioClassText.setPadding(8, 8, 8, 8)
                if (index == lastIndex)
                    root.setMargins(32, 40, 0, 40)

                if (data.isCategoryBold == true)
                    bioClassText.setTypeface(bioClassText.typeface, Typeface.BOLD)

                if (category.isSelected == true) {
                    root.strokeColor =
                        requireContext().getColorRes(R.color.mock_test_button)
                    categoriesSelected++
                    selectedCategories.add(category)
                }

                binding.flexLayout.setPadding(0, 0, 0, 16)
                binding.flexLayout.addView(root)

                root.setOnClickListener {
                    if (category.isMutable == true) {
                        category.isSelected = category.isSelected?.not()
                        if (category.isSelected == true) {
                            root.strokeColor =
                                requireContext().getColorRes(R.color.mock_test_button)
                            categoriesSelected++
                            selectedCategories.add(category)
                        } else {
                            root.strokeColor =
                                requireContext().getColorRes(R.color.colorTransparent)
                            categoriesSelected--
                            selectedCategories.remove(category)
                        }
                    }
                }
            }
            binding.btSave.show()
        }
        inflatedListener?.onInflated(true)
    }

    fun shortsCategoryBottomSheetListener(inflatedListener: InflatedListener) {
        this.inflatedListener = inflatedListener
    }

    private fun sendCategoryData() {
        if (categoriesSelected >= minimumCategories) {
            val list = selectedCategories.map { it.id ?: 0 }
            viewModel.sendCategoriesData(list)
        } else {
            toast("Please select the required number of categories")
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        inflatedListener?.onInflated(false)
    }
}

interface InflatedListener {
    fun onInflated(inflated: Boolean)
}