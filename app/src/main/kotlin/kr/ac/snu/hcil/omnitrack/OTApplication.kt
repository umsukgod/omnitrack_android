package kr.ac.snu.hcil.omnitrack

import android.app.Application
import android.graphics.Color
import kr.ac.snu.hcil.omnitrack.core.OTShortcutManager
import kr.ac.snu.hcil.omnitrack.core.OTUser
import kr.ac.snu.hcil.omnitrack.core.attributes.*
import kr.ac.snu.hcil.omnitrack.core.database.CacheHelper
import kr.ac.snu.hcil.omnitrack.core.database.DatabaseHelper
import kr.ac.snu.hcil.omnitrack.core.externals.OTExternalService
import kr.ac.snu.hcil.omnitrack.core.triggers.OTTimeTriggerAlarmManager
import kr.ac.snu.hcil.omnitrack.core.triggers.OTTriggerManager
import kr.ac.snu.hcil.omnitrack.utils.UniqueStringEntryList

/**
 * Created by Young-Ho Kim on 2016-07-11.
 */

class OTApplication : Application() {

    companion object {
        lateinit var app: OTApplication
            private set

        const val INTENT_EXTRA_OBJECT_ID_TRACKER = "trackerObjectId"
        const val INTENT_EXTRA_OBJECT_ID_ATTRIBUTE = "attributeObjectId"
        const val INTENT_EXTRA_OBJECT_ID_USER = "userObjectId"
        const val INTENT_EXTRA_OBJECT_ID_TRIGGER = "triggerObjectId"
        const val INTENT_EXTRA_DB_ID_ITEM = "itemDbId"


        const val INTENT_EXTRA_ITEMBUILDER = "itemBuilderId"

        const val BROADCAST_ACTION_ALARM = "kr.ac.snu.hcil.omnitrack.action.ALARM"

        /*
        const val BROADCAST_ACTION_SHORTCUT_PUSH_NOW = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_PUSH_NOW"
        const val BROADCAST_ACTION_SHORTCUT_OPEN_TRACKER = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_OPEN_TRACKER"
        const val BROADCAST_ACTION_SHORTCUT_INCLUDE_TRACKER = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_INCLUDE_TRACKER"
        const val BROADCAST_ACTION_SHORTCUT_EXCLUDE_TRACKER = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_EXCLUDE_TRACKER"
        const val BROADCAST_ACTION_SHORTCUT_TRACKER_INFO_CHANGED = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_TRACKER_INFO_CHANGED"
*/
        const val BROADCAST_ACTION_SHORTCUT_REFRESH = "kr.ac.snu.hcil.omnitrack.action.SHORTCUT_TRACKER_REFRESH"

        const val BROADCAST_ACTION_ITEM_ADDED = "kr.ac.snu.hcil.omnitrack.action.ITEM_ADDED"
        const val BROADCAST_ACTION_ITEM_REMOVED = "kr.ac.snu.hcil.omnitrack.action.ITEM_REMOVED"
        const val BROADCAST_ACTION_ITEM_EDITED = "kr.ac.snu.hcil.omnitrack.action.ITEM_EDITED"

        const val PREFERENCE_KEY_FOREGROUND_ITEM_BUILDER_STORAGE = "item_builder_storage_foreground"

        const val PREFERENCE_KEY_BACKGROUND_ITEM_BUILDER_STORAGE = "item_builder_storage_background"
    }

    private lateinit var _currentUser: OTUser

    lateinit var dbHelper: DatabaseHelper
        private set

    val cacheHelper: CacheHelper by lazy {
        CacheHelper(this)
    }

    val currentUser: OTUser
        get() {
            return _currentUser
        }

    val colorPalette: IntArray by lazy {
        this.resources.getStringArray(R.array.colorPaletteArray).map { Color.parseColor(it) }.toIntArray()
    }

    val googleApiKey: String by lazy {
        this.resources.getString(R.string.google_maps_key)
    }

    lateinit var triggerManager: OTTriggerManager
        private set


    lateinit var supportedAttributePresets: Array<AttributePresetInfo>
        private set

    override fun onCreate() {
        super.onCreate()

        app = this

        dbHelper = DatabaseHelper(this)

        val user = dbHelper.findUserById(1)
        if (user == null) {
            val defaultUser = OTUser("Young-Ho Kim", "yhkim@hcil.snu.ac.kr")
            _currentUser = defaultUser
            val coffeeTracker = defaultUser.newTracker("Coffee", true)
            coffeeTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Name", OTAttribute.TYPE_SHORT_TEXT))
            coffeeTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Drank At", OTAttribute.TYPE_TIME))

            val waterTracker = defaultUser.newTracker("Water", true)
            waterTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Drank At", OTAttribute.TYPE_TIME))

            val sleepTracker = defaultUser.newTracker("Manual Sleep", true)

            val stepAttribute = OTAttribute.Companion.createAttribute(defaultUser, "Steps of the day", OTAttribute.TYPE_NUMBER)

            sleepTracker.attributes.add(stepAttribute)

            sleepTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "When to Bed", OTAttribute.TYPE_TIME))

