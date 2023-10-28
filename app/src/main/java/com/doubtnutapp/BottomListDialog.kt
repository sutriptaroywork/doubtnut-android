package com.doubtnutapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.databinding.DialogBottomListBinding
import com.doubtnutapp.home.HomeFragmentViewModel
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.model.BottomData
import com.doubtnutapp.similarVideo.model.NcertViewItemEntity
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BottomListDialog :
    BaseBindingBottomSheetDialogFragment<HomeFragmentViewModel, DialogBottomListBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "BottomListDialog"
        fun newInstance(event: AppEvent): BottomListDialog {
            return BottomListDialog().apply {
                arguments = Bundle().apply {
                    putParcelable("event", event)
                }
            }
        }
    }

    val adapter: NcertViewItemBottomAdapter by lazy { NcertViewItemBottomAdapter(this) }

    override fun onStart() {
        super.onStart()
        initView()
    }

    private fun initView() {
        mBinding?.buttonClose?.setOnClickListener {
            dismiss()
        }

        mBinding?.recyclerView?.adapter = adapter
        if (arguments != null) {
            val event: AppEvent = requireArguments().getParcelable("event")!!
            getDatabase(context)?.eventsDao()?.delete(event)
            val bottomData: BottomData? = Gson().fromJson(event.data, object : TypeToken<BottomData>() {}.type)
            if (bottomData != null) {
                val list: List<NcertViewItemEntity> = bottomData.list?.map {
                    NcertViewItemEntity(
                        id = it.id.orEmpty(),
                        name = it.name.orEmpty(),
                        description = it.description.orEmpty(),
                        isLast = it.isLast.orEmpty(),
                        parent = it.parent.orEmpty(),
                        resourceType = it.resourceType.orEmpty(),
                        studentClass = it.studentClass.orEmpty(),
                        subject = it.subject.orEmpty(),
                        mainDescription = it.mainDescription.orEmpty()
                    )
                }.orEmpty()
                adapter.updateData(list)
                mBinding?.textViewTitle?.text = bottomData.title
            } else {
                dismiss()
            }
        }
    }

    override fun performAction(action: Any) {
        context?.let {
            viewModel.handleAction(action, it)
            if (action is OpenLibraryPlayListActivity || action is OpenLibraryVideoPlayListScreen) {
                dismiss()
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogBottomListBinding {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        return DialogBottomListBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeFragmentViewModel {
        return ViewModelProviders.of(requireParentFragment()).get(HomeFragmentViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}
