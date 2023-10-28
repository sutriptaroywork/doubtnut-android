package com.doubtnutapp.ui.course

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnutapp.databinding.SheetSelectClassBinding
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class SelectClassDialog :
    BaseBindingBottomSheetDialogFragment<SelectClassViewModel, SheetSelectClassBinding>() {

    companion object {
        const val TAG = "SelectClassDialog"
        fun newInstance(): SelectClassDialog {
            return SelectClassDialog()
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    private lateinit var eventTracker: Tracker


    private lateinit var v: View
    val adapter = SelectClassAdapter()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        v = View.inflate(context, R.layout.sheet_select_class, null)

        dialog.setContentView(v)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)


        mBehavior = BottomSheetBehavior.from(v.parent as View)
        eventTracker = getTracker()

        showClassSheet()

        mBinding?.rvClasses!!.layoutManager =  GridLayoutManager(requireActivity(), 2)
        mBinding?.rvClasses!!.adapter = adapter

        mBinding?.rvClasses!!.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
               defaultPrefs(activity!!).edit {
                    putString(Constants.STUDENT_CLASS, adapter.classes!![position].name.toString())
                   putString(Constants.STUDENT_CLASS_DISPLAY, adapter.classes!![position].classDisplay.toString())

                   putString(Constants.STUDENT_CLASS_NCERT, adapter.classes!![position].name.toString())
                   putString(Constants.STUDENT_CLASS_DISPLAY_NCERT, adapter.classes!![position].classDisplay.toString())


               }
                if(parentFragment is ChaptersFragment) {
                    (parentFragment as ChaptersFragment)
                                .showCourseSheet(getStudentClass())
                }

                sendEventByClass(EventConstants.EVENT_NAME_CLASS_CLICK, adapter.classes!![position].name.toString())

                dialog.dismiss()
            }
        })

        dialog.setOnKeyListener { dialogInterface, i, keyEvent ->

            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {
//                dialog.onBackPressed()
                requireActivity().onBackPressed()
            }
            return@setOnKeyListener false
        }
        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<View>).state=BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<View>).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                    (mBehavior as BottomSheetBehavior<View>).state=BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
//                    setOffsetText(slideOffset)
//                    mBehavior.setT\\\
                }
            })
        }


        return dialog
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showClassSheet() {
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED

        val progressBar = v.findViewById<ProgressBar>(R.id.progressBarSheet)

        viewModel.getClassesWithSSC(defaultPrefs(requireActivity())
                .getString(Constants.STUDENT_LANGUAGE_CODE, "en").orDefaultValue())
                .observe(this, Observer { response ->
            when(response) {
                is Outcome.Progress -> { progressBar.visibility = View.VISIBLE }
                is Outcome.Failure -> {
                    progressBar.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")                }
                is Outcome.ApiError -> {
                    progressBar.visibility = View.GONE
                    toast(getString(R.string.api_error))

                }is Outcome.BadRequest -> {
                    progressBar.visibility = View.GONE
                val dialog = BadRequestDialog.newInstance("unauthorized")
                dialog.show(requireFragmentManager(), "BadRequestDialog")
            }
                is Outcome.Success -> {
                    updateClassData(response.data.data)
                    progressBar.visibility = View.GONE

                }
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun updateClassData(studentClass: ArrayList<StudentClass>) {

        val btnSSCName = studentClass[studentClass.lastIndex].name
        val btnSSCNameDisplay = studentClass[studentClass.lastIndex].classDisplay
        mBinding?.btnClassSSC?.visibility = View.VISIBLE
        mBinding?.btnClassSSC?.text = studentClass[studentClass.lastIndex].classDisplay.toString()
        studentClass.removeAt(studentClass.lastIndex)
        adapter.updateData(studentClass)

        mBinding?.btnClassSSC?.setOnClickListener {
            defaultPrefs(requireActivity()).edit {
                putString(Constants.STUDENT_CLASS, btnSSCName.toString())
                putString(Constants.STUDENT_CLASS_DISPLAY, btnSSCNameDisplay.toString())
                sendEventByClass(EventConstants.EVENT_NAME_CLASS_SSC_CLICK, btnSSCNameDisplay.toString())

            }
            if(parentFragment is ChaptersFragment) {
                (parentFragment as ChaptersFragment).showCourseSheet(getStudentClass())

            }
            dialog?.dismiss()

        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEventByClass(eventName : String, className:  String){
        activity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_SELECT_CLASS_DIALOG)
                    .addEventParameter(Constants.CLASS, className)
                    .track()
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetSelectClassBinding {
        return SheetSelectClassBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SelectClassViewModel {
        return ViewModelProviders.of(this).get(SelectClassViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}