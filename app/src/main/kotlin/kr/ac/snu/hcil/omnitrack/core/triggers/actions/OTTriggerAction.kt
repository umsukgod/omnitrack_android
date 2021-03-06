package kr.ac.snu.hcil.omnitrack.core.triggers.actions

import android.content.Context
import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonObject
import io.reactivex.Completable
import kr.ac.snu.hcil.omnitrack.core.database.models.OTTriggerDAO

/**
 * Created by younghokim on 2017. 4. 17..
 */
abstract class OTTriggerAction {

    abstract fun performAction(trigger: OTTriggerDAO, triggerTime: Long, metadata: JsonObject, context: Context): Completable

    open fun writeEventLogContent(trigger: OTTriggerDAO, table: JsonObject) {
        table.add("trackers", jsonArray(*trigger.liveTrackerIds))
    }

    abstract fun getSerializedString(): String

    abstract fun clone(): OTTriggerAction
}