package com.doubtnutapp.domain.mainscrren.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.mainscrren.repository.MainScreenRepository
import io.reactivex.Completable
import javax.inject.Inject

class SetBottomNavigationButtonClickedStateToTrue @Inject constructor(private val mainScreenRepository: MainScreenRepository) : CompletableUseCase<String> {
    override fun execute(param: String): Completable = mainScreenRepository.setBottomNavigationButtonStateToTrue(param)
}
