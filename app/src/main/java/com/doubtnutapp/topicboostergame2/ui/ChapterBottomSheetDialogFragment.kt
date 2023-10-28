package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.databinding.FragmentChapterBottomSheetDialogBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.ChapterAdapter
import com.doubtnutapp.utils.RxEditTextObservable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class ChapterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(FragmentChapterBottomSheetDialogBinding::bind)

    private val args by navArgs<ChapterBottomSheetDialogFragmentArgs>()

    private val chapterAdapter by lazy { ChapterAdapter(TbgChapterSelectionFragment.CHAPTER, null) }

    private lateinit var mDisposable: Disposable

    private lateinit var topicsList: List<Topic>

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chapter_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        setupUi(args.chapters.toList())
    }

    private fun setupUi(data: List<Topic>) {

        with(binding) {
            rvChapters.adapter = chapterAdapter
            chapterAdapter.updateList(data.orEmpty())

            ivClose.setOnClickListener {
                dialog?.cancel()
            }
        }

        topicsList = data

        mDisposable = RxEditTextObservable.fromView(binding.etSearch)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                searchChapter(it)
            }
    }

    fun searchChapter(query: String) {
        val newList = if (query.isBlank()) {
            topicsList
        } else {
            topicsList.filter {
                it.title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
            }
        }
        chapterAdapter.updateList(newList)
    }
}