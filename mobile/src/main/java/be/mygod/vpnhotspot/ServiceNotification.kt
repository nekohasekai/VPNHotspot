package be.mygod.vpnhotspot

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import be.mygod.vpnhotspot.App.Companion.app
import java.util.*

object ServiceNotification {
    private const val CHANNEL_ACTIVE = "tethering"
    private const val CHANNEL_INACTIVE = "tethering-inactive"
    private const val NOTIFICATION_ID = 1

    private val deviceCountsMap = WeakHashMap<Service, Map<String, Int>>()
    private val inactiveMap = WeakHashMap<Service, List<String>>()
    private val manager = app.getSystemService<NotificationManager>()!!

    private fun buildNotification(context: Context): Notification {
        val deviceCounts = deviceCountsMap.values.flatMap { it.entries }.sortedBy { it.key }
        val inactive = inactiveMap.values.flatten()
        val isInactive = inactive.isNotEmpty() && deviceCounts.isEmpty()
        val builder = NotificationCompat.Builder(context, if (isInactive) CHANNEL_INACTIVE else CHANNEL_ACTIVE).apply {
            setWhen(0)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            setContentTitle(context.getText(R.string.notification_tethering_title))
            setSmallIcon(R.drawable.ic_quick_settings_tile_on)
            setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            priority = if (isInactive) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW
        }
        var lines = deviceCounts.map { (dev, size) ->
            context.resources.getQuantityString(R.plurals.notification_connected_devices, size, size, dev)
        }
        if (inactive.isNotEmpty()) {
            lines += context.getString(R.string.notification_interfaces_inactive, inactive.joinToString())
        }
        return if (lines.size <= 1) builder.setContentText(lines.singleOrNull()).build() else {
            val deviceCount = deviceCounts.sumOf { it.value }
            val interfaceCount = deviceCounts.size + inactive.size
            NotificationCompat.BigTextStyle(builder
                    .setContentText(context.resources.getQuantityString(R.plurals.notification_connected_devices,
                            deviceCount, deviceCount,
                            context.resources.getQuantityString(R.plurals.notification_interfaces,
                                    interfaceCount, interfaceCount))))
                    .bigText(lines.joinToString("\n"))
                    .build()!!
        }
    }

    fun startForeground(service: Service, deviceCounts: Map<String, Int>, inactive: List<String> = emptyList()) {
        synchronized(this) {
            deviceCountsMap[service] = deviceCounts
            if (inactive.isEmpty()) inactiveMap.remove(service) else inactiveMap[service] = inactive
            service.startForeground(NOTIFICATION_ID, buildNotification(service))
        }
    }
    fun stopForeground(service: Service) = synchronized(this) {
        deviceCountsMap.remove(service) ?: return@synchronized
        val shutdown = deviceCountsMap.isEmpty()
        service.stopForeground(shutdown)
        if (!shutdown) manager.notify(NOTIFICATION_ID, buildNotification(service))
    }

    fun updateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) @TargetApi(26) {
            NotificationChannel(CHANNEL_ACTIVE,
                    app.getText(R.string.notification_channel_tethering), NotificationManager.IMPORTANCE_LOW).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                manager.createNotificationChannel(this)
            }
            NotificationChannel(CHANNEL_INACTIVE,
                    app.getText(R.string.notification_channel_monitor), NotificationManager.IMPORTANCE_LOW).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                manager.createNotificationChannel(this)
            }
            // remove old service channels
            manager.deleteNotificationChannel("hotspot")
            manager.deleteNotificationChannel("repeater")
        }
    }
}
