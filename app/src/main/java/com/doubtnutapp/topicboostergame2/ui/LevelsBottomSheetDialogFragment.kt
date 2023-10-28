package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TbgLevelInfoClicked
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.LevelData
import com.doubtnutapp.databinding.FragmentLevelsBottomSheetDialogBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.LevelAdapter
import com.doubtnutapp.topicboostergame2.ui.adapter.LevelFooterAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.LevelsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class LevelsBottomSheetDialogFragment : BottomSheetDialogFragment(), ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentLevelsBottomSheetDialogBinding::bind)
    private val viewModel by viewModels<LevelsViewModel> { viewModelFactory }

    private val levelAdapter by lazy { LevelAdapter() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_levels_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        setupObservers()
        viewModel.getLevels()
    }

    private fun setupObservers() {
        viewModel.levelsLiveData.observe(this) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }
    }

    private fun updateUi(data: LevelData) {
        with(binding) {
            val footerAdapter = LevelFooterAdapter(data.completedText.orEmpty())
            rvLevels.adapter = ConcatAdapter(levelAdapter, footerAdapter)
            levelAdapter.updateList(data.levels)

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun performAction(action: Any) {
        when(action){
            is TbgLevelInfoClicked -> {
                viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_LEVEL_INFO_CLICK)
            }
        }
    }
}