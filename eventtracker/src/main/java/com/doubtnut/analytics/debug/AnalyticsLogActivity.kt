package com.doubtnut.analytics.debug

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.R
import com.doubtnut.core.utils.ToastUtils

class AnalyticsLogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AnalyticsInterceptor.isEnabled) return
        setContentView(R.layout.activity_analytics_log)
        findViewById<RecyclerView>(R.id.rvLogs).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvLogs).addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val logs = AnalyticsInterceptor.analyticsEvents
        val adapter = AnalyticsLogAdapter().apply {
            addLogs(logs)
        }
        findViewById<RecyclerView>(R.id.rvLogs).adapter = adapter

        findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.setLogs(
                    logs.filter {
                        it.toString().contains(s ?: "")
                    }
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    class AnalyticsLogAdapter : RecyclerView.Adapter<AnalyticsLogAdapter.LogViewHolder>() {

        private val logs: ArrayList<AnalyticsInterceptor.Event> = arrayListOf()

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.itemView.findViewById<TextView>(R.id.tvEventName).text = logs[position].eventName + " ${logs[position].destination}"
            holder.itemView.findViewById<TextView>(R.id.tvEventParams).text = logs[position].params.toString()
            holder.itemView.findViewById<TextView>(R.id.textViewCopy).setOnClickListener {
                val clipboard = holder.itemView.context
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText(
                    logs[position].eventName + " ${logs[position].destination}",
                    logs[position].eventName + " ${logs[position].destination}" + logs[position].params
                )
                clipboard?.setPrimaryClip(clip)
                ToastUtils.makeText(holder.itemView.context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            return LogViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_analytics_log, parent, false))
        }

        override fun getItemCount(): Int {
            return logs.size
        }

        fun addLog(item: AnalyticsInterceptor.Event) {
            this.logs.add(item)
            notifyDataSetChanged()
        }

        fun addLogs(items: List<AnalyticsInterceptor.Event>) {
            this.logs.addAll(items)
            notifyDataSetChanged()
        }

        fun setLogs(items: List<AnalyticsInterceptor.Event>) {
            logs.clear()
            logs.addAll(items)
            notifyDataSetChanged()
        }

        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
