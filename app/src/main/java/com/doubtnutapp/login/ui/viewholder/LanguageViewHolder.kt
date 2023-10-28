package com.doubtnutapp.login.ui.viewholder

import androidx.core.content.edit
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnBoardLanguageSelected
import com.doubtnutapp.data.remote.models.Language
import com.doubtnutapp.databinding.ItemSelectLanguageBinding


class LanguageViewHolder(val binding: ItemSelectLanguageBinding) : BaseViewHolder<Language>(binding.root) {

    override fun bind(data: Language) {
        binding.tvTitle.text = data.languageDisplay
        binding.tvSubTitle.text = data.language

        if (data.icons.isNotEmpty()) {
            binding.ivIcon.show()
            binding.ivIcon.loadImage(data.icons)
        } else {
            binding.ivIcon.hide()
        }

        binding.cvLanguage.setOnClickListener {
            updateLanguage(data)

            actionPerformer?.performAction(OnBoardLanguageSelected(data.code, data.languageDisplay))

            // Update Locale for selected language
            LocaleManager.updateResources(binding.root.context, data.code)
        }
    }

    /**
     * This method update language code, name and display name to preference
     */
    private fun updateLanguage(language: Language) {

        defaultPrefs().edit {
            putString(Constants.STUDENT_LANGUAGE_CODE, language.code)
            putString(Constants.STUDENT_LANGUAGE_NAME, language.language)
            putString(Constants.STUDENT_LANGUAGE_NAME_DISPLAY, language.languageDisplay)
        }
    }
}