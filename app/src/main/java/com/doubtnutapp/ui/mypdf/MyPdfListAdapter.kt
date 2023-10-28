package com.doubtnutapp.ui.mypdf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R

class MyPdfListAdapter(private val myPdfList: List<PdfFile>, val sharePDFListener: SharePDFListener) :
    RecyclerView.Adapter<MyPdfItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPdfItemViewHolder {
        return MyPdfItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_my_pdf,
                parent,
                false
            ), sharePDFListener
        )
    }

    override fun getItemCount() = myPdfList.size

    override fun onBindViewHolder(holder: MyPdfItemViewHolder, position: Int) {
        holder.bind(myPdfList[position])
    }
}
