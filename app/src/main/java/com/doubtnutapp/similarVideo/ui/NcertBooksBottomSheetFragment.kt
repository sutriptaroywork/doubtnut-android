package com.doubtnutapp.similarVideo.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.databinding.FragmentNcertBookBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.similarVideo.viewmodel.NcertSimilarViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class NcertBooksBottomSheetFragment : BottomSheetDialogFragment(), ActionPerformer {

    companion object {

        const val TAG = "NcertBooksBottomSheetFragment"
        const val SOURCE_NCERT_BOOK_BOTTOM_SHEET = "ncert_book_bottom_sheet"
        const val BOTTOM_SHEET_DETAILS = "bottom_sheet_details"

        fun newInstance(bottomSheetDetails: BottomSheetDetails) =
            NcertBooksBottomSheetFragment().apply {
                val bundle = Bundle()
                bundle.putParcelable(BOTTOM_SHEET_DETAILS, bottomSheetDetails)
                arguments = bundle
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: NcertSimilarViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(NcertSimilarViewModel::class.java)
    }

    private var _binding: FragmentNcertBookBinding? = null
    private val binding
        get() = _binding!!

    private var bottomSheetDetails: BottomSheetDetails? = null

    private val booksAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(requireContext(), this, SOURCE_NCERT_BOOK_BOTTOM_SHEET)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bottomSheetDetails = arguments?.getParcelable(BOTTOM_SHEET_DETAILS)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_ncert_book, container, false)
        _binding = FragmentNcertBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
        setUpObservers()
    }

    private fun setUpUi() {
        bottomSheetDetails?.let { sheetDetails ->

            sheetDetails.title?.let { title ->
                binding.tvTitle.show()
                binding.tvTitle.text = resources.getString(title)
            } ?: binding.tvTitle.hide()

            sheetDetails.titleSize?.let { size ->
                binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            }

            sheetDetails.titleColor?.let { color ->
                binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), color))
            }

            sheetDetails.subtitle?.let { subtitle ->
                binding.tvSubtitle.show()
                binding.tvSubtitle.text = resources.getString(subtitle)
            }

            sheetDetails.subtitleSize?.let { size ->
                binding.tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            }

            sheetDetails.subtitleColor?.let { color ->
                binding.tvSubtitle.setTextColor(ContextCompat.getColor(requireContext(), color))
            }
        }
    }

    private fun setUpObservers() {
        viewModel.bookWidgetData.observe(viewLifecycleOwner) { list ->
            binding.rvNcertBooks.adapter = booksAdapter
            booksAdapter.setWidgets(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun performAction(action: Any) {
        dismiss()
        viewModel.handleAction(action)
    }

    @Keep
    @Parcelize
    data class BottomSheetDetails(
        @StringRes val title: Int?,
        val titleSize: Float? = null,
        @ColorRes val titleColor: Int? = null,
        @StringRes val subtitle: Int? = null,
        val subtitleSize: Float? = null,
        @ColorRes val subtitleColor: Int? = null
    ) : Parcelable
}