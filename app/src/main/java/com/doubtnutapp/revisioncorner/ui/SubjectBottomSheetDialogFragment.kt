package com.doubtnutapp.revisioncorner.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentRevisionCornerSubjectBottomSheetDialogBinding
import com.doubtnutapp.revisioncorner.ui.adapter.ChooseSubjectAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

/**
 * Created by devansh on 12/08/21.
 */

class SubjectBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(FragmentRevisionCornerSubjectBottomSheetDialogBinding::bind)
    private val args by navArgs<SubjectBottomSheetDialogFragmentArgs>()

    private val subjectAdapter by lazy { ChooseSubjectAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRevisionCornerSubjectBottomSheetDialogBinding
            .inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        with(binding) {
            rvSubjects.adapter = subjectAdapter
            subjectAdapter.updateList(args.subjects.toList())

            ivClose.setOnClickListener {
                dialog?.cancel()
            }
        }
    }
}