package com.doubtnut.referral.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.DateTimeUtils.getCurrentTimeInMillis
import com.doubtnut.referral.data.remote.ReferralRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShareYourReferralCodeBottomSheetDialogFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val referralRepository: ReferralRepository
) : CoreViewModel(compositeDisposable) {

    companion object {
        const val TAG = "ShareYourReferralCodeBSDFVM"
    }

    private val _contacts = MutableLiveData<List<ContactData>?>(null)
    val contacts: LiveData<List<ContactData>?>
        get() = _contacts

    private val _progress = MutableLiveData(false)
    val progress: LiveData<Boolean>
        get() = _progress



    fun uploadAndFetchLatestMobileNumbers(shareContactBatchSize: Int = 1500) {

        _progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = ContactUtils.getMobileNumbers().values
            shareAndThenCollectContacts(
                contacts.take(shareContactBatchSize)
            )
        }
    }

    private fun shareAndThenCollectContacts(contacts: List<ContactData>) {

        viewModelScope.launch {
            referralRepository.storeContact(
                ContactDataRequest(manipulateArray(contacts))
            ).flatMapConcat {
                referralRepository.getContacts()
            }
                .catch {
                    it.printStackTrace()
                    _progress.value = false
                }
                .collect {
                    _contacts.value = ArrayList<ContactData>().apply {
                        addAll(it)
                    }
                    defaultPreferences().edit().apply {
                        putLong(
                            ContactUtils.LAST_UPDATED_CONTACTS_TIMESTAMP,
                            getCurrentTimeInMillis()
                        )
                    }.apply()
                    _progress.value = false
                }
        }

    }

    private fun manipulateArray(contacts: List<ContactData>): List<ContactDataManipulated> {
        val list = mutableListOf<ContactDataManipulated>()
        contacts.forEach { contact ->
            contact.mobileNumbers?.forEach { it ->
                list.add(
                    ContactDataManipulated(
                        id = contact.id,
                        mobileNumbers = it?.replace(" ", ""),
                        customer = contact.customer,
                        name = contact.name,
                        type = null,
                        title = null
                    )
                )
            }
        }
        return list
    }


    fun referralShareInfo(
        refereeName: String,
        refereePhone: String,
    ) {
        viewModelScope.launch {
            referralRepository.referralShareInfo(
                refereeName = refereeName,
                refereePhone = refereePhone
            )
                .catch {
                    it.printStackTrace()
                }
                .collect {}
        }
    }

}