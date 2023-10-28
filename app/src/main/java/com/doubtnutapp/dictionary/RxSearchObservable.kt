package com.doubtnutapp.dictionary

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object RxSearchObservable {
    fun fromView(searchView: TextInputEditText): Observable<String> {
        val subject = PublishSubject.create<String>()

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                subject.onNext(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        return subject
    }
}
