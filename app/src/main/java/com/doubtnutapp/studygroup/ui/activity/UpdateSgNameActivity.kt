package com.doubtnutapp.studygroup.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.Utils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityUpdateSgNameBinding
import com.doubtnutapp.studygroup.viewmodel.UpdateSgInfoViewModel
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.KeyboardUtils.showKeyboard
import dagger.android.AndroidInjection
import javax.inject.Inject

class UpdateSgNameActivity : BaseActivity() {

    companion object {

        private const val TAG = "UpdateSgNameActivity"

        const val PARAM_KEY_GROUP_ID = "group_id"
        const val PARAM_KEY_GROUP_NAME = "group_name"
        const val GROUP_NAME_LIMIT = 18

        fun getStartIntent(
            context: Context, groupId: String, groupName: String?
        ) = Intent(context, UpdateSgNameActivity::class.java).apply {
            putExtra(PARAM_KEY_GROUP_ID, groupId)
            putExtra(PARAM_KEY_GROUP_NAME, groupName)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UpdateSgInfoViewModel by viewModels { viewModelFactory }

    private val binding by viewBinding(ActivityUpdateSgNameBinding::inflate)

    private var groupId: String? = null
    private var groupName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        groupId = intent.getStringExtra(PARAM_KEY_GROUP_ID)
        groupName = intent.getStringExtra(PARAM_KEY_GROUP_NAME)
        setUpListeners()
        setUpUi()
        setUpObservers()
    }

    private fun setUpUi() {
        binding.tvCharacterCount.text = GROUP_NAME_LIMIT.toString()
        binding.etStudyGroupName.setText(groupName)
        showKeyboard(binding.etStudyGroupName)
        binding.etStudyGroupName.requestFocus()
        showKeyboard(binding.etStudyGroupName)
    }

    private fun setUpObservers() {
        viewModel.updateSgInfoLiveData.observe(this) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progress.isVisible = it.loading
                }
                is Outcome.Success -> {
                    val data = it.data
                    if (data.isUpdated) {
                        viewModel.sendEvent(
                            EventConstants.SG_GROUP_NAME_UPDATED,
                            ignoreSnowplow = true
                        )
                        val resultIntent = Intent()
                        resultIntent.putExtra(
                            PARAM_KEY_GROUP_NAME,
                            binding.etStudyGroupName.text.toString().trim()
                        )
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        toast(data.message.orEmpty())
                    }
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                else -> {
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.etStudyGroupName.addTextChangedListener {
            val charCount = it?.toString()?.trim()?.length ?: 0
            binding.tvCharacterCount.text = (GROUP_NAME_LIMIT - charCount).toString()
            binding.buttonOk.isEnabled =
                charCount in 1..GROUP_NAME_LIMIT && groupName != binding.etStudyGroupName.text.toString()
                    .trim() && Utils.isAlphaNumeric(it?.toString()?.trim())
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.buttonOk.setOnClickListener {
            viewModel.updateStudyGroupInfo(
                groupId!!,
                binding.etStudyGroupName.text.toString().trim(),
                null
            )
        }
    }
}