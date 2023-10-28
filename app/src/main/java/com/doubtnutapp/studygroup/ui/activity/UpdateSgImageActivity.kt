package com.doubtnutapp.studygroup.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.Status
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityUpdateSgIconBinding
import com.doubtnutapp.hasStoragePermission
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.viewmodel.UpdateSgInfoViewModel
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.FilePathUtils
import com.doubtnutapp.utils.PickContent
import com.doubtnutapp.utils.PickContentInput
import com.doubtnutapp.utils.showToast
import dagger.android.AndroidInjection
import javax.inject.Inject


class UpdateSgImageActivity : BaseActivity() {

    companion object {

        private const val TAG = "UpdateSgImageActivity"

        const val PARAM_KEY_GROUP_ID = "group_id"
        const val PARAM_KEY_GROUP_IMAGE = "group_image"
        const val PARAM_KEY_IS_ADMIN = "is_admin"

        fun getStartIntent(
            context: Context, groupId: String, groupImage: String?, isAdmin: Boolean
        ) = Intent(context, UpdateSgImageActivity::class.java).apply {
            putExtra(PARAM_KEY_GROUP_ID, groupId)
            putExtra(PARAM_KEY_GROUP_IMAGE, groupImage)
            putExtra(PARAM_KEY_IS_ADMIN, isAdmin)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UpdateSgInfoViewModel by viewModels { viewModelFactory }

    private val binding by viewBinding(ActivityUpdateSgIconBinding::inflate)

    private var groupId: String? = null
    private var groupImage: String? = null
    private var isAdmin: Boolean = false

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> pickContentFromGallery()
                else -> toast(getString(R.string.needstoragepermissions))
            }
        }

    private val pickContent = registerForActivityResult(PickContent()) { outputIntent ->
        outputIntent?.let { contentOutput ->
            when (contentOutput.requestCode) {
                SgChatFragment.REQUEST_CODE_GALLERY -> {
                    contentOutput.uri?.let { uri ->
                        val filePath =
                            FilePathUtils.getRealPath(this, uri) ?: return@registerForActivityResult
                        viewModel.uploadAttachment(filePath)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        groupId = intent.getStringExtra(PARAM_KEY_GROUP_ID)
        groupImage = intent.getStringExtra(PARAM_KEY_GROUP_IMAGE)
        isAdmin = intent.getBooleanExtra(PARAM_KEY_IS_ADMIN, false)
        setUpListeners()
        setUpUi()
        setUpObservers()
    }

    private fun setUpUi() {
        binding.ivGroupIcon.loadImage(groupImage)
        binding.ivEdit.isVisible = isAdmin
    }

    private fun setUpObservers() {

        viewModel.updateSgInfoLiveData.observe(this, {
            when (it) {
                is Outcome.Progress -> {
                    binding.progress.isVisible = it.loading
                }
                is Outcome.Success -> {
                    val resultIntent = Intent()
                    resultIntent.putExtra(
                        PARAM_KEY_GROUP_IMAGE,
                        viewModel.uploadedAttachmentUrl
                    )
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                else -> {
                }
            }
        })

        viewModel.stateLiveData.observe(this, {
            when (it.status) {
                Status.SUCCESS -> {
                    viewModel.updateStudyGroupInfo(groupId!!, null, viewModel.uploadedAttachmentFileName)
                }
                Status.NONE -> {
                    binding.fileUploadProgress.hide()
                }
                Status.LOADING -> {
                    binding.fileUploadProgress.show()
                    if (it.message != null) {
                        it.message.toIntOrNull()?.let { progress ->
                            binding.fileUploadProgress.setProgressCompat(progress, true)
                        } ?: toast(it.message.orEmpty())
                    }
                }
                Status.ERROR -> {
                    binding.fileUploadProgress.hide()
                    showToast(this, it.message!!)
                }
            }
        })
    }

    private fun setUpListeners() {
        binding.ivEdit.setOnClickListener {
            if (hasStoragePermission()) {
                pickContentFromGallery()
            } else {
                requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun pickContentFromGallery() {
        pickContent.launch(
            PickContentInput(
                type = "image/*",
                requestCode = SgChatFragment.REQUEST_CODE_GALLERY,
                extraMimeTypes = null
            )
        )
    }
}