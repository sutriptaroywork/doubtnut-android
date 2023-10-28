package com.doubtnutapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


object RxEditTextObservable {

    fun fromView(editText: EditText): Observable<String> {
        val subject = PublishSubject.create<String>()
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                editText.error = null
                subject.onNext(p0.toString())
            }

        })
        return subject
    }
}