package com.doubtnutapp.liveclass.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.LiveClassAnnouncementData
import com.doubtnutapp.databinding.FragmentTeacherCommBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveClassCommFragment :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentTeacherCommBinding>(),
    ActionPerformer {

    private lateinit var v: View

    @Inject
    lateinit var disposable: CompositeDisposable

    companion object {
        const val TAG = "LiveClassCommFrag"
        private const val ANNOUNCEMENT_DATA = "announcement_data"
        fun newInstance(data: LiveClassAnnouncementData): LiveClassCommFragment {
            return LiveClassCommFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ANNOUNCEMENT_DATA, data)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun handleTimer(count: Long) {
        disposable.add(
                getTimerObservable(count)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Long>() {
                            override fun onComplete() {
                                disposable.clear()
                                doOnTimerComplete()
                            }

                            override fun onNext(t: Long) {
                                val timerText = (count - t).toString() + " Sec"
                                mBinding?.textViewTimer?.text = timerText
                            }

                            override fun onError(e: Throwable) {
                                disposable.clear()
                                dismiss()
                            }
                        })
        )
    }

    fun doOnTimerComplete() {
        dismiss()
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    private fun initUI() {
        val announcementData =
            arguments?.getParcelable<LiveClassAnnouncementData>(ANNOUNCEMENT_DATA)
        mBinding?.tvTeacherName?.text = announcementData?.title
        mBinding?.tvTeacherText?.text = announcementData?.description
        mBinding?.teacherImage?.loadImage(announcementData?.imageUrl)
        mBinding?.closeButton?.setOnClickListener {
            dismiss()
        }
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        handleTimer(announcementData?.expiry ?: 0)
    }

    override fun performAction(action: Any) {
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTeacherCommBinding {
        return FragmentTeacherCommBinding.inflate(inflater, container, false)
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