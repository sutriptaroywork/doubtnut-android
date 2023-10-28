package com.doubtnutapp.addtoplaylist

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity
import com.doubtnutapp.domain.addtoplaylist.interactor.GetUserPlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.resourcelisting.interactor.CreateNewPlaylistUseCase
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class AddToPlaylistViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getUserPlaylistUseCase: GetUserPlaylistUseCase,
    private val createNewPlaylistUseCase: CreateNewPlaylistUseCase,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase
) : BaseViewModel(compositeDisposable) {

    private val _userPlaylistLiveData: MutableLiveData<Event<ArrayList<PlaylistEntity>>> = MutableLiveData()
    val userPlaylistLiveData: LiveData<Event<ArrayList<PlaylistEntity>>>
        get() = _userPlaylistLiveData

    fun fetchUserPlaylist() {
        compositeDisposable.add(
            getUserPlaylistUseCase
                .execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onListSuccess, this::onError)
        )
    }

    private fun onListSuccess(playlist: ArrayList<PlaylistEntity>) {
        playlist.add(0, PlaylistEntity("1", "Watch Later", true))
        _userPlaylistLiveData.value = Event(playlist)
    }

    fun onError(throwable: Throwable) {
        logException(throwable)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException ||
                error is NullPointerException ||
                error is ClassCastException ||
                error is FormatException ||
                error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {
        }
    }

    private val _createAndAddToPlayList: MutableLiveData<Event<Boolean>> = MutableLiveData()

    val createAndAddToPlayList: LiveData<Event<Boolean>>
        get() = _createAndAddToPlayList

    fun createNewPlaylistAndAdd(id: String, playListName: String) {
        compositeDisposable + createNewPlaylistUseCase
            .execute(Pair(id, playListName))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _createAndAddToPlayList.postValue(Event(true))
            }, this::onError)
    }

    fun submitPlayLists(id: String, playListIds: List<String>) {
        compositeDisposable + submitPlayListsUseCase
            .execute(Pair(id, playListIds))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _createAndAddToPlayList.postValue(Event(true))
            }, this::onError)
    }
}
