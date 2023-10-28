package com.doubtnutapp.feed.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.databinding.AddPostTopicLayoutBinding
import com.doubtnutapp.databinding.ItemPostTopicBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.HashMap

class AddTopicDialog(
    val type: String,
    val topics: HashMap<String, List<String>?>,
    val topicSelectedListener: (topic: String) -> Unit
) : BaseBindingBottomSheetDialogFragment<DummyViewModel, AddPostTopicLayoutBinding>() {

    private var mBehavior: BottomSheetBehavior<*>? = null

    companion object {
        private const val TAG = "AddTopicDialog"
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupTopicList(type: String, topics: HashMap<String, List<String>?>?) {
        val topicList = topics?.get(type) ?: return
        mBinding?.rvTopics?.layoutManager = LinearLayoutManager(requireActivity())
        mBinding?.rvTopics?.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        mBinding?.rvTopics?.adapter = TopicsAdapter(topicList) {
            topicSelectedListener(it)
            dismiss()
        }
        mBinding?.topicsProgressBar?.hide()
    }

    internal class TopicsAdapter(
        private val topics: List<String>,
        val topicSelected: (topic: String) -> Unit
    ) : RecyclerView.Adapter<TopicsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                ItemPostTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            holder.binding.tvTopic.apply {
                text = topics[position]
                setOnClickListener {
                    topicSelected(topics[position])
                }
            }
        }

        override fun getItemCount(): Int {
            return topics.size
        }

        internal inner class ViewHolder(val binding: ItemPostTopicBinding) : RecyclerView.ViewHolder(binding.root)
    }


    fun updateTopics(type: String, topics: HashMap<String, List<String>?>?) {
        setupTopicList(type, topics)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AddPostTopicLayoutBinding {
        return AddPostTopicLayoutBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        dialog?.setCanceledOnTouchOutside(false)
        setupTopicList(type, topics)
    }
}