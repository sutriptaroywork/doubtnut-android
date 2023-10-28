package com.doubtnutapp.revisioncorner.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.revisioncorner.Topic
import com.doubtnutapp.databinding.FragmentRevisionCornerChapterBottomSheetDialogBinding
import com.doubtnutapp.revisioncorner.ui.adapter.ChapterAdapter
import com.doubtnutapp.utils.RxEditTextObservable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by devansh on 12/08/21.
 */

class ChapterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(FragmentRevisionCornerChapterBottomSheetDialogBinding::bind)
    private val args by navArgs<ChapterBottomSheetDialogFragmentArgs>()

    private val chapterAdapter by lazy { ChapterAdapter(RcChapterSelectionFragment.CHAPTER, null) }

    private lateinit var mDisposable: Disposable

    private lateinit var topicsList: List<Topic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRevisionCornerChapterBottomSheetDialogBinding
            .inflate(inflater, container, false).root
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
            val queryLowercase = query.toLowerCase(Locale.ROOT)
            topicsList.filter {
                it.title.toLowerCase(Locale.ROOT).contains(queryLowercase)
            }
        }
        chapterAdapter.updateList(newList)
    }
}