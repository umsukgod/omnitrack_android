package kr.ac.snu.hcil.omnitrack.core

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.TaskStackBuilder
import kr.ac.snu.hcil.omnitrack.OTApplication
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.ui.pages.items.ItemEditingActivity
import kr.ac.snu.hcil.omnitrack.utils.FillingIntegerIdReservationTable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by younghokim on 16. 9. 1..
 */
object OTNotificationManager {

    private val increment = AtomicInteger(100)

    private val reminderTrackerPendingCounts = Hashtable<String, Int>()

    private val reminderTrackerIdTable = FillingIntegerIdReservationTable<String>()

    enum class Type(val priority: Int, val collapse: Boolean) {
        TRACKING_REMINDER(Notification.PRIORITY_MAX, false),
        BACKGROUND_LOGGING_NOTIFICATION(Notification.PRIORITY_DEFAULT, true),
        BACKGROUND_LOGGING_PROGRESS(Notification.PRIORITY_DEFAULT, false);


        fun getNewId(): Int {
            return if (collapse) {
                Type.values().indexOf(this)
            } else {
                increment.incrementAndGet()
            }
        }
    }

    private fun getNotiService(): NotificationManager {
        return OTApplication.app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun pushReminderNotification(context: Context, tracker: OTTracker, reminderTime: Long) {
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ItemEditingActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(ItemEditingActivity.makeIntent(tracker.objectId, reminderTime, context))
        val resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = makeBaseBuilder(context, Type.TRACKING_REMINDER, reminderTime)
                .setContentIntent(resultPendingIntent)
                .setContentText(String.format(context.resources.getString(R.string.msg_noti_tap_for_tracking_format), tracker.name))

        if (reminderTrackerPendingCounts.containsKey(tracker.objectId)) {
            println("merge reminder - ${tracker.name}")
            //not first. merge notification
            reminderTrackerPendingCounts[tracker.objectId] = reminderTrackerPendingCounts[tracker.objectId]!! + 1

            builder.setAutoCancel(false)
                    .setContentTitle("${reminderTrackerPendingCounts[tracker.objectId]} ${tracker.name} Reminders")
        } else {
            println("show new reminder - ${tracker.name}")
            //first time. this is the only notification with that tracker.
            reminderTrackerPendingCounts[tracker.objectId] = 1

            builder.setAutoCancel(true)
                    .setContentTitle("${tracker.name} Reminder")
        }


        getNotiService().notify(reminderTrackerIdTable[tracker.objectId], builder.build())
    }

    fun pushBackgroundLoggingNotification(context: Context, tracker: OTTracker, loggedTime: Long) {

    }

    fun notifyReminderChecked(trackerId: String, reminderTime: Long) {
        //if(reminderTrackerPendingCounts[trackerId] != null)
        //{
        //  if(reminderTrackerPendingCounts[trackerId]!! == 1)
        //  {
        //remove reminder
        reminderTrackerPendingCounts.remove(trackerId)
        getNotiService().cancel(reminderTrackerIdTable[trackerId])
        reminderTrackerIdTable.removeKey(trackerId)
        //  }
        //  else{
        //      reminderTrackerPendingCounts[trackerId] = reminderTrackerPendingCounts[trackerId]!! - 1
        //  }

        // }
    }

    fun makeBaseBuilder(context: Context, type: Type, time: Long): Notification.Builder {

        val builder = Notification.Builder(context).setPriority(type.priority)
                .setWhen(time)
                .setShowWhen(true)
                .setSmallIcon(R.drawable.icon_simple)

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(context.resources.getColor(R.color.colorPrimary, null))
        }


        return builder
    }

}