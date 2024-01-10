package com.toddler.recordit.appmodule

import android.content.Context
import com.toddler.recordit.MyApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(application: MyApplication): Context = application.applicationContext
}


 /**
  *
  * @Module: Tells Hilt it's a module providing dependencies.
  * @InstallIn(SingletonComponent::class): Means dependencies are available throughout the app's lifecycle.
  * @Provides: Marks a method for providing a dependency.
  * @Singleton: Ensures a single instance of the dependency is created.
  *
  *
  * The provideApplicationContext() function is used by Hilt internally, not directly in the ViewModel code.
  *
  **/