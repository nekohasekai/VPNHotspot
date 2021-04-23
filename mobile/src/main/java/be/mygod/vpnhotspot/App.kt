package be.mygod.vpnhotspot

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.Size
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.preference.PreferenceManager
import be.mygod.librootkotlinx.NoShellException
import be.mygod.vpnhotspot.net.DhcpWorkaround
import be.mygod.vpnhotspot.room.AppDatabase
import be.mygod.vpnhotspot.root.RootManager
import be.mygod.vpnhotspot.util.DeviceStorageApp
import be.mygod.vpnhotspot.util.Services
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var app: App
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        if (Build.VERSION.SDK_INT >= 24) @SuppressLint("RestrictedApi") {
            deviceStorage = DeviceStorageApp(this)
            // alternative to PreferenceManager.getDefaultSharedPreferencesName(this)
            deviceStorage.moveSharedPreferencesFrom(this, PreferenceManager(this).sharedPreferencesName)
            deviceStorage.moveDatabaseFrom(this, AppDatabase.DB_NAME)
        } else deviceStorage = this
        Services.init { this }

        // overhead of debug mode is minimal: https://github.com/Kotlin/kotlinx.coroutines/blob/f528898/docs/debugging.md#debug-mode
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        ServiceNotification.updateNotificationChannels()
        EmojiCompat.init(FontRequestEmojiCompatConfig(deviceStorage, FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)).apply {
            setEmojiSpanIndicatorEnabled(BuildConfig.DEBUG)
            registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() = Timber.d("EmojiCompat initialized")
                override fun onFailed(throwable: Throwable?) = Timber.d(throwable)
            })
        })
        if (DhcpWorkaround.shouldEnable) DhcpWorkaround.enable(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ServiceNotification.updateNotificationChannels()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_RUNNING_CRITICAL || level >= TRIM_MEMORY_BACKGROUND) GlobalScope.launch {
            RootManager.closeExisting()
        }
    }

    lateinit var deviceStorage: Application
    val english by lazy {
        createConfigurationContext(Configuration(resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        })
    }
    val pref by lazy { PreferenceManager.getDefaultSharedPreferences(deviceStorage) }
    val clipboard by lazy { getSystemService<ClipboardManager>()!! }

    val hasTouch by lazy { packageManager.hasSystemFeature("android.hardware.faketouch") }
    val customTabsIntent by lazy {
        CustomTabsIntent.Builder().apply {
            setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(ContextCompat.getColor(app, R.color.light_colorPrimary))
            }.build())
            setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(ContextCompat.getColor(app, R.color.dark_colorPrimary))
            }.build())
        }.build()
    }
}