            val sleepTimeAttribute = OTAttribute.Companion.createAttribute(defaultUser, "Slept for", OTAttribute.TYPE_TIMESPAN)
            sleepTimeAttribute.setPropertyValue(OTTimeSpanAttribute.PROPERTY_GRANULARITY, OTTimeAttribute.GRANULARITY_MINUTE)

            sleepTracker.attributes.add(sleepTimeAttribute)
            sleepTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Memo", OTAttribute.TYPE_LONG_TEXT))

            val diaryTracker = defaultUser.newTracker("Diary", true)
            val dateAttribute = OTAttribute.createAttribute(defaultUser, "Date", OTAttribute.TYPE_TIME)
            dateAttribute.setPropertyValue(OTTimeAttribute.GRANULARITY, OTTimeAttribute.GRANULARITY_DAY)
            diaryTracker.attributes.add(dateAttribute)

            val moodAttribute = OTAttribute.createAttribute(defaultUser, "Mood", OTAttribute.TYPE_CHOICE)
            moodAttribute.setPropertyValue(OTChoiceAttribute.PROPERTY_ENTRIES, UniqueStringEntryList("Wonderful", "Sad", "Good", "Insomnia", "Depressed", "Angry", "Fatigued", "Happy"))
            moodAttribute.setPropertyValue(OTChoiceAttribute.PROPERTY_MULTISELECTION, true)
            diaryTracker.attributes.add(moodAttribute)

            diaryTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Title", OTAttribute.TYPE_SHORT_TEXT))
            diaryTracker.attributes.add(OTAttribute.Companion.createAttribute(defaultUser, "Content", OTAttribute.TYPE_LONG_TEXT))



            _currentUser = defaultUser
        } else {
            _currentUser = user
        }


        triggerManager = OTTriggerManager(_currentUser, if (_currentUser.dbId != null) {
            dbHelper.findTriggersOfUser(_currentUser.dbId!!)
        } else {
            null
        })


        for (service in OTExternalService.availableServices) {
            if (service.state == OTExternalService.ServiceState.ACTIVATED) {
                service.prepareServiceAsync({
                    result ->

                })
            }
        }

        for (tracker in currentUser.getTrackersOnShortcut())
        {
            OTShortcutManager+= tracker
        }


        supportedAttributePresets = arrayOf(
                SimpleAttributePresetInfo(OTAttribute.TYPE_SHORT_TEXT, R.drawable.field_icon_shorttext, this.getString(R.string.type_shorttext_name), this.getString(R.string.type_shorttext_desc)),
                SimpleAttributePresetInfo(OTAttribute.TYPE_LONG_TEXT, R.drawable.field_icon_longtext, this.getString(R.string.type_longtext_name), this.getString(R.string.type_longtext_desc)),
                SimpleAttributePresetInfo(OTAttribute.TYPE_NUMBER, R.drawable.field_icon_number, this.getString(R.string.type_number_name), this.getString(R.string.type_number_desc)),
                SimpleAttributePresetInfo(OTAttribute.TYPE_TIME, R.drawable.field_icon_time, this.getString(R.string.type_timepoint_name), this.getString(R.string.type_timepoint_desc)),
                SimpleAttributePresetInfo(OTAttribute.TYPE_LOCATION, R.drawable.field_icon_location, this.getString(R.string.type_location_name), this.getString(R.string.type_location_desc)),
                SimpleAttributePresetInfo(OTAttribute.TYPE_TIMESPAN, R.drawable.field_icon_timer, this.getString(R.string.type_timespan_name), this.getString(R.string.type_timespan_desc)),
                AttributePresetInfo(R.drawable.field_icon_singlechoice, this.getString(R.string.type_single_choice_name), this.getString(R.string.type_single_choice_desc),
                        { user, columnName ->
                            val attr = OTAttribute.createAttribute(user, columnName, OTAttribute.TYPE_CHOICE) as OTChoiceAttribute
                            attr.allowedMultiselection = false
                            attr
                        }),

                AttributePresetInfo(R.drawable.field_icon_multiplechoice, this.getString(R.string.type_multiple_choices_name), this.getString(R.string.type_multiple_choices_desc),
                        { user, columnName ->
                            val attr = OTAttribute.createAttribute(user, columnName, OTAttribute.TYPE_CHOICE) as OTChoiceAttribute
                            attr.allowedMultiselection = true
                            attr
                        })

        )


    }

    fun syncUserToDb() {
        dbHelper.save(_currentUser)
        for (triggerEntry in triggerManager.withIndex()) {
            dbHelper.save(triggerEntry.value, _currentUser, triggerEntry.index)
        }
        dbHelper.deleteObjects(DatabaseHelper.TriggerScheme, *triggerManager.fetchRemovedTriggerIds())

        OTTimeTriggerAlarmManager.storeTableToPreferences()
    }

    override fun onTerminate() {
        super.onTerminate()

        syncUserToDb()

        dbHelper.close()
    }
}