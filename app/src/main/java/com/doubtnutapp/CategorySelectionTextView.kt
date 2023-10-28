package com.doubtnutapp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.databinding.ViewCategorySelectionTextBinding
import com.doubtnutapp.widgets.MultiSelectFilterWidgetV2Item

class CategorySelectionTextView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    ConstraintLayout(context, attrs) {

    @Suppress("JoinDeclarationAndAssignment")
    private val binding: ViewCategorySelectionTextBinding

    init {
        binding = ViewCategorySelectionTextBinding.inflate(LayoutInflater.from(context), this, true)

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CategorySelectionTextView)
        val backgroundTint = typeArray.getResourceId(
            R.styleable.CategorySelectionTextView_cstv_background_tint,
            R.color.colorLightGray
        )

        setBackgroundTint(backgroundTint)

        typeArray.getText(R.styleable.CategorySelectionTextView_cstv_text)?.let {
            setText(it as String)
        }

        val isMultiSelect =
            typeArray.getBoolean(R.styleable.CategorySelectionTextView_cstv_is_multiselect, false)
        val isSelected =
            typeArray.getBoolean(R.styleable.CategorySelectionTextView_cstv_is_selected, false)
        val selectedItemCount =
            typeArray.getInt(R.styleable.CategorySelectionTextView_cstv_selected_item_count, 0)

        updateSelection(isMultiSelect, isSelected, selectedItemCount)
        typeArray.recycle()
    }

    fun setBackgroundTint(@ColorRes backgroundTint: Int) {
        binding.root.background.setTint(context.getColorRes(backgroundTint))
    }

    fun setText(text: String) {
        binding.tvLabel.text = text
    }

    fun setText(widgetItem: MultiSelectFilterWidgetV2Item) {
        if (widgetItem.isMultiSelectFilter()) {
            binding.tvLabel.text = widgetItem.title.orEmpty()
        } else {
            if (widgetItem.isItemSelected()) {
                binding.tvLabel.text = widgetItem.filterItems?.firstOrNull { it.isSelected }?.text.orEmpty()
            } else {
                binding.tvLabel.text = widgetItem.title.orEmpty()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateSelection(
        isMultiSelect: Boolean,
        isSelected: Boolean,
        selectedItemCount: Int
    ) {
        if (isMultiSelect) {
            binding.tvSelectionCount.layoutParams = binding.tvSelectionCount.layoutParams.apply {
                height = 10.dpToPx()
                width = 10.dpToPx()
            }
            if (isSelected) {
                binding.tvSelectionCount.show()
                binding.tvSelectionCount.text = selectedItemCount.toString()
            } else {
                binding.tvSelectionCount.hide()
            }
        } else {
            binding.tvSelectionCount.layoutParams = binding.tvSelectionCount.layoutParams.apply {
                width = 8.dpToPx()
                height = 8.dpToPx()
            }
            binding.tvSelectionCount.text = ""
            if (isSelected) {
                binding.tvSelectionCount.show()
            } else {
                binding.tvSelectionCount.hide()
            }
        }
    }

    fun setUpView(widgetItem: MultiSelectFilterWidgetV2Item) {
        setText(widgetItem)

        // If selected there will always be filter items and thus will always have selected items.
        updateSelection(
            widgetItem.isMultiSelectFilter(),
            widgetItem.isItemSelected(),
            widgetItem.filterItems?.count { it.isSelected } ?: 0,
        )
    }
}
