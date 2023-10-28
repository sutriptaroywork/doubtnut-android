package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.doubtnut.core.utils.observeNonNull
import com.doubtnutapp.*
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.databinding.FragmentTbgOpponentWaitBinding
import com.doubtnutapp.socket.OnConnect
import com.doubtnutapp.socket.OnJoin
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.SocketEventType
import com.doubtnutapp.topicboostergame2.extensions.loadOpponentImage
import com.doubtnutapp.topicboostergame2.viewmodel.TbgGameFlowViewModel
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.UserUtil
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 25/06/21.
 */

class TbgOpponentWaitFragment : Fragment(R.layout.fragment_tbg_opponent_wait) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var userPreference: UserPreference

    private val binding by viewBinding(FragmentTbgOpponentWaitBinding::bind)
    private val args by navArgs<TbgOpponentWaitFragmentArgs>()
    private val viewModel by navGraphViewModels<TbgGameFlowViewModel>(R.id.navGraphGameFlow) { viewModelFactory }
    private val navController by lazy { findNavController() }

    private var opponentFoundText: String? = ""

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        private var isBackPressPopupShown = false

        override fun handleOnBackPressed() {
            if (isBackPressPopupShown.not()) {
                val action = TbgOpponentWaitFragmentDirections.actionShowWaitExitDialog(
                    PopupDialogData(
                        description = getString(R.string.wait_exit_dialog_info),
                        button1 = getString(R.string.cancel),
                        button2 = getString(R.string.play_with_an_opponent),
                    )
                )
                isBackPressPopupShown = true
                navController.navigate(action)
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.isInviterFlowExecuted = false
        viewModel.isInviteeFlowExecuted = false

        setupObservers()
        if (args.isOpponentBot) {
            executeInviterFlow()
        } else {
            viewModel.connectSocket()
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
        }
    }

    private fun setupObservers() {
        viewModel.gameLiveData.observeNonNull(viewLifecycleOwner) {
            if (args.isOpponentBot) {
                setupOpponentSearchAnimation(it)
            } else {
                val loadingScreenData = it.loadingScreenContainer
                opponentFoundText = loadingScreenData?.opponentFound
                binding.tvWaitTitle.text = loadingScreenData?.waitTitle
                binding.tvStatus.text = loadingScreenData?.statusText

                //This will be true only in the case of invitee
                it.unavailableData?.isInviter = args.isInviter
                if (args.isInviter.not()) {
                    if (it.canGameStart == true) {
                        val inviteeId = userPreference.getUserStudentId()
                        viewModel.gameBeginEvent(args.inviterId, inviteeId, getGameId())
                        startTimerAnimation(
                            it.inviterData?.name.orEmpty(),
                            it.inviterData?.image.orEmpty(),
                            userPreference.getUserStudentId(),
                            loadingScreenData?.opponentFound.orEmpty()
                        )
                    } else {
                        openOpponentUnavailableScreen(it.unavailableData)
                    }
                } else {
                    startInviteeWaitTimer(it.unavailableData, loadingScreenData?.waitingTime ?: 0)
                }
            }
        }

        viewModel.socketMessageLiveData.observe(viewLifecycleOwner, EventObserver {
            onSocketEvent(it)
        })

        getNavigationResult<String?>(getString(R.string.cancel))?.observe(viewLifecycleOwner) {
        }

        getNavigationResult<String?>(getString(R.string.play_with_an_opponent))?.observe(
            viewLifecycleOwner
        ) {
            val action = NavGraphGameFlowDirections.actionStartGameFlowFromWaitFragment(
                chapterAlias = args.chapterAlias,
                isInviter = true,
                inviterId = UserUtil.getStudentId(),
                isOpponentBot = true,
            )
            navController.navigate(action)
        }
    }

    private fun onSocketEvent(event: SocketEventType) {
        when (event) {
            is OnConnect -> {
                viewModel.joinSocket(getGameId())
            }

            is OnJoin -> {
                if (args.isInviter) {
                    if (viewModel.isInviterFlowExecuted.not()) {
                        executeInviterFlow()
                    }
                } else {
                    if (viewModel.isInviteeFlowExecuted.not()) {
                        executeInviteeFlow()
                    }
                }
            }

            is OnResponseData -> {
                onSocketResponseData(event)
            }
            else -> {
            }
        }
    }

    private fun executeInviterFlow() {
        viewModel.getGameData(args.chapterAlias, getGameId(), args.inviteeIds, args.isWhatsApp)
    }

    private fun executeInviteeFlow() {
        viewModel.checkInviterOnline(args.inviterId, getGameId())
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        when (val data = responseData.data) {
            is InviterOnline -> {
                if (args.isInviter.not()) {
                    viewModel.acceptInvite(
                        getGameId(), data.inviterId.orEmpty(), data.isOnline, args.chapterAlias
                    )
                }
            }
            is GameBegin -> {
                startTimerAnimation(
                    data.name.orEmpty(), data.image.orEmpty(), data.inviteeId, opponentFoundText.orEmpty()
                )
            }
        }
    }

    private fun setupOpponentSearchAnimation(data: TbgGameData) {
        with(binding) {
            tvOnlinePlayers.text = data.inviterData?.counter
            lifecycleScope.launch {
                tvWaitTitle.text = data.loadingScreenContainer?.waitTitle
                tvStatus.text = data.loadingScreenContainer?.statusText
                delay(4000)
                tvStartingIn.setText(R.string.starting_in)
                startTimerAnimation(
                    data.inviterData?.name.orEmpty(),
                    data.inviterData?.image.orEmpty(),
                    null,
                    data.loadingScreenContainer?.opponentFound.orEmpty()
                )
            }
        }
    }

    private fun startTimerAnimation(
        opponentName: String, opponentImage: String, inviteeId: String?, opponentFoundText: String
    ) {
        with(binding) {
            ivOpponent.show()
            ivOpponent.loadOpponentImage(opponentImage)
            ivOpponent.setBackgroundColor(viewModel.opponentImageBackgroundColor)
            tvOpponentName.text = opponentName
            tvWaitTitle.text = opponentFoundText
            tvStatus.hide()

            animationOpponentSearch.cancelAnimation()
            animationOpponentSearch.hide()
            animationTimer.show()
            tvStartingIn.show()
            animationTimer.addAnimatorEndListener {
                mayNavigate {
                    val action = TbgOpponentWaitFragmentDirections.actionStartGame(
                        gameId = getGameId(),
                        inviterId = args.inviterId,
                        inviteeId = inviteeId,
                        topic = viewModel.topic,
                        isInviter = args.isInviter,
                        opponentName = opponentName,
                        opponentImage = opponentImage,
                        isOpponentBot = args.isOpponentBot,
                    )
                    navigate(action)
                }
            }
            animationTimer.playAnimation()
        }
    }

    private fun openOpponentUnavailableScreen(data: UnavailableData?) {
        data ?: return
        val action = NavGraphGameFlowDirections.actionOpponentUnavailable(data)
        navController.navigate(action)
    }

    private fun startInviteeWaitTimer(data: UnavailableData?, waitDuration: Long) {
        lifecycleScope.launch {
            delay(waitDuration)
            mayNavigate {
                openOpponentUnavailableScreen(data)
            }
        }
    }

    private fun getGameId(): String = args.gameId ?: viewModel.gameId
}