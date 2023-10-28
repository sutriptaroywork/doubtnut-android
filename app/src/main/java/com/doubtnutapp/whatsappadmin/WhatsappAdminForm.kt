package com.doubtnutapp.whatsappadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.DropDownListItemSelected
import com.doubtnutapp.base.SubmitWhatsappAdminForm
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.data.remote.models.whatsappadmin.StateDistrictModel
import com.doubtnutapp.databinding.FragmentWhatsappAdminFormBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class WhatsappAdminForm : BaseBindingFragment<DummyViewModel, FragmentWhatsappAdminFormBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var ctaText: String? = null
    private var source: String? = ""

    private var actionPerformer: ActionPerformer? = null

    private lateinit var stateDistrictList: ArrayList<StateDistrictModel>
    private var mobile: String? = ""
    private var name: String? = ""
    private var state: String? = ""
    private var district: String? = ""
    private var friendsCount: Int = -1

    private var selectedStatePosition: Int = -1

    private var dropDownPopup: StateDistrictDropDownMenu? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWhatsappAdminFormBinding =
        FragmentWhatsappAdminFormBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (activity as WhatsappAdminActivity).supportActionBar?.title = "Apply to be a District Admin"

        if(!ctaText.isNullOrEmpty()) {
            binding.btnSubmitAdminForm.text = ctaText
        }
        binding.btnSubmitAdminForm.setOnClickListener {
            val eventParams = hashMapOf<String,Any>()
            eventParams[Constants.SOURCE] = source.orEmpty()
            eventParams["state"] = state.orEmpty()
            eventParams["district"] = district.orEmpty()
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WHATSAPP_ADMIN_FORM_SUBMIT,eventParams))

            getParamsFromForm()

            actionPerformer?.performAction(
                SubmitWhatsappAdminForm(
                    mobile!!,
                    name!!,
                    state!!,
                    district!!,
                    friendsCount
                )
            )
        }

        setTextChangeListeners()

        binding.flState.setOnClickListener {
            showStateDropdown()
        }

        binding.flDistrict.setOnClickListener {
            showDistrictDropdown()
        }
    }

    private fun showDistrictDropdown() {
        if (state.isNullOrEmpty() || selectedStatePosition < 0) {
            toast("Please select state")
            return
        }

        showPopup(stateDistrictList[selectedStatePosition].districts, "district", binding.etDistrict)
    }

    private fun showStateDropdown() {
        val stateList = arrayListOf<String>()
        for (item in stateDistrictList) {
            stateList.add(item.state)
        }
        showPopup(stateList, "state", binding.etState)
    }

    private fun showPopup(list: ArrayList<String>, type: String, anchorView: View) {
        if (dropDownPopup?.isShowing == true) {
            dropDownPopup?.dismiss()
        }
        dropDownPopup = StateDistrictDropDownMenu(
            requireContext(),
            this,
            list,
            type
        )

        dropDownPopup!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        dropDownPopup!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        dropDownPopup!!.isOutsideTouchable = true
        dropDownPopup!!.isFocusable = true
        dropDownPopup!!.elevation = 4.0f

        dropDownPopup!!.showAsDropDown(anchorView)
    }

    private fun setTextChangeListeners() {
        binding.etPhone.doOnTextChanged { text, start, before, count ->
            mobile = binding.etPhone.text.toString()
            setCtaEnableState()
        }
        binding.etName.doOnTextChanged { text, start, before, count ->
            name = binding.etName.text.toString()
            val nameError = isValidName(name)
            if (nameError.isNullOrEmpty()) {
                setInvalidNameError(null)
            } else {
                setInvalidNameError(nameError)
            }
            setCtaEnableState()
        }
        binding.etState.doOnTextChanged { text, start, before, count ->
            state = binding.etState.text.toString()
            setCtaEnableState()
        }
        binding.etDistrict.doOnTextChanged { text, start, before, count ->
            district = binding.etDistrict.text.toString()
            setCtaEnableState()
        }

        binding.etFriendsCount.doOnTextChanged { text, start, before, count ->
            val friends = binding.etFriendsCount.text.toString()
            if (!friends.isNullOrEmpty()) {
                friendsCount = friends.toInt()
            }
            setCtaEnableState()
        }
    }

    private fun setCtaEnableState() {
        binding.btnSubmitAdminForm.isEnabled =
            (isValidPhone(mobile) && isValidName(name).isNullOrEmpty() && !state.isNullOrEmpty() && !district.isNullOrEmpty() && friendsCount >= 0)

        if(binding.btnSubmitAdminForm.isEnabled){
            binding.btnSubmitAdminForm.setBackgroundColor(requireContext().getColorRes(R.color.tomato))
        }else{
            binding.btnSubmitAdminForm.setBackgroundColor(requireContext().getColorRes(R.color.warm_grey))
        }
    }

    private fun getParamsFromForm() {
        mobile = binding.etPhone.text.toString()
        if (isValidPhone(mobile)) {
            setInvalidPhoneError(false)
        } else {
            setInvalidPhoneError(true)
        }

        name = binding.etName.text.toString()
        val nameError = isValidName(name)
        if (nameError.isNullOrEmpty()) {
            setInvalidNameError(null)
        } else {
            setInvalidNameError(nameError)
        }

        state = binding.etState.text.toString()
        if (!state.isNullOrEmpty()) {
            setInvalidStaeError(false)
        } else {
            setInvalidStaeError(true)
        }

        district = binding.etDistrict.text.toString()
        if (!district.isNullOrEmpty()) {
            setInvalidDistrictError(false)
        } else {
            setInvalidDistrictError(true)
        }

        val friends = binding.etFriendsCount.text.toString()
        if (!friends.isNullOrEmpty()) {
            friendsCount = friends.toInt()
            setInvalidFriendsCount(false)
        } else {
            setInvalidFriendsCount(true)
        }

    }

    private fun isValidName(name: String?): String? {
        if(name.isNullOrEmpty()){
            return "Apna naam enter kare"
        }
        val pattern: Pattern = Pattern.compile("^[\\p{L} ]+$", Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(name)

        if(!matcher.find()){
            return "Invalid name, only a-zA-z and space allowed hai"
        }

        return null
    }

    private fun setInvalidPhoneError(isError: Boolean) {
        binding.etPhone.error = if (isError) "Apna whatsapp number enter kare" else null
    }

    private fun setInvalidNameError(error: String?) {
        binding.etName.error = error
    }

    private fun setInvalidStaeError(isError: Boolean) {
        binding.etState.error = if (isError) "Apna state select kare" else null
    }

    private fun setInvalidDistrictError(isError: Boolean) {
        binding.etDistrict.error = if (isError) "Apna district select kare" else null
    }

    private fun setInvalidFriendsCount(isError: Boolean) {
        binding.etFriendsCount.error = if (isError) "Apne whatsapp friends ka count enter kare" else null
    }

    private fun isValidPhone(phone: String?): Boolean {
        return !phone.isNullOrEmpty() && phone.length == 10
    }

    override fun performAction(action: Any) {
        if (action is DropDownListItemSelected) {
            if (dropDownPopup?.isShowing == true) {
                dropDownPopup?.dismiss()
            }
            if (action.listType == "state") {
                binding.etState.setText(action.value)
                selectedStatePosition = action.clickedPosition
                binding.etDistrict.text = ""
            }

            if (action.listType == "district") {
                binding.etDistrict.text = action.value
            }
        }
    }

    companion object {

        const val TAG = "WhatsappAdminForm"

        @JvmStatic
        fun newInstance(
            actionPerformer: ActionPerformer,
            stateDistrictList: ArrayList<StateDistrictModel>,
            ctaText: String?,
            source: String?
        ) =
            WhatsappAdminForm().apply {
                this.actionPerformer = actionPerformer
                this.stateDistrictList = stateDistrictList
                this.ctaText = ctaText
                this.source = source
            }
    }
}