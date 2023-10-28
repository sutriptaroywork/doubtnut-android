package com.doubtnutapp.ui.mypdf

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.databinding.ItemMyPdfBinding
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import java.io.File

class MyPdfItemViewHolder(val binding: ItemMyPdfBinding, sharePDFListener: SharePDFListener) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.sharePDFListener = sharePDFListener
    }

    private val eventTracker: Tracker =
        (binding.root.context.applicationContext as DoubtnutApp).getEventTracker()

    fun bind(myPfdFile: PdfFile) {
        binding.pdfFile = myPfdFile
        binding.root.setOnClickListener {
            val packageName =
                myPfdFile.fileName.replace("[-+.^:,]", "").replace(".pdf", "").replace(" ", "_")
            openFile(myPfdFile.filePath)

            sendEvent(it.context, EventConstants.EVENT_NAME_MY_PDF_OPEN)
            sendEvent(it.context, "${EventConstants.EVENT_NAME_MY_PDF_OPEN}$packageName")
        }
        binding.executePendingBindings()
    }

    private fun openFile(filePath: String) {
        val pdfUri = FileProvider.getUriForFile(
            binding.root.context,
            BuildConfig.AUTHORITY,
            File(filePath)
        )
        Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(pdfUri, "application/pdf")
        }.also {
            if (AppUtils.isCallable(binding.root.context, it)) {
                binding.root.context.startActivity(it)
            } else {
                ToastUtils.makeText(
                    binding.root.context,
                    R.string.string_install_whatsApp,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendEvent(context: Context, eventName: String) {
        eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.SCREEN_NAME_MY_PDF)
            .track()
    }
}
