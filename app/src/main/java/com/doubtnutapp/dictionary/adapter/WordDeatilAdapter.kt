package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.data.dictionary.Language
import com.doubtnutapp.data.dictionary.WordDetail
import com.doubtnutapp.data.dictionary.WordMeaning
import com.doubtnutapp.databinding.LayoutContentDictionaryActivityBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show

class WordDeatilAdapter(
    private val actionPerformer: ActionPerformer,
    private val list: ArrayList<WordDetail> = arrayListOf(),
    private var languageArray: ArrayList<Language>?
) :
    RecyclerView.Adapter<BaseViewHolder<WordDetail>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<WordDetail> {
        return WordDeatilViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_content_dictionary_activity, parent, false
            ),
            actionPerformer, languageArray
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<WordDetail>, position: Int) {
        (holder as WordDeatilViewHolder).languageArray = languageArray
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(wordDetails: List<WordDetail>, languageArray: ArrayList<Language>?) {
        list.clear()
        list.addAll(wordDetails)
        this.languageArray = languageArray
        notifyDataSetChanged()
    }

    class WordDeatilViewHolder(
        val binding: LayoutContentDictionaryActivityBinding,
        mActionPerformer: ActionPerformer,
        var languageArray: ArrayList<Language>?
    ) :
        BaseViewHolder<WordDetail>(binding.root) {

        init {
            actionPerformer = mActionPerformer
        }

        override fun bind(data: WordDetail) {
            if (!data.word?.audioUrl.isNullOrEmpty()) {
                binding.btnAudio.show()
                binding.btnAudio.setOnClickListener { playAudio(data.word!!.audioUrl!!) }
            } else {
                binding.btnAudio.hide()
            }

            if (!data.word?.text.isNullOrEmpty()) {
                binding.tvText.show()
                binding.tvText.text = data.word?.text.orEmpty()
            } else {
                binding.tvText.hide()
            }

            if (!data.word?.localized.isNullOrEmpty()) {
                binding.tvPhonetic.show()
                binding.tvPhonetic.text = data.word?.phonetic.orEmpty()
            } else {
                binding.tvPhonetic.hide()
            }

            if (!data.word?.localized.isNullOrEmpty()) {
                binding.tvMeaning.text = data.word?.localized.orEmpty()
                binding.tvMeaning.show()
            } else {
                binding.tvMeaning.hide()
            }

            if (!languageArray.isNullOrEmpty() && absoluteAdapterPosition == 0) {
                binding.tvLanguage.hide()
                for (language in languageArray!!) {
                    if (language.isSelected == true) {
                        binding.tvLanguage.text = language.text
                        binding.tvLanguage.show()
                        binding.tvLanguage.setOnClickListener {
                            openLanguageBottomsheet(
                                languageArray!!
                            )
                        }
                    }
                }
            } else {
                binding.tvLanguage.hide()
            }

            binding.rvWordMeaning.adapter =
                WordMeaningAdapter(
                    actionPerformer,
                    data.meanings.orEmpty() as ArrayList<WordMeaning>
                )
        }

        private fun openLanguageBottomsheet(languageArray: ArrayList<Language>) {
            actionPerformer?.performAction(OpenDictionaryLangaugeBottomSheet(languageArray))
        }

        private fun playAudio(audioUrl: String) {
            actionPerformer?.performAction(PlayAudio(audioUrl))
        }
    }
}
