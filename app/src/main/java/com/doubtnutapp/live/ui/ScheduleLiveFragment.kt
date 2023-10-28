package com.doubtnutapp.live.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.Status
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.feed.view.AddTopicDialog
import com.doubtnutapp.feed.viewmodel.CreatePostViewModel
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.FilePathUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_schedule_live.*
import kotlinx.android.synthetic.main.fragment_schedule_live.btnAddTopic
import kotlinx.android.synthetic.main.fragment_schedule_live.progressBar
import java.util.*
import javax.inject.Inject

class ScheduleLiveFragment : Fragment() {

    companion object {
        val TYPE_GO_LIVE_NOW = "go_live"
        val TYPE_SCHEDULE_LIVE = "schedule_live"

        private const val PICK_IMAGE = 111

    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CreatePostViewModel

    private var pendingAction: (() -> Unit)? = null

    private var liveAction: String = TYPE_GO_LIVE_NOW

    private var addTopicFragment: AddTopicDialog? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule_live, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(activity!!, R.color.colorSecondaryDark)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreatePostViewModel::class.java)
        viewModel.getTopics()
        viewModel.addLivePost()
        setObservers()
        setClickListeners()
        Utils.setMaxLinesEditText(etDescription, 5)
    }

    private fun setObservers() {
        viewModel.postDataLiveData.observe(viewLifecycleOwner, Observer {
            updateUIData(it)
        })

        viewModel.createdPostLiveData.observe(viewLifecycleOwner, Observer {
            handleLivePostCreated(it)
        })

        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    progressBar.hide()
                }
                Status.NONE -> {
                    progressBar.hide()
                }
                Status.LOADING -> {
                    progressBar.show()
                }
                Status.ERROR -> {
                    progressBar.hide()
                    showToast(activity, it.message!!)
                }
            }
        })

        viewModel.topicsLiveData.observe(viewLifecycleOwner, Observer {
            addTopicFragment?.updateTopics(viewModel.postDataLiveData.value!!.type, it)
        })

    }

    private fun handleLivePostCreated(livePostItem: FeedPostItem) {
        if (liveAction == TYPE_GO_LIVE_NOW) {
            startActivity(LiveActivity.getStartIntent(activity!!,
                    LiveActivity.TYPE_LIVE_STREAM_PUBLISH,
                    Bundle().apply {
                        putString(Constants.POST_ID, livePostItem.id)
                    }))
            activity!!.finish()
        } else if (liveAction == TYPE_SCHEDULE_LIVE) {
            ScheduleLiveConfirmationDialog(livePostItem).show(childFragmentManager,
                    "ScheduleLiveConfirmation")
        }
    }

    private fun setClickListeners() {

        btnScheduleLive.setOnClickListener {
            if (validatePrice() && validateSchedule()) {
                updatePrice()
                liveAction = TYPE_SCHEDULE_LIVE
                viewModel.createPost(etDescription.text.toString())
            }
        }

        btnGoLive.setOnClickListener {
            if (validatePrice()) {
                updatePrice()
                liveAction = TYPE_GO_LIVE_NOW
                viewModel.createPost(etDescription.text.toString())
            }
        }

        viewPostImage.setOnClickListener {
            addImage()
        }

        etDate.setOnClickListener {
            val listener = DatePickerDialog.OnDateSetListener { picker, year, month, day ->
                DateUtils.formatDate(year, month, day).also {
                    etDate.setText(it)
                    viewModel.addLivePostSchedule(it)
                }
            }
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(activity!!, listener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        etTimeStart.setOnClickListener {
            val listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay: Int, minute: Int ->
                DateUtils.formatDayTime(hourOfDay, minute).also {
                    etTimeStart.setText(it)
                    viewModel.addLivePostSchedule(null, it, null)
                }
            }
            val timePickerDialog = TimePickerDialog(activity!!, listener, 0, 0, false)
            timePickerDialog.show()
        }

        etTimeEnd.setOnClickListener {
            val listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay: Int, minute: Int ->
                DateUtils.formatDayTime(hourOfDay, minute).also {
                    etTimeEnd.setText(it)
                    viewModel.addLivePostSchedule(null, null, it)
                }
            }
            val timePickerDialog = TimePickerDialog(activity!!, listener, 0, 0, false)
            timePickerDialog.show()
        }

        priceRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            updatePrice()
            etPrice.isEnabled = radioGroup.checkedRadioButtonId == R.id.btnPaid
        }

        btnAddTopic.setOnClickListener {
            addTopicFragment = AddTopicDialog(viewModel.postDataLiveData.value!!.type,
                    viewModel.topicsLiveData.value ?: hashMapOf()) {
                viewModel.addTopic(it)
            }.also {
                it.show(childFragmentManager, "AddTopicDialog")
            }
        }

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun addImage() {
        if (hasPermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        } else {
            pendingAction = this::addImage
            requestPermission()
        }
    }

    private fun updatePrice() {
        if (btnFree.isChecked || etPrice.text.toString().isEmpty()) {
            viewModel.addLivePostPrice(0)
        } else {
            viewModel.addLivePostPrice(etPrice.text.toString().toInt())
        }
    }

    private fun validatePrice(): Boolean {
        if (btnPaid.isChecked && etPrice.text.toString().isEmpty()) {
            toast("Please enter amount")
            return false
        }
        return true
    }

    private fun validateSchedule(): Boolean {
        if (etDate.text.isEmpty() || etTimeStart.text.isEmpty() || etTimeEnd.text.isEmpty()) {
            toast("Please enter schedule date and time")
            return false
        }
        return true
    }

    private fun updateUIData(postData: CreatePostViewModel.PostData) {
        if (postData.images.isNotEmpty()) {
            ivPostImage.loadImage(postData.images[0])
        }
        if (postData.topic != null) {
            btnAddTopic.setCompoundDrawables(null, null, null, null)
            btnAddTopic.setPadding(30, 8, 30, 8)
            btnAddTopic.textSize = 15f
            btnAddTopic.text = postData.topic
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_STORAGE_PERMISSION
                && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingAction != null) {
                pendingAction!!()
            }
        } else {
            toast(getString(R.string.needstoragepermissions))
        }

    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        var permissions = arrayOf<String>()
        permissions += Manifest.permission.READ_EXTERNAL_STORAGE

        requestPermissions(permissions, Constants.REQUEST_STORAGE_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            when (requestCode) {
                PICK_IMAGE -> {
                    val selectedImage = data.data!!
                    val imagePath = FilePathUtils.getRealPath(activity!!, selectedImage)
                    if (imagePath != null) {
                        viewModel.clearAttachments()
                        viewModel.addImage(imagePath)
                        // update post type back to live
                        viewModel.addLivePost()
                    }
                }
            }
        }
    }
}