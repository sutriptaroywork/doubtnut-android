package com.doubtnutapp.studygroup.ui.fragment

import android.Manifest
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.Utils
import com.doubtnut.core.utils.blockSpecialCharacters
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.Status
import com.doubtnutapp.base.extension.hasStoragePermission
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.safeNavigate
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.FragmentSgCreateBinding
import com.doubtnutapp.studygroup.model.GroupType
import com.doubtnutapp.studygroup.model.SgType
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.FilePathUtils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.utils.showReusableToast
import com.doubtnutapp.utils.showToast

class SgCreateBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<SgCreateViewModel, FragmentSgCreateBinding>() {

    companion object {
        const val TAG = "CreateGroupBottomSheetFragment"
        const val CAN_REFRESH_LIST = "can_refresh_list"
        fun newInstance(): SgCreateBottomSheetFragment =
            SgCreateBottomSheetFragment()
        private const val CHARACTER_LIMIT = 18
    }

    private var groupTypes: List<GroupType>? = null
    private var button1Data: GroupType? = null
    private var button2Data: GroupType? = null

    private var selectedButtonIndex = 0
    private var groupName: String? = null
    private var groupImage: String? = null

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> pickImageFromGallery()
                else -> toast(getString(R.string.needstoragepermissions))
            }
        }

    private val pickGalleryImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            imageUri?.let {
                uploadGalleryImage(imageUri)
            }
        }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgCreateBinding =
        FragmentSgCreateBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgCreateViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        mBinding?.etGroupName?.blockSpecialCharacters()
        viewModel.getCreateSgInfo()
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        mBinding?.apply {
            etGroupName.addTextChangedListener {
                groupName = it?.toString()?.trim()
                val isEnabled = groupName.isNullOrEmpty().not() && Utils.isAlphaNumeric(groupName)
                        && groupName?.length!! <= CHARACTER_LIMIT
                btCreateGroup.isEnabled = isEnabled

                if (isEnabled) {
                    btCreateGroup.setStrokeColorResource(R.color.tomato)
                    btCreateGroup.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.tomato
                        )
                    )
                } else {
                    btCreateGroup.setStrokeColorResource(R.color.grey_cbcbcb)
                    btCreateGroup.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grey_cbcbcb
                        )
                    )
                }
            }

            ivClose.setOnClickListener {
                dismiss()
            }

            mBinding?.btGroupType1?.setOnClickListener {
                if (selectedButtonIndex == SgButtonIndex.BUTTON1.index) return@setOnClickListener
                setUpSelectedGroup(SgButtonIndex.BUTTON1.index)
            }

            btGroupType2.setOnClickListener {
                if (selectedButtonIndex == SgButtonIndex.BUTTON2.index) return@setOnClickListener
                setUpSelectedGroup(SgButtonIndex.BUTTON2.index)
            }

            tvUploadImage.setOnClickListener {
                selectGroupImage()
            }

            ivCamera.setOnClickListener {
                selectGroupImage()
            }

            btCreateGroup.setOnClickListener(object : DebouncedOnClickListener(2000) {
                override fun onDebouncedClick(v: View?) {
                    val selectedGroupType = when (selectedButtonIndex) {
                        SgButtonIndex.BUTTON1.index -> button1Data?.groupType
                        SgButtonIndex.BUTTON2.index -> button2Data?.groupType
                        else -> return
                    } ?: return
                    viewModel.sendEvent(EventConstants.SG_CREATE_GROUP_CLICKED,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TYPE, selectedGroupType)
                        })
                    when (selectedGroupType) {
                        SgType.PUBLIC.type -> {
                            viewModel.createPublicGroup(
                                groupName = groupName!!,
                                groupImage = groupImage,
                                onlySubAdminCanPost = if (mBinding?.memberPostSwitch?.isChecked == true) 1 else 0
                            )
                        }
                        SgType.PRIVATE.type -> {
                            viewModel.createGroup(groupName = groupName!!, groupImage = groupImage, isSupport = null)
                        }
                    }
                }
            })
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        setNavigationResult(true, CAN_REFRESH_LIST)
        super.onDismiss(dialog)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.groupInfoLiveDataCreate.observe(this) { createStudyGroupInfo ->
            mBinding?.tvTitle?.text = createStudyGroupInfo.heading
            groupTypes = createStudyGroupInfo.groupTypes
            button1Data = createStudyGroupInfo.groupTypes.find {
                it.activeTab
            }
            button2Data = createStudyGroupInfo.groupTypes.find {
                !it.activeTab
            }
            updateGroupTypes()
        }

        viewModel.groupCreatedLiveData.observeEvent(this) { createStudyGroup ->
            mBinding ?: return@observeEvent
            val selectedGroupType = when (selectedButtonIndex) {
                SgButtonIndex.BUTTON1.index -> button1Data?.groupType
                SgButtonIndex.BUTTON2.index -> button2Data?.groupType
                else -> ""
            } ?: ""
            if (createStudyGroup.isGroupCreated) {
                viewModel.sendEvent(EventConstants.SG_CREATED, hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, selectedGroupType)
                })
                mayNavigate {
                    val direction =
                        SgCreateBottomSheetFragmentDirections.actionSgCreateBottomSheetFragmentToSgChatFragment(
                            groupId = createStudyGroup.groupId,
                            isFaq = false,
                            initialMessageInfo = createStudyGroup.initialMessagesData
                        )
                    safeNavigate(direction) {
                        navigate(this)
                    }
                }
                mBinding?.btCreateGroup?.disable()
            } else {
                mBinding?.btCreateGroup?.enable()
                toast(createStudyGroup.message)
            }
        }

        viewModel.imageFileNameLiveData.observe(this) { imageFileName ->
            if (imageFileName != null) {
                groupImage = imageFileName
            }
        }

        viewModel.stateLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    mBinding?.progressBar?.hide()
                    showReusableToast(it.message!!)
                }
                Status.NONE -> {
                    mBinding?.progressBar?.hide()
                }
                Status.LOADING -> {
                    mBinding?.progressBar?.show()
                    showToast(requireContext(), it.message!!)
                }
                Status.ERROR -> {
                    mBinding?.progressBar?.hide()
                    showToast(requireContext(), it.message!!)
                }
            }
        }
    }

    private fun updateGroupTypes() {
        val button1Data = button1Data ?: return
        val button2Data = button2Data ?: return
        mBinding?.createGroupContainer?.show()
        mBinding?.btGroupType1?.text = button1Data.title
        mBinding?.btGroupType2?.text = button2Data.title
        selectedButtonIndex = SgButtonIndex.BUTTON1.index
        setUpSelectedGroup(selectedButtonIndex)
    }

    private fun setUpSelectedGroup(selectedGroupIndex: Int) {
        val button1Data = button1Data ?: return
        val button2Data = button2Data ?: return
        this.selectedButtonIndex = selectedGroupIndex
        val selectedGroup = when (selectedGroupIndex) {
            SgButtonIndex.BUTTON1.index -> {
                button1Data
            }
            SgButtonIndex.BUTTON2.index -> {
                button2Data
            }
            else -> return
        }

        setUpButton1(button1Data.canCreateGroup, selectedGroup.groupType == button1Data.groupType)
        setUpButton2(button2Data.canCreateGroup, selectedGroup.groupType == button2Data.groupType)

        when (selectedGroup.canCreateGroup) {
            true -> {
                mBinding?.apply {
                    tvGroupInfoTitle.show()
                    groupImageLayout.show()
                    tvGroupInfoTitle.text = selectedGroup.heading
                    val guidelines = selectedGroup.guidelines
                    var description = ""
                    guidelines.forEachIndexed { index, guideline ->
                        val sequence = guideline.index
                        val content = guideline.content
                        description = "$description $sequence $content"
                        if (index != guidelines.size - 1) {
                            description = "$description\n\n"
                        }
                    }
                    tvGroupInfo.text = description
                    groupNameInputLayout.apply {
                        show()
                        hint = buildSpannedString {
                            append(selectedGroup.groupNameTitle)
                            color(Color.RED) { append(" *") }
                        }
                    }
                    tvUploadImage.text = selectedGroup.groupImageTitle

                    when (selectedGroup.onlySubAdminCanPostToggle) {
                        true -> {
                            memberPostSwitch.show()
                            memberPostSwitch.text =
                                selectedGroup.subAdminPostContainer?.title
                            memberPostSwitch.isChecked =
                                selectedGroup.subAdminPostContainer?.onlySubAdminCanPost == true
                        }
                        else -> {
                            memberPostSwitch.hide()
                        }
                    }
                }
            }
            else -> {
                mBinding?.apply {
                    tvGroupInfo.text = selectedGroup.noGroupCreateMessage
                    tvGroupInfoTitle.hide()
                    groupNameInputLayout.hide()
                    groupImageLayout.hide()
                }
            }
        }
    }

    private fun setUpButton1(canCreateGroup: Boolean, isActive: Boolean) {
        when (canCreateGroup) {
            true -> {
                if (isActive) {
                    mBinding?.btGroupType1?.setStrokeColorResource(R.color.tomato)
                    mBinding?.btGroupType1?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.tomato
                        )
                    )
                    mBinding?.btGroupType1?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                } else {
                    mBinding?.btGroupType1?.setStrokeColorResource(R.color.tomato)
                    mBinding?.btGroupType1?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    mBinding?.btGroupType1?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_two
                        )
                    )
                }
            }
            else -> {
                mBinding?.btGroupType1?.setStrokeColorResource(R.color.grey_cbcbcb)
                mBinding?.btGroupType1?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                mBinding?.btGroupType1?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_cbcbcb
                    )
                )
            }
        }
    }

    private fun setUpButton2(canCreateGroup: Boolean, isActive: Boolean) {
        when (canCreateGroup) {
            true -> {
                if (isActive) {
                    mBinding?.btGroupType2?.setStrokeColorResource(R.color.tomato)
                    mBinding?.btGroupType2?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.tomato
                        )
                    )
                    mBinding?.btGroupType2?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                } else {
                    mBinding?.btGroupType2?.setStrokeColorResource(R.color.tomato)
                    mBinding?.btGroupType2?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    mBinding?.btGroupType2?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_two
                        )
                    )
                }
            }
            else -> {
                mBinding?.btGroupType2?.setStrokeColorResource(R.color.grey_cbcbcb)
                mBinding?.btGroupType2?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                mBinding?.btGroupType2?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_cbcbcb
                    )
                )
            }
        }
    }

    private fun uploadGalleryImage(imageUri: Uri) {
        val imagePath = FilePathUtils.getRealPath(requireContext(), imageUri)
        if (imagePath != null) {
            mBinding?.ivCamera?.hide()
            mBinding?.ivGroup?.show()
            mBinding?.ivGroup?.loadImage(imagePath)
            viewModel.uploadAttachment(imagePath)
        }
    }

    private fun selectGroupImage() {
        if (hasStoragePermission()) {
            pickImageFromGallery()
        } else {
            requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun pickImageFromGallery() {
        pickGalleryImage.launch("image/*")
    }

    enum class SgButtonIndex(val index: Int) {
        BUTTON1(0),
        BUTTON2(1)
    }

}