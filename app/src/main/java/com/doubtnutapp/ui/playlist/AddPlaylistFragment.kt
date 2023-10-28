package com.doubtnutapp.ui.playlist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants.CREATE_NEW_PLAYLIST
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.AddPlaylistFragmentBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.playlist.event.PlaylistEventManager
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AddPlaylistFragment :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, AddPlaylistFragmentBinding>() {

    companion object {
        const val TAG = "AddPlaylistFragment"

        fun newInstance(qid: String): AddPlaylistFragment {
            val fragment = AddPlaylistFragment()
            val args = Bundle()
            args.putString(Constants.QUESTION_ID, qid)
            fragment.arguments = args
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    lateinit var qid: String

    @Inject
    lateinit var playlistEventManager: PlaylistEventManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val v = View.inflate(context, R.layout.add_playlist_fragment, null)

        dialog.setContentView(v)

        mBehavior = BottomSheetBehavior.from(v.parent as View)

        qid = requireArguments().getString(Constants.QUESTION_ID).orDefaultValue()
        playlistEventManager.onAddToPlaylistClicked(qid)

        val adapter = AddPlaylistAdapter()

        val rvPlaylists = v.findViewById<RecyclerView>(R.id.rvPlaylists)
        val progressBar = v.findViewById<ProgressBar>(R.id.progressBar)

        DataHandler.INSTANCE.playlistRepository.getPlaylists()
                .observe(this, Observer {
                    when (it) {
                        is Outcome.Progress -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is Outcome.Failure -> {
                            progressBar.visibility = View.GONE
                            toast(this.getString(R.string.poor_connection))
                        }
                        is Outcome.ApiError -> {
                            progressBar.visibility = View.GONE
                            apiErrorToast(it.e)

                        }
                        is Outcome.BadRequest -> {
                            progressBar.visibility = View.GONE
                            val dialog = BadRequestDialog.newInstance("unauthorized")
                            dialog.show(requireFragmentManager(), "BadRequestDialog")
                        }
                        is Outcome.Success -> {
                            progressBar.visibility = View.GONE

                            rvPlaylists.layoutManager = LinearLayoutManager(requireActivity())
                            rvPlaylists.addItemDecoration(
                                DividerItemDecoration(
                                    requireActivity(),
                                    DividerItemDecoration.VERTICAL
                                )
                            )

                            rvPlaylists.adapter = adapter

                            adapter.updateData(it.data.data)
                        }
                    }


                })

        rvPlaylists.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                rvPlaylists.visibility = View.GONE
                val map: HashMap<String, Any> = hashMapOf()
                map["question_id"] = qid
                map["playlist_id"] = adapter.name!![position].id
                if (::qid.isInitialized) {
                    playlistEventManager.onAddToPlaylistClick(qid)
                }
                DataHandler.INSTANCE.playlistRepository.addToPlaylist(
                        map.toRequestBody()).observe(this@AddPlaylistFragment, Observer {
                    when (it) {
                        is Outcome.Progress -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is Outcome.Failure -> {
                            rvPlaylists.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            val dialog = NetworkErrorDialog.newInstance()
                            dialog.show(fragmentManager!!, "NetworkErrorDialog")
                        }
                        is Outcome.ApiError -> {
                            progressBar.visibility = View.GONE
                            apiErrorToast(it.e)
                        }
                        is Outcome.BadRequest -> {
                            progressBar.visibility = View.GONE
                            val dialog = BadRequestDialog.newInstance("unauthorized")
                            dialog.show(fragmentManager!!, "BadRequestDialog")
                        }
                        is Outcome.Success -> {
                            progressBar.visibility = View.GONE

                            onBookmark()

                            ToastUtils.makeText(activity!!, getString(R.string.addedtoplaylist), Toast.LENGTH_SHORT).show()
                            dialog.dismiss()


                        }
                    }
                })
            }
        })


        binding.btnCreatePlaylist.setOnClickListener {
            playlistEventManager.eventWith(CREATE_NEW_PLAYLIST)
            Log.d("playlistName", binding.etPlaylistName.text.toString())
            if (binding.etPlaylistName.text.length > 0) {
                val map: HashMap<String, Any> = hashMapOf()
                map["playlist_name"] = binding.etPlaylistName.text.toString()
                map["question_id"] = qid
                DataHandler.INSTANCE.playlistRepository.createPlaylistAndAddItem(
                    map.toRequestBody()
                ).observe(this, Observer {
                    when (it) {
                        is Outcome.Progress -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is Outcome.Failure -> {
//                            rvPlaylists.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            toast(this.getString(R.string.poor_connection))
                        }
                        is Outcome.ApiError -> {
                            progressBar.visibility = View.GONE
                            toast(getString(R.string.api_error))

                        }
                        is Outcome.BadRequest -> {
                            progressBar.visibility = View.GONE
                            val dialog = BadRequestDialog.newInstance("unauthorized")
                            dialog.show(requireFragmentManager(), "BadRequestDialog")
                        }
                        is Outcome.Success -> {
                            progressBar.visibility = View.GONE
                            if (::qid.isInitialized) {
                                playlistEventManager.onAddToPlaylistClick(qid)
                            }
                            onBookmark()
                            ToastUtils.makeText(
                                requireActivity(),
                                "Added to playlist",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                    }
                })
            } else {
                ToastUtils.makeText(
                    requireActivity(),
                    "Playlist name cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        binding.tvCreatePlaylist.setOnClickListener {
            if (binding.etPlaylistName.visibility == View.GONE) {
                binding.etPlaylistName.visibility = View.VISIBLE
                binding.btnCreatePlaylist.visibility = View.VISIBLE
            } else if (binding.etPlaylistName.visibility == View.VISIBLE) {
                binding.etPlaylistName.visibility = View.GONE
                binding.btnCreatePlaylist.visibility = View.GONE
            }

        }

        binding.btnClose.setOnClickListener {
            mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            dialog.dismiss()
        }

        return dialog
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun onBookmark() {
        if (parentFragment is SimilarVideoFragment) {

        } else {
            //it cast to ViewAnswerActivity if it is possible, otherwise it will return null.
            val viewAnswerActivity = activity as? VideoPageActivity
            //call onBookmark if viewAnswerActivity is not null.
            viewAnswerActivity?.onBookmark()
        }


    }

    private fun addToPlaylist(qid: String, playlistId: String) {

    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AddPlaylistFragmentBinding {
        return AddPlaylistFragmentBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }


}
