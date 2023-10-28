package com.doubtnutapp.ui.course

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
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
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.Course
import com.doubtnutapp.databinding.SheetSelectCourseBinding
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class SelectCourseDialog :
    BaseBindingBottomSheetDialogFragment<SelectCourseViewModel, SheetSelectCourseBinding>() {

    companion object {
        const val TAG = "SelectCourseDialog"
        fun newInstance(clazz: ArrayList<Course>): SelectCourseDialog {

            val fragment  = SelectCourseDialog()
            val args = Bundle()
            args.putParcelableArrayList(Constants.STUDENT_COURSE_LIST, clazz as ArrayList<Parcelable> )
            fragment.arguments = args
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    private lateinit var eventTracker: Tracker


    private lateinit var v: View
    val adapter = SelectCourseAdapter()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        v = View.inflate(context, R.layout.sheet_select_course, null)
        eventTracker = getTracker()

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)


        mBehavior = BottomSheetBehavior.from(v.parent as View)

        val courseList: ArrayList<Course> = requireArguments().getParcelableArrayList<Course>(Constants.STUDENT_COURSE_LIST) as ArrayList<Course>

        mBinding?.rvCourses!!.layoutManager = GridLayoutManager(requireActivity(), 2)
        mBinding?.rvCourses!!.adapter = adapter
        adapter.updateData(courseList)


//        showCourseSheet(getStudentClass())

        mBinding?.rvCourses?.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
               defaultPrefs(activity!!).edit {
                    putString(Constants.STUDENT_COURSE, adapter.courses!![position].course)
                }
                if(parentFragment is ChaptersFragment){
                    (parentFragment as ChaptersFragment).updateCourse()
                }
                sendEventByCourse(EventConstants.EVENT_NAME_COURSE_CLICK, adapter.courses!![position].course)

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

    private fun showCourseSheet(clazz: String) {
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED

        val progressBar = v.findViewById<ProgressBar>(R.id.progressBarSheet)

        viewModel.getCourses(clazz).observe(this, Observer { response ->
            when(response) {
                is Outcome.Progress -> { progressBar.visibility = View.VISIBLE }
                is Outcome.Failure -> {
                    progressBar.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")                }
                is Outcome.ApiError -> {
                    progressBar.visibility = View.GONE
                    apiErrorToast(response.e)

                }is Outcome.BadRequest -> {
                    progressBar.visibility = View.GONE
                val dialog = BadRequestDialog.newInstance("unauthorized")
                dialog.show(requireFragmentManager(), "BadRequestDialog")
            }
                is Outcome.Success -> {

                    if (response.data.data.size == 1)
                        mBinding?.rvCourses!!.layoutManager = GridLayoutManager(
                            requireActivity(),
                            1
                        ) else mBinding?.rvCourses!!.layoutManager =
                        GridLayoutManager(requireActivity(), 2)
                    mBinding?.rvCourses!!.adapter = adapter
                    adapter.updateData(response.data.data)
                    progressBar.visibility = View.GONE

                }
            }
        })

    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEventByCourse(eventName : String, courseName:  String){
        activity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_SELECT_COURSE_DIALOG)
                    .addEventParameter(Constants.COURSE, courseName)
                    .track()
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetSelectCourseBinding {
        return SheetSelectCourseBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SelectCourseViewModel {
      return ViewModelProviders.of(this).get(SelectCourseViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}