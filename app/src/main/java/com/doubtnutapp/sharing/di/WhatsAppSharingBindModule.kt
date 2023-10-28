package com.doubtnutapp.sharing.di

import com.doubtnut.core.sharing.IWhatsAppSharing
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.whatsappsharing.repository.WhatsAppShareRepositoryImpl
import com.doubtnutapp.data.whatsappsharing.service.WhatsAppSharingService
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import com.doubtnutapp.sharing.WhatsAppSharing
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
abstract class WhatsAppSharingBindModule {

    @Binds
    abstract fun bindWhatsAppRepository(whatsAppShareRepositoryImpl: WhatsAppShareRepositoryImpl): WhatsAppShareRepository

    @Binds
    abstract fun bindWhatsAppSharing(whatsAppSharing: WhatsAppSharing): IWhatsAppSharing

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providerWhatsAppSharingService(@ApiRetrofit retrofit: Retrofit): WhatsAppSharingService {
            return retrofit.create(WhatsAppSharingService::class.java)
        }
    }
}