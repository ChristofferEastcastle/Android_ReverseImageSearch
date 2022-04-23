package no.exam.android.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import no.exam.android.repo.ImageRepo
import no.exam.android.repo.ImageRepoImpl
import no.exam.android.service.ImageService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyModule {

    @Provides
    @Singleton
    fun provideService(@ApplicationContext context: Context): ImageService {
        return ImageService(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ImageRepo =
        ImageRepoImpl(context)
}