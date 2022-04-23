package no.exam.android

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.internal.GeneratedComponent
import no.exam.android.components.ApplicationComponent
import no.exam.android.repo.ImageRepo
import no.exam.android.repo.ImageRepoImpl
import javax.inject.Singleton

@HiltAndroidApp
class Application : Application()



