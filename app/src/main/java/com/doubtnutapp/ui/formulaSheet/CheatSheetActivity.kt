package com.doubtnutapp.ui.formulaSheet


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.EXISTING
import com.doubtnut.analytics.EventConstants.NEW
import com.doubtnut.analytics.EventConstants.VIEW_EXISTING_CHEAT_SHEETS
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.repository.FormulaSheetRepository
import com.doubtnutapp.databinding.ActivityCheatSheetActivityBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import javax.inject.Inject

class CheatSheetActivity :
    BaseBindingActivity<FormulaSheetViewModel, ActivityCheatSheetActivityBinding>() {

    private lateinit var adapter: CheatSheetAdapter
    private lateinit var eventTracker: Tracker
    val mapItem: HashMap<String, String> = hashMapOf()
    private var addToCheatSheet = false


    @Inject
    lateinit var formulaSheetRepository: FormulaSheetRepository
    private val TAG = "CheatSheetActivity"
    override fun provideViewBinding(): ActivityCheatSheetActivityBinding =
        ActivityCheatSheetActivityBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FormulaSheetViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        eventTracker = getTracker()

        val itemId = intent?.getStringExtra(Constants.FORMULA_ITEM_ID)
        val type = intent?.getStringExtra(Constants.SEARCH_TYPE)

        if (!itemId.isNullOrBlank() && !type.isNullOrBlank()) {
            mapItem["search_type"] = type!!
            mapItem["id"] = itemId!!
            addToCheatSheet = true
        } else {
            addToCheatSheet = false
        }

        getCheatSheet()
        adapter = CheatSheetAdapter()
        binding.rvCheatSheet.layoutManager = LinearLayoutManager(this)
        binding.rvCheatSheet.adapter = adapter

        setSupportActionBar(binding.toolbarFormulas)
        supportActionBar!!.title = Constants.CHEAT_SHEET
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.tvCreatePlaylist.setOnClickListener {
            if (binding.etCheatSheetName.visibility == View.GONE) {
                binding.etCheatSheetName.visibility = View.VISIBLE
                binding.btnCreateCheatSheet.visibility = View.VISIBLE
            } else if (binding.etCheatSheetName.visibility == View.VISIBLE) {
                binding.etCheatSheetName.visibility = View.GONE
                binding.btnCreateCheatSheet.visibility = View.GONE
            }
        }

        binding.btnCreateCheatSheet.setOnClickListener {
            Log.d("playlistName", binding.etCheatSheetName.text.toString())
            if (binding.etCheatSheetName.text.isNotEmpty()) {
                val map: HashMap<String, Any> = hashMapOf()
                map["cheatsheet_name"] = binding.etCheatSheetName.text.toString()
                map["formulas_to_add"] = arrayListOf(mapItem)
                map["cheatsheet_id"] = "0"
                map["is_edit"] = "0"
                if (mapItem.size != 0) {
                    viewModel.publishOnAddToCheatSheetEvent(
                        binding.etCheatSheetName.text.toString(),
                        NEW
                    )
                }
                addToCheatSheet(map)
            } else {
                ToastUtils.makeText(
                    this,
                    getText(R.string.cheat_sheet_may_not_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.rvCheatSheet.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (addToCheatSheet) {
                    val map: HashMap<String, Any> = hashMapOf()
                    map["cheatsheet_name"] = adapter.cheatSheetData[position].cheatSheetName
                    map["formulas_to_add"] = arrayListOf(mapItem)
                    map["cheatsheet_id"] = adapter.cheatSheetData[position].cheatSheetId
                    map["is_edit"] = "1"
                    viewModel.publishOnAddToCheatSheetEvent(
                        adapter.cheatSheetData[position].cheatSheetName,
                        EXISTING
                    )
                    if (adapter.cheatSheetData[position].isGeneric != 1) {
                        addToCheatSheet(map)
                    } else {
                        ToastUtils.makeText(
                            this@CheatSheetActivity,
                            getText(R.string.cannot_add_to_cheat_sheet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val intent =
                        Intent(this@CheatSheetActivity, CheatSheetFormulaListActivity::class.java)
                    intent.putExtra(
                        Constants.CLICKED_ITEM_NAME,
                        adapter.cheatSheetData[position].cheatSheetName
                    )
                    intent.putExtra(
                        Constants.FORMULA_ITEM_ID,
                        adapter.cheatSheetData[position].cheatSheetId
                    )
                    startActivity(intent)
                }
            }
        })

        if (mapItem.size == 0)
            viewModel.eventWith(VIEW_EXISTING_CHEAT_SHEETS, ignoreSnowplow = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.statusBarColor)

    }


    private fun getCheatSheet() {
        viewModel.getCheatSheet().observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarCheatSheet.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarCheatSheet.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_FAILURE)
                }
                is Outcome.ApiError -> {
                    binding.progressBarCheatSheet.visibility = View.GONE
                    toast(getString(R.string.api_error))
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_API_ERROR)
                }
                is Outcome.Success -> {
                    adapter.updateData(response.data.data)
                    binding.progressBarCheatSheet.visibility = View.GONE
                    sendEvent(EventConstants.EVENT_NAME_GET_FORMULA_HOME_CALL_SUCCESS)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun sendEvent(eventName: String) {
        this@CheatSheetActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@CheatSheetActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_FORMULA_SHEET_HOME_ACTIVITY)
                .track()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@CheatSheetActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun addToCheatSheet(map: HashMap<String, Any>) {
        formulaSheetRepository.addCheatsheet(
            map.toRequestBody()
        ).observe(this, Observer {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarCheatSheet.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarCheatSheet.visibility = View.GONE
                    toast(this.getString(R.string.poor_connection))
                }
                is Outcome.ApiError -> {
                    binding.progressBarCheatSheet.visibility = View.GONE
                    toast(getString(R.string.api_error))

                }
                is Outcome.BadRequest -> {
                    binding.progressBarCheatSheet.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    viewModel.publishOnAddToCheatSheetSuccessFullEvent()
                    binding.progressBarCheatSheet.visibility = View.GONE
                    ToastUtils.makeText(
                        this,
                        getText(R.string.added_to_cheatsheet),
                        Toast.LENGTH_SHORT
                    ).show()
                    adapter.notifyDataSetChanged()
                    this.finish()
                }
            }
        })
    }

}