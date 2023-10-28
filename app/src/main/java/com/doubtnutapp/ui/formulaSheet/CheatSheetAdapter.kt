package com.doubtnutapp.ui.formulaSheet



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.CheatSheetData
import com.doubtnutapp.databinding.ItemCheatSheetBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class CheatSheetAdapter : RecyclerView.Adapter<CheatSheetAdapter.ViewHolder>() {

    var cheatSheetData = mutableListOf<CheatSheetData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_cheat_sheet, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cheatSheetData[position])


    }

    override fun getItemCount(): Int {
        return cheatSheetData.size
    }

    class ViewHolder(var binding: ItemCheatSheetBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(cheatSheetData: CheatSheetData) {
            binding.cheatSheetData = cheatSheetData
            binding.executePendingBindings()

//            binding.root.setOnClickListener {
//
//                if(cheatSheetData.subjectsName == Constants.CHEAT_SHEET){
//                    val intent = Intent(binding.root.context, CheatSheetActivity::class.java)
//                    binding.root.context.startActivity(intent)
//
//
//                }else{
//                    val intent = Intent(binding.root.context, FormulaSheetChapterActivity::class.java)
//                    intent.putExtra(Constants.FORMULA_SUBJECT_ID, cheatSheetData.subjectsId)
//                    intent.putExtra(Constants.FORMULA_SUBJECT_NAME, cheatSheetData.subjectsName)
//                    intent.putExtra(Constants.FORMULA_SUBJECT_ICON, cheatSheetData.subjectsImage)
//                    intent.putExtra(Constants.COLOR_INDEX, colorIndex)
//                    binding.root.context.startActivity(intent)
//                    sendEvent(EventConstants.EVENT_NAME_FORMULA_SHEET_SUBJECT_CLICK)
//                    sendEvent("""${EventConstants.EVENT_NAME_FORMULA_SHEET_SUBJECT_CLICK}${cheatSheetData.subjectsName}""")
//
//                }
//
//
//            }

        }

      
        private fun sendEvent(eventName : String){
            binding.root.context?.apply {
                (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                        .addNetworkState(NetworkUtils.isConnected(binding.root.context).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_FORMULA_SHEET_HOME_ACTIVITY)
                        .track()
            }
        }
    }

    fun updateData(cheatSheetData: ArrayList<CheatSheetData>) {
        this.cheatSheetData.addAll(cheatSheetData)
        notifyDataSetChanged()
    }


}

