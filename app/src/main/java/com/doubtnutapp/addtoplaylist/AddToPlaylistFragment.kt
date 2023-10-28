package com.doubtnutapp.addtoplaylist

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants

import com.doubtnutapp.databinding.DialogCreateNewPlaylistBinding
import com.doubtnutapp.databinding.FragmentAddToPlaylistBinding
import com.doubtnut.core.utils.toast
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnut.core.utils.viewModelProvider

/**
 * Created by Anand Gaurav on 2019-11-21.
 */
class AddToPlaylistFragment : BaseBindingBottomSheetDialogFragment<AddToPlaylistViewModel, FragmentAddToPlaylistBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "AddToPlaylistFragment"
        fun newInstance(id: String): AddToPlaylistFragment {
            val fragment = AddToPlaylistFragment()
            val args = Bundle()
            args.putString(Constants.QUESTION_ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    var id = ""

    private val adapter by lazy { AddToPlaylistAdapter(this) }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): AddToPlaylistViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddToPlaylistBinding =
        FragmentAddToPlaylistBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        id = arguments?.getString(Constants.QUESTION_ID) ?: ""

        binding.recyclerViewAddToPlaylist.adapter = adapter
        viewModel.fetchUserPlaylist()
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.createNewPlaylist.setOnClickListener {
            showCreateNewPlaylistDialog()
        }

        binding.buttonDone.setOnClickListener {
            if (adapter.listings.none { it.isChecked }) {
                toast("Select At Least one...")
            } else {
                val selectedList = adapter.listings.filter { it.isChecked }
                val ids = mutableListOf<String>()
                selectedList.forEach {
                    ids.add(it.id)
                }
                viewModel.submitPlayLists(id, ids)
            }
        }
    }

    override fun setupObservers() {
        viewModel.userPlaylistLiveData.observe(
            viewLifecycleOwner,
            EventObserver {
                adapter.updateList(it)
            }
        )

        viewModel.createAndAddToPlayList.observe(
            viewLifecycleOwner,
            EventObserver {
                if (it) {
                    toast("Added Successfully")
                    dismiss()
                }
            }
        )
    }

    override fun performAction(action: Any) {
    }

    private fun showCreateNewPlaylistDialog() {
        context?.let {
            Dialog(it).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                val dialogBinding = DialogCreateNewPlaylistBinding.inflate(LayoutInflater.from(it))
                setContentView(dialogBinding.root)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                show()
                dialogBinding.buttonCancel.setOnClickListener {
                    KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                    dismiss()
                }
                dialogBinding.buttonCreate.setOnClickListener {
                    val enteredPlayListName: String? = dialogBinding.editTextPlaylistName.text.toString()
                    if (enteredPlayListName.isNullOrBlank()) {
                        toast("Enter Playlist Name")
                    } else {
                        viewModel.createNewPlaylistAndAdd(id, enteredPlayListName)
                        KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                        dismiss()
                    }
                }
                try {
                    dialogBinding.editTextPlaylistName.requestFocus()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                } catch (e: Exception) {
                }
                setCancelable(false)
            }
        }
    }
}
