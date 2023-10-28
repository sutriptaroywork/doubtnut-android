package com.doubtnutapp.ui.course

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.doAsyncPost
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

class SelectImageViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable)
    val verticalImage = MutableLiveData<Event<Uri>>()

    fun processImage(data: ByteArray) {

        val destinationFileName =  "CameraTempImage" + ".jpg"

        doAsyncPost(handler = {
            val file = File(DoubtnutApp.INSTANCE.cacheDir, destinationFileName)
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file)
                os.write(data)
                os.close()
            } catch (e: IOException) {
                Log.w("MainViewModel", "Cannot write to $file", e)
            } finally {
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        // Ignore
                    }

                }
            }
        }, postHandler = {
            verticalImage.value = Event(Uri.fromFile(File(DoubtnutApp.INSTANCE.cacheDir, destinationFileName)))
//            return Uri.fromFile(File(getApplication<DoubtnutApp>().cacheDir, destinationFileName))
        }).execute()

    }