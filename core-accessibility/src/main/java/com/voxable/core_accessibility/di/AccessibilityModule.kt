package com.voxable.core_accessibility.di

import android.content.Context
import com.voxable.core_accessibility.AccessibilityStateManager
import com.voxable.core_accessibility.TalkBackUtils
import com.voxable.core_accessibility.VoiceFeedbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccessibilityModule {

    @Provides
    @Singleton
    fun provideAccessibilityStateManager(
        @ApplicationContext context: Context
    ): AccessibilityStateManager {
        return AccessibilityStateManager(context)
    }

    @Provides
    @Singleton
    fun provideTalkBackUtils(
        @ApplicationContext context: Context
    ): TalkBackUtils {
        return TalkBackUtils(context)
    }

    @Provides
    @Singleton
    fun provideVoiceFeedbackManager(
        @ApplicationContext context: Context,
        stateManager: AccessibilityStateManager
    ): VoiceFeedbackManager {
        return VoiceFeedbackManager(context, stateManager)
    }
}
