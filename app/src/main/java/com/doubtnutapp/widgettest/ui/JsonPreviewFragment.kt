package com.doubtnutapp.widgettest.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.databinding.FragmentJsonPreviewBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgettest.data.DummyJson.DUMMY_WIDGET_JSON
import com.doubtnutapp.widgettest.viewmodel.ApiTestViewModel
import org.json.JSONArray
import java.lang.Exception

/**
 * Created by Mehul Bisht on 18/11/21
 */

class JsonPreviewFragment : BaseBindingFragment<ApiTestViewModel, FragmentJsonPreviewBinding>() {

    private var isExpanded = true
    private lateinit var jsonArray: JSONArray
    private var text = ""
    private var hint = "Json response [ ]\n(must be in the form of a json array [ ])"

    companion object {
        const val TAG = "JsonPreviewFragment"
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ApiTestViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentJsonPreviewBinding =
        FragmentJsonPreviewBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        init()

        val jsonView = binding.jsonView
        resetJsonView()

        binding.makeRequest.setOnClickListener {
            text = binding.query.text.toString().trim()
            previewClick()
            if (text.isEmpty()) {
                ToastUtils.makeText(requireContext(), "Please enter a valid json", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.fetch(text)
            }
        }

        binding.clear.setOnClickListener {
            resetEditor()
            resetHint()
            resetJsonView()
            viewModel.reset()
        }

        binding.previewJson.setOnClickListener {
            previewClick()
        }

        binding.toggle.setOnClickListener {
            if (isExpanded) {
                jsonView.collapseJson()
                binding.toggle.text = "Expand All"
            } else {
                jsonView.expandJson()
                binding.toggle.text = "Collapse All"
            }
            isExpanded = !isExpanded
        }

        binding.share.setOnClickListener {
            shareText()
        }
    }

    private fun init() {
        resetHint()
        binding.query.setText(DUMMY_WIDGET_JSON)
        previewClick()
    }

    private fun hasValidJson(): Boolean {
        text = binding.query.text.toString().trim()

        if (text.isEmpty())
            return false

        try {
            val jsonArray = JSONArray(text)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    private fun shareText() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        val chooser = Intent.createChooser(intent, "Select an app to proceed")
        startActivity(chooser)
    }

    private fun previewClick() {
        if (hasValidJson())
            updateJsonView(text)
        else
            resetJsonView()
    }

    private fun updateJsonView(text: String) {
        jsonArray = JSONArray(text)
        binding.jsonView.setJson(jsonArray)
    }

    private fun resetJsonView() {
        updateJsonView("[]")
    }

    private fun resetHint() {
        binding.query.hint = hint
    }

    private fun resetEditor() {
        binding.query.setText("")
    }
}