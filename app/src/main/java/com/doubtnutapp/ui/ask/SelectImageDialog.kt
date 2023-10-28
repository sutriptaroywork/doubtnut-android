package com.doubtnutapp.ui.ask

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnutapp.R
import com.doubtnutapp.data.common.BitmapDiskSerializeWrapper
import com.doubtnutapp.databinding.SheetSelectImageBinding
import com.doubtnutapp.hide
import com.doubtnutapp.matchquestion.listener.OnImageSelectListener
import com.doubtnut.core.utils.toast
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.course.SelectCourseAdapter
import com.doubtnutapp.ui.course.SelectImageViewModel
import com.doubtnutapp.utils.RotateTransformation
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SelectImageDialog :
    BaseBindingBottomSheetDialogFragment<SelectImageViewModel, SheetSelectImageBinding>() {

    companion object {
        private const val IMAGE_BITMAP = "image_bitmap"

        const val TAG = "SelectImageDialog"

        fun newInstance(questionBitmap: Bitmap): SelectImageDialog {
            val fragment = SelectImageDialog()
            val args = Bundle()
            args.putParcelable(IMAGE_BITMAP, BitmapDiskSerializeWrapper(questionBitmap))
            fragment.arguments = args
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null

    private var onImageSelectListener: OnImageSelectListener? = null

    private lateinit var v: View
    val adapter = SelectCourseAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnImageSelectListener) onImageSelectListener = context
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    @SuppressLint("CheckResult")
    private fun showImageSheet(view: View, imageBitmap: Bitmap?) {

        imageBitmap?.let {

            val bitmapMinus90 = Glide.with(this)
                .asBitmap()
                .load(it)
                .apply(RequestOptions().transform(RotateTransformation(-90F)))

            val bitmapPlus90 = Glide.with(this)
                .asBitmap()
                .load(it)
                .apply(RequestOptions().transform(RotateTransformation(90F)))

            val originalBitmap = Glide.with(this)
                .asBitmap()
                .load(it)


            bitmapMinus90.into(mBinding?.ivminus90!!)
            bitmapPlus90.into(mBinding?.ivplus90!!)

            originalBitmap.into(mBinding?.ivOriginal!!)

            mBinding?.selectImgProgressbar?.hide()
            mBinding?.ivplus90?.setOnClickListener {
                Single.fromCallable { bitmapPlus90.submit().get() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { bitmap ->
                        if (bitmap != null) {
                            onImageSelectListener?.onImageSelected(bitmap, 90)
                            dialog?.dismiss()
                            Lifecycle.Event.ON_DESTROY
                        } else {
                            toast("Error")
                        }
                    }
            }
            mBinding?.ivminus90?.setOnClickListener {
                Single.fromCallable { bitmapMinus90.submit().get() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { bitmap ->
                        if (bitmap != null) {
                            onImageSelectListener?.onImageSelected(bitmap, -90)
                            dialog?.dismiss()
                            Lifecycle.Event.ON_DESTROY
                        } else {
                            toast("Error")
                        }
                    }

            }
            mBinding?.ivOriginal?.setOnClickListener {
                Single.fromCallable { originalBitmap.submit().get() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { bitmap ->
                        if (bitmap != null) {
                            onImageSelectListener?.onImageSelected(bitmap, 0)
                            dialog?.dismiss()
                            Lifecycle.Event.ON_DESTROY
                        } else {
                            toast("Error")
                        }
                    }
            }
        }
    }

    fun setOnImageSelectListener(listener: OnImageSelectListener) {
        onImageSelectListener = listener
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetSelectImageBinding {
        return SheetSelectImageBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): SelectImageViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        v = View.inflate(context, R.layout.sheet_select_image, null)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        val imageBitmap =
            requireArguments().getParcelable<BitmapDiskSerializeWrapper>(IMAGE_BITMAP)!!.imageBitmap
        showImageSheet(v, imageBitmap)
        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
//                    setOffsetText(slideOffset)
//                    mBehavior.setT\\\
                }
            })
        }
        dialog?.setOnKeyListener { dialogInterface, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {
                activity?.finish()
            }
            return@setOnKeyListener false
        }
    }
}