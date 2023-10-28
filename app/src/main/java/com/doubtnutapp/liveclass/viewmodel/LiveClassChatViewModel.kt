package com.doubtnutapp.liveclass.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.LiveClassChatResponse
import com.doubtnutapp.data.remote.models.ReportUserResponse
import com.doubtnutapp.plus
import com.doubtnutapp.socket.SocketEventType
import com.doubtnutapp.socket.SocketManager
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.Utils
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class LiveClassChatViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val socketManager: SocketManager
) : BaseViewModel(compositeDisposable) {

    private val _liveClassChatData: MutableLiveData<Outcome<LiveClassChatResponse>> =
        MutableLiveData()
    private val _reportMessageData: MutableLiveData<Outcome<ReportUserResponse>> = MutableLiveData()
    private val _banUserData: MutableLiveData<Outcome<LiveClassChatResponse>> = MutableLiveData()
    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())
    private var _imagePathData: String = ""
    private var _imageFileData: String = ""
    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val socketMessage: MediatorLiveData<Event<SocketEventType>> = MediatorLiveData()
    val chatLiveData: LiveData<Outcome<LiveClassChatResponse>>
        get() = _liveClassChatData
    val reportMessageLiveData: LiveData<Outcome<ReportUserResponse>>
        get() = _reportMessageData
    val banUserLiveData: LiveData<Outcome<LiveClassChatResponse>>
        get() = _banUserData
    val imagePathLiveData: MutableLiveData<String> = MutableLiveData(_imagePathData)
    val imageFileNameLiveData: MutableLiveData<String> = MutableLiveData(_imageFileData)

    private val _message: MutableLiveData<Int> = MutableLiveData()
    val message: LiveData<Int>
        get() = _message

    companion object {
        const val PATH = "quiztfs"
    }

    init {
        socketManager(PATH, socketMessage)
    }

    fun getPreviousMessages(roomId: String, page: Int, offset: String?) {
        _liveClassChatData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.chatRepository.getMessages(roomId, PATH, page, offset)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _liveClassChatData.value = Outcome.success(it)
                        _liveClassChatData.value = Outcome.loading(false)
                    }, {
                        _liveClassChatData.value = Outcome.loading(false)
                    })
    }

    fun addImage(imagePath: String) {
        _imagePathData = ""
        _imageFileData = ""
        _imagePathData = imagePath
        imagePathLiveData.postValue(_imagePathData)
    }

    @SuppressLint("CheckResult")
    fun getSignedUrlsForAttachments(imagePath: String) {
        val requests: MutableList<Single<Unit>?> = ArrayList()
        val imageUri = Uri.parse(imagePath)
        val uploadRequest = teslaRepository.getSignedUrl(
            "image/png", imageUri.lastPathSegment
                ?: "", Utils.getFileExtension(imagePath), Utils.getMimeType(imageUri)
        )
            .subscribeOn(Schedulers.io())
            .flatMap { signUrlResponse ->
                if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                    _imageFileData = signUrlResponse.data.fileName
                    val imageFile = File(imagePath)
                    val fileBody = ProgressRequestBody(
                        imageFile,
                        "application/octet",
                        object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                                stateLiveData.value = (ViewState.loading("Posting..."))
                            }
                        })
                    teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                } else {
                    null
                }
            }
        requests.add(uploadRequest)
        compositeDisposable.add(Single.zip(requests.filterNotNull()) {
            it
        }.subscribe({
            imageFileNameLiveData.postValue(_imageFileData)
        }) {
            stateLiveData.postValue(ViewState.error("Some Error Occured"))
        })
    }

    fun reportUser(studentId: String, roomId: String, postId: String) {
        _banUserData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.chatRepository.banUser(hashMapOf<String, String>().apply {
                    put("student_id", studentId)
                    put("room_id", roomId)
                    put("post_id", postId)
                })
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it
                    }
                    .subscribeToSingle({
                        _banUserData.value = Outcome.success(it)
                        _banUserData.value = Outcome.loading(false)
                    }, {
                        _banUserData.value = Outcome.loading(false)
                    })
    }

    fun deleteMessage(studentId: String, postId: String) {
        _reportMessageData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.chatRepository.reportMessage(hashMapOf<String, String>().apply {
                    put("student_id", studentId)
                    put("post_id", postId)
                })
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it
                    }
                    .subscribeToSingle({
                        _reportMessageData.value = Outcome.success(it)
                        _reportMessageData.value = Outcome.loading(false)
                    }, {
                        _reportMessageData.value = Outcome.loading(false)
                    })
    }

    //socket implementation start
    fun connectSocket() {
        socketManager.connect()
    }

    fun joinSocket(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.join(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun sendMessage(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.sendMessage(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun banUser(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.banUser(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun reportUserEvent(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.reportUser(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    private fun disposeSocket() {
        socketManager.disposeSocket()
    }

    fun isSocketConnected(): Boolean {
        return socketManager.isSocketConnected
    }

    override fun onCleared() {
        super.onCleared()
        disposeSocket()
    }
    //socket implementation end
}