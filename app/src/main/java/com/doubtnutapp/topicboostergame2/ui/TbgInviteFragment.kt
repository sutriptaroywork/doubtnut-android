package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.safeNavigate
import com.doubtnutapp.base.extension.setBackgroundTint
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.data.remote.models.topicboostergame2.MultipleInvite
import com.doubtnutapp.data.remote.models.topicboostergame2.PopupDialogData
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgInviteData
import com.doubtnutapp.databinding.FragmentTbgInviteBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.studygroup.ui.fragment.SgActionDialogFragment
import com.doubtnutapp.studygroup.ui.fragment.SgSelectFriendFragment
import com.doubtnutapp.topicboostergame2.ui.adapter.InviteFriendsViewPagerAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.InviteFriendsViewModel
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.RxEditTextObservable
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgets.countrycodepicker.CountryCodePickerDialogFragment
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * Created by devansh on 21/06/21.
 */

class TbgInviteFragment : Fragment(R.layout.fragment_tbg_invite),
        CountryCodePickerDialogFragment.OnCountryCodePickedListener {

    companion object {
        fun newInstance(args: Bundle): TbgInviteFragment {
            return TbgInviteFragment().apply {
                arguments = args
            }
        }

        const val TAG = "tbg_invite_fragment"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val viewModel by viewModels<InviteFriendsViewModel> { viewModelFactory }
    private val binding by viewBinding(FragmentTbgInviteBinding::bind)
    private val args by navArgs<TbgInviteFragmentArgs>()
    private val navController by lazy { findNavController() }

    private var gameId: String = ""
    private lateinit var numberPopupdata: MultipleInvite
    private var mSelectFriendListener: SelectFriendListener? = null
    private var source: String? = TAG

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getInviteFriendData(args.chapterAlias, args.source)
    }

    private fun setupObservers() {
        viewModel.tbgInviteLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    setupUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.isAnyMemberInvitedLiveData.observe(viewLifecycleOwner) {
            binding.buttonInviteFriends.isEnabled = it
            tabSwitching(!it)
        }

        getNavigationResult<CountryCode>(CountryCodePickerDialogFragment.COUNTRY_CODE)
                ?.observe(viewLifecycleOwner) {
                    binding.tvCountryCode.text = it.plusAppendedPhoneCode
                    validatePhoneNumber(binding.etPhoneNumber.text.toString())
                }

        viewModel.sendInvitationLiveData.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                viewModel.sendEvent(
                        EventConstants.TOPIC_BOOSTER_GAME_INVITE_SENT,
                        hashMapOf(EventConstants.SOURCE to EventConstants.NOTIFICATION)
                )
                startGameFlow(viewModel.inviteeIds)
            }
        })

        viewModel.sendNumberInviteLiveData.observe(viewLifecycleOwner) {
            if (it.userExist) {
                when (source) {
                    TAG -> {
                        viewModel.sendEvent(
                                EventConstants.TOPIC_BOOSTER_GAME_INVITE_SENT,
                                hashMapOf(EventConstants.SOURCE to EventConstants.SMS)
                        )
                        startGameFlow(arrayOf(it.inviteeId.orEmpty()))
                    }
                    mSelectFriendListener?.getSource() -> {
                        if (it.inviteeId != UserUtil.getStudentId()) {
                            deeplinkAction.performAction(binding.root.context, it.chatDeeplink)
                        } else {
                            mSelectFriendListener?.navigateUp()
                        }
                    }
                }
            } else {
                val popupData = it.popupData
                if (popupData != null) {
                    numberPopupdata = popupData
                    val popupDialogData = PopupDialogData(
                            numberPopupdata.description,
                            numberPopupdata.primaryCta,
                            numberPopupdata.secondaryCta
                    )
                    when (source) {
                        TAG -> navController.navigate(TbgInviteFragmentDirections.actionOpenPopupDialogFragment(popupDialogData))

                        mSelectFriendListener?.getSource() -> {
                            TbgPopupDialogFragment.newInstance(TbgPopupDialogFragmentArgs(
                                    popupDialogData
                            ).toBundle()).show(parentFragmentManager, SgActionDialogFragment.TAG)
                        }
                    }
                }
            }
        }
        viewModel.selectSingleFriendLiveData.observeEvent(viewLifecycleOwner, {
            mSelectFriendListener?.onSelectFriend(it)
        })
    }

    private fun setupUi(data: TbgInviteData) {
        gameId = data.gameId
        if (mSelectFriendListener != null) {
            source = mSelectFriendListener?.getSource()
        }
        with(binding) {
            tvTitleSelectFriends.apply {
                isVisible = data.title.isNotNullAndNotEmpty()
                text = data.title
            }
                tvTitleInviteFriends.text = data.subtitle

                if (viewModel.isWhatsAppInstalled() && data.whatsappCta.isNullOrEmpty().not()) {
                    groupViewWhatsapp.show()
                    buttonInviteWhatsApp.apply {
                        text = data.whatsappCta
                        setOnClickListener {
                            shareOnWhatsApp(data.whatsappShareText)
                            if (source == TAG) {
                                startGameFlow(emptyArray(), true)
                            }
                        }
                    }
                } else {
                    groupViewWhatsapp.hide()
                }

                tvCountryCode.setOnClickListener {
                    when (source) {
                        TAG -> {
                            navController.navigate(R.id.actionOpenCountryCodePickerDialog)
                        }
                        mSelectFriendListener?.getSource() -> {
                            CountryCodePickerDialogFragment().apply {
                                setOnCountryCodePickedListener(this@TbgInviteFragment)
                            }.show(childFragmentManager, CountryCodePickerDialogFragment.TAG)
                        }
                    }
                }

                tvSendInvite.text = data.sendInviteText
                tvSendInvite.setBackgroundTint(R.color.gray_c4c4c4)

                etPhoneNumber.hint = data.numberInvite
                viewModel.compositeDisposable + RxEditTextObservable.fromView(etPhoneNumber)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            validatePhoneNumber(it.orEmpty())
                        }

                viewPagerFriends.adapter =
                        InviteFriendsViewPagerAdapter(this@TbgInviteFragment, data.tabs, source)

                var selectedTabPosition = 0
                TabLayoutMediator(tabLayoutFriends, viewPagerFriends) { tab, position ->
                    val tabData = data.tabs[position]
                    tab.id = tabData.id
                    tab.text = tabData.title
                    if (tabData.id == data.activeTab) {
                        selectedTabPosition = position
                    }
                }.attach()
                tabLayoutFriends.getTabAt(selectedTabPosition)?.select()

                etSearch.hint = data.searchPlaceholder
                viewModel.compositeDisposable + RxEditTextObservable.fromView(etSearch)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            viewModel.searchFriends(it)
                        }

                buttonInviteFriends.isVisible = source != SgSelectFriendFragment.TAG
                buttonInviteFriends.text = data.cta
                buttonInviteFriends.setOnClickListener {
                    if (viewModel.inviteeIds.size > 1) {
                        val popupData = data.multipleInvite
                        navController.navigate(
                                TbgInviteFragmentDirections.actionOpenPopupDialogFragment(
                                        PopupDialogData(
                                                popupData.description,
                                                popupData.primaryCta,
                                                popupData.secondaryCta
                                        )
                                )
                        )
                    } else {
                        viewModel.sendTbgInvitation(gameId, args.chapterAlias)
                    }
                }

                tvSendInvite.setOnClickListener {
                    if (!etPhoneNumber.text.isNullOrEmpty()) {
                        viewModel.sendNumberInvitation(
                                gameId,
                                etPhoneNumber.text.toString(),
                                args.chapterAlias,
                                source
                        )
                    }
                }

                if (source == TAG) {
                    getNavigationResult<String?>(data.multipleInvite.primaryCta)?.observe(viewLifecycleOwner) {
                        viewModel.sendTbgInvitation(gameId, args.chapterAlias)
                    }

                    getNavigationResult<String?>(data.multipleInvite.secondaryCta)?.observe(
                            viewLifecycleOwner
                    ) {}
                }
            }
        }

    private fun startGameFlow(inviteeIds: Array<String>, isWhatsApp: Boolean = false) {
        mayNavigate {
            val action = TbgInviteFragmentDirections.actionStartGameFlow(
                chapterAlias = args.chapterAlias,
                inviteeIds = inviteeIds,
                isInviter = true,
                inviterId = UserUtil.getStudentId(),
                gameId = gameId,
                isWhatsApp = isWhatsApp
            )
            safeNavigate(action) {
                navigate(action)
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String?) {
        if (phoneNumber.isNullOrBlank() || phoneNumber == UserUtil.getPhoneNumber()) {
            binding.apply {
                tvSendInvite.isEnabled = false
                tvErrorPhone.hide()
            }
        } else {
            binding.apply {
                val isIndiaCode = tvCountryCode.text == CountryCode.PLUS_APPENDED_COUNTRY_CODE_INDIA
                val phoneNumberLength = if (isIndiaCode) 10 else 0
                //If country code is not India, number will always be valid as it is not empty
                val isNotValid = if (isIndiaCode) phoneNumber.length != phoneNumberLength else false
                if (isNotValid) {
                    tvErrorPhone.show()
                    tvSendInvite.isEnabled = false
                    tvSendInvite.setBackgroundTint(R.color.gray_c4c4c4)
                } else {
                    tvErrorPhone.hide()
                    tvSendInvite.isEnabled = true
                    tvSendInvite.setBackgroundTint(R.color.tomato)
                }
                setPhoneNumberColor(
                        if (isIndiaCode) phoneNumber.length else 1,
                        if (isIndiaCode) phoneNumberLength else phoneNumber.length
                )
            }
        }
    }

    private fun setPhoneNumberColor(currentLength: Int, requireLength: Int) =
            when {
                currentLength > requireLength -> binding.etPhoneNumber.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.red)
                )

                else -> binding.etPhoneNumber.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.greyBlack)
                )
            }

    override fun onCountryCodePicked(countryCodeData: CountryCode) {
        binding.tvCountryCode.text = countryCodeData.plusAppendedPhoneCode
    }

    private fun shareOnWhatsApp(sharingMessage: String?) {
        viewModel.sendEvent(
                EventConstants.TOPIC_BOOSTER_GAME_INVITE_SENT,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.WHATSAPP)
                })
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, sharingMessage)
            type = "text/plain"
        }.also {
            if (AppUtils.isCallable(activity, it)) {
                startActivity(it)
            } else {
                toast(R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun tabSwitching(state: Boolean) {

        val tabStrip: LinearLayout = binding.tabLayoutFriends.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = state
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = state
        }
    }

    fun setSelectFriendListener(selectFriendListener: SelectFriendListener) {
        mSelectFriendListener = selectFriendListener
    }

    interface SelectFriendListener {
        fun onSelectFriend(friend: Friend)
        fun getSource(): String
        fun navigateUp()
    }
}