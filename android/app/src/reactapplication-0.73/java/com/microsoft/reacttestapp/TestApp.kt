package com.microsoft.reacttestapp

import android.app.Activity
import android.app.Application
import android.content.Context
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.soloader.SoLoader
import com.microsoft.reacttestapp.manifest.Manifest
import com.microsoft.reacttestapp.manifest.ManifestProvider
import com.microsoft.reacttestapp.react.ReactBundleNameProvider
import com.microsoft.reacttestapp.react.TestAppReactNativeHost
import com.microsoft.reacttestapp.support.ReactTestAppLifecycleEvents

class TestApp :
    Application(),
    ReactApplication {

    val bundleNameProvider: ReactBundleNameProvider
        get() = reactNativeBundleNameProvider

    val manifest: Manifest by lazy {
        ManifestProvider.manifest()
    }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(this.applicationContext, reactNativeHost)

    override val reactNativeHost: TestAppReactNativeHost
        get() = reactNativeHostInternal

    private lateinit var reactNativeBundleNameProvider: ReactBundleNameProvider
    private lateinit var reactNativeHostInternal: TestAppReactNativeHost

    fun reloadJSFromServer(activity: Activity, bundleURL: String) {
        reactNativeHostInternal.reloadJSFromServer(activity, bundleURL)
    }

    override fun onCreate() {
        super.onCreate()

        SoLoader.init(this, false)

        reactNativeBundleNameProvider = ReactBundleNameProvider(this, manifest.bundleRoot)
        reactNativeHostInternal =
            TestAppReactNativeHost(this, reactNativeBundleNameProvider)

        val eventConsumers = PackageList(this).packages
            .filter { it is ReactTestAppLifecycleEvents }
            .map { it as ReactTestAppLifecycleEvents }

        eventConsumers.forEach { it.onTestAppInitialized() }

        reactNativeHostInternal.init(
            beforeReactNativeInit = {
                eventConsumers.forEach { it.onTestAppWillInitializeReactNative() }
            },
            afterReactNativeInit = {
                eventConsumers.forEach { it.onTestAppDidInitializeReactNative() }
            }
        )
    }
}

val Context.testApp: TestApp
    get() = applicationContext as TestApp
