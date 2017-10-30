package kr.ac.snu.hcil.omnitrack.services

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import kr.ac.snu.hcil.omnitrack.BuildConfig
import kr.ac.snu.hcil.omnitrack.OTApp
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.database.RemoteConfigManager
import kr.ac.snu.hcil.omnitrack.core.system.OTNotificationManager
import kr.ac.snu.hcil.omnitrack.utils.VectorIconHelper
import org.jetbrains.anko.notificationManager

/**
 * Created by younghokim on 2017. 4. 15..
 */
class OTVersionCheckService : Service() {

    companion object {
        const val TAG = "VersionCheckService"

        const val REQUEST_CODE = 20

        const val PREF_LAST_NOTIFIED_VERSION = "last_notified_version"

        private val checkSubscription = SerialDisposable()

        fun setupServiceAlarm(context: Context) {
            val serviceIntent = Intent(context, OTVersionCheckService::class.java)
            val pendingIntent = PendingIntent.getService(context, REQUEST_CODE, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_check_updates", false)) {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 1000, 7200 * 1000, pendingIntent)
            } else {
                alarmManager.cancel(pendingIntent)
                context.stopService(serviceIntent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        synchronized(checkSubscription)
        {
            Log.d(TAG, "check latest version of OmniTrack...: Service")
            RemoteConfigManager.getServerLatestVersionName().subscribe({
                versionName ->
                Log.d(TAG, "received version name.: ${versionName}")
                if (BuildConfig.DEBUG || RemoteConfigManager.isNewVersionGreater(BuildConfig.VERSION_NAME, versionName)) {
                    val lastNotifiedVersion = OTApp.instance.systemSharedPreferences.getString(PREF_LAST_NOTIFIED_VERSION, "")
                    if (lastNotifiedVersion != versionName) {

                        if (!OTApp.instance.isAppInForeground) {
                            Log.d(TAG, "instance is in background. send notification.")
                            val notification = NotificationCompat.Builder(this, OTNotificationManager.CHANNEL_ID_NOTICE)
                                    .setColor(ContextCompat.getColor(this, R.color.colorPointed))
                                    .setContentTitle(OTApp.getString(R.string.msg_notification_new_version_detected_title))
                                    .setContentText(OTApp.getString(R.string.msg_notification_new_version_detected_text))
                                    .setLargeIcon(VectorIconHelper.getConvertedBitmap(this, R.drawable.icon_simple))
                                    .setSmallIcon(R.drawable.icon_simple)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setAutoCancel(true)
                                    .setContentIntent(
                                            PendingIntent.getActivity(this, REQUEST_CODE,
                                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kr.ac.snu.hcil.omnitrack")),
                                                    PendingIntent.FLAG_UPDATE_CURRENT)
                                    )
                                    .build()
                            notificationManager.notify(TAG, REQUEST_CODE, notification)

                            OTApp.instance.systemSharedPreferences.edit()
                                    .putString(PREF_LAST_NOTIFIED_VERSION, versionName)
                                    .apply()
                        } else {

                            Log.d(TAG, "instance is in foreground. send broadcast.")
                            this.notificationManager.cancel(TAG, REQUEST_CODE)
                            val intent = Intent(OTApp.BROADCAST_ACTION_NEW_VERSION_DETECTED)
                            intent.putExtra(OTApp.INTENT_EXTRA_LATEST_VERSION_NAME, versionName)

                            sendBroadcast(intent)
                        }
                    } else {
                        Log.d(TAG, "this version was already notified. ignore notification.")
                    }
                }
            }, { e -> e.printStackTrace() })
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSubscription.set(Disposables.empty())
    }
}