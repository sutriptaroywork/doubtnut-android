package com.doubtnutapp.teacherchannel.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.DownloadFailed
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.data.remote.models.teacherchannel.TeacherProfile
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.ChannelContentFilterWidget
import com.doubtnutapp.widgetmanager.widgets.ChannelSubTabFilterWidget
import com.doubtnutapp.widgetmanager.widgets.ChannelTabFilterWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class TeacherChannelViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var currentPage: Int = 1
    var teacherId: Int = 0
    var teacherType: String = ""
    var tabFilter: String = ""
    var subFilter: String = ""
    var contentFilter: String = ""
    var source: String = ""


    private val _event = MutableLiveData<Event<Any>>()
    val eventLiveData: LiveData<Event<Any>>
        get() = _event

    private val _subscribedState = MutableLiveData<Int>()
    val subscribedState: LiveData<Int>
        get() = _subscribedState

    private val _channelPageData = MutableLiveData<Outcome<Widgets>>()
    val channelPageData: LiveData<Outcome<Widgets>>
        get() = _channelPageData

    private val _teacherProfileData = MutableLiveData<Outcome<TeacherProfile>>()
    val teacherProfileData: LiveData<Outcome<TeacherProfile>>
        get() = _teacherProfileData

    fun initDataFromIntentExtra(extras: Bundle?) {
        teacherId = extras?.getInt(Constants.TEACHER_ID) ?: -1
        teacherType = extras?.getString(Constants.TEACHER_TYPE) ?: ""
        tabFilter = extras?.getString(Constants.TAB_FILTER) ?: ""
        subFilter = extras?.getString(Constants.SUB_FILTER) ?: ""
        contentFilter = extras?.getString(Constants.CONTENT_FILTER) ?: ""
        source = extras?.getString(Constants.SOURCE) ?: ""
    }

    fun handleAction(action: Any) {
        TODO("Not yet implemented")
    }

    fun fetchTeacherPageData() {
        _channelPageData.postValue(Outcome.loading(true))
        compositeDisposable.add(
            DataHandler.INSTANCE.teacherChannelRepository.getTeacherChannel(
                teacherId,
                teacherType,
                currentPage++,
                tabFilter,
                subFilter,
                contentFilter
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _channelPageData.postValue(Outcome.loading(false))
                    _channelPageData.postValue(Outcome.success(it.data))
                    if (!it.data.widgets.isNullOrEmpty()) {
                        for (widget in it.data.widgets) {
                            when (widget.type) {
                                WidgetTypes.TYPE_CHANNEL_FILTER_TAB -> {
                                    for(item in (widget.data as ChannelTabFilterWidget.ChannelFilterTabData).items){
                                        if (item.isActive == 1){
                                            tabFilter = item.key
                                            break
                                        }
                                    }
                                }
                                WidgetTypes.TYPE_CHANNEL_FILTER -> {
                                    for(item in (widget.data as ChannelSubTabFilterWidget.WidgetChannelFilterSubTabData).items){
                                        if (item.isActive == 1){
                                            subFilter = item.key
                                            break
                                        }
                                    }
                                }
                                WidgetTypes.TYPE_CHANNEL_FILTER_CONTENT -> {
                                    for(item in (widget.data as ChannelContentFilterWidget.ChannelContentFilterData).items){
                                        if (item.isActive == 1){
                                            contentFilter = item.key
                                            break
                                        }
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }, {
                    _channelPageData.postValue(Outcome.loading(false))
                    _channelPageData.postValue(Outcome.Failure(it))
                })
        )
    }

    fun fetchTeacherProfileData(teacherId: Int) {
        _teacherProfileData.postValue(Outcome.loading(true))
        compositeDisposable.add(
            DataHandler.INSTANCE.teacherChannelRepository.getTeacherProfile(
                teacherId
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _teacherProfileData.postValue(Outcome.loading(false))
                    _teacherProfileData.postValue(Outcome.success(it.data))
                }, {
                    _teacherProfileData.postValue(Outcome.loading(false))
                    _teacherProfileData.postValue(Outcome.Failure(it))
                })
        )
    }

    fun subscribeChannel(teacherId: Int, isSubscribe: Int) {
        compositeDisposable.add(
            DataHandler.INSTANCE.teacherChannelRepository.subscribeChannel(
                teacherId, isSubscribe
            ).subscribeOn(Schedulers.io())
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({
                    _subscribedState.postValue(1)
                })

        )
    }

    fun downloadPdf(url: String) {
        url?.let {
            var tempFilePathPdfDownload = getFileDestinationPath(DoubtnutApp.INSTANCE, url)

            when {
                FileUtils.isFilePresent(tempFilePathPdfDownload) -> saveFile(url)
                else -> compositeDisposable.add(
                    DataHandler.INSTANCE.pdfRepository.downloadPdf(
                        url,
                        tempFilePathPdfDownload!!
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            saveFile(url)
                        }, {
                            _event.postValue(Event(DownloadFailed("PDF Downloading failed, Please try later")))
                        })
                )
            }
        }
    }

    private fun saveFile(url: String) {
        val name = FileUtils.fileNameFromUrl(url)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, name)
        }
    }

    private fun getFileDestinationPath(context: Context, url: String): String {

        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }

        return FileUtils.EMPTY_PATH
    }

    fun updateTabFilter(tabFilterKey: String) {
        tabFilter = tabFilterKey
        subFilter = ""
        currentPage = 1
        fetchTeacherPageData()
    }

    fun updateSubFilter(subFilterKey: String) {
        subFilter = subFilterKey
        contentFilter = ""
        currentPage = 1
        fetchTeacherPageData()
    }

    fun updateContentFilter(contentFilterKey: String) {
        contentFilter = contentFilterKey
        currentPage = 1
        fetchTeacherPageData()
    }

}