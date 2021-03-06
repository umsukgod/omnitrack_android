package kr.ac.snu.hcil.omnitrack.core.attributes.helpers

import android.content.Context
import io.realm.Realm
import kr.ac.snu.hcil.android.common.containers.UniqueStringEntryList
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.attributes.NumericCharacteristics
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.AFieldValueSorter
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.ChoiceSorter
import kr.ac.snu.hcil.omnitrack.core.attributes.properties.OTChoiceEntryListPropertyHelper
import kr.ac.snu.hcil.omnitrack.core.attributes.properties.OTPropertyHelper
import kr.ac.snu.hcil.omnitrack.core.attributes.properties.OTPropertyManager
import kr.ac.snu.hcil.omnitrack.core.database.models.OTAttributeDAO
import kr.ac.snu.hcil.omnitrack.core.serialization.TypeStringSerializationHelper
import kr.ac.snu.hcil.omnitrack.core.visualization.ChartModel
import kr.ac.snu.hcil.omnitrack.core.visualization.models.ChoiceCategoricalBarChartModel
import java.util.*

/**
 * Created by Young-Ho on 10/7/2017.
 */
class OTChoiceAttributeHelper(context: Context) : OTAttributeHelper(context) {

    companion object {
        const val PROPERTY_MULTISELECTION = "multiSelection"
        const val PROPERTY_ENTRIES = "entries"
        const val PROPERTY_ALLOW_APPENDING_FROM_VIEW = "allowAppendingFromView"
    }

    override fun getValueNumericCharacteristics(attribute: OTAttributeDAO): NumericCharacteristics {
        return if (getIsMultiSelectionAllowed(attribute) == true)
            NumericCharacteristics(false, false)
        else NumericCharacteristics(true, false)
    }

    override fun getTypeNameResourceId(attribute: OTAttributeDAO): Int = R.string.type_choice_name

    override fun getTypeSmallIconResourceId(attribute: OTAttributeDAO): Int {
        return if (getIsMultiSelectionAllowed(attribute) == true) {
            R.drawable.icon_small_multiple_choice
        } else R.drawable.icon_small_single_choice
    }

    override val typeNameForSerialization: String = TypeStringSerializationHelper.TYPENAME_INT_ARRAY

    override fun getSupportedSorters(attribute: OTAttributeDAO): Array<AFieldValueSorter> {
        return arrayOf(
                ChoiceSorter(attribute.name, getChoiceEntries(attribute)
                        ?: UniqueStringEntryList(), attribute.localId)
        )
    }

    override val propertyKeys: Array<String> = arrayOf(
            PROPERTY_MULTISELECTION, PROPERTY_ALLOW_APPENDING_FROM_VIEW, PROPERTY_ENTRIES)

    override fun <T> getPropertyHelper(propertyKey: String): OTPropertyHelper<T> {
        return when (propertyKey) {
            PROPERTY_MULTISELECTION -> propertyManager.getHelper(OTPropertyManager.EPropertyType.Boolean)
            PROPERTY_ALLOW_APPENDING_FROM_VIEW -> propertyManager.getHelper(OTPropertyManager.EPropertyType.Boolean)
            PROPERTY_ENTRIES -> propertyManager.getHelper(OTPropertyManager.EPropertyType.ChoiceEntryList)
            else -> throw IllegalArgumentException("Unsupported property key $propertyKey")
        } as OTPropertyHelper<T>
    }

    override fun getPropertyInitialValue(propertyKey: String): Any? {
        return when (propertyKey) {
            PROPERTY_MULTISELECTION -> false
            PROPERTY_ALLOW_APPENDING_FROM_VIEW -> false
            PROPERTY_ENTRIES -> UniqueStringEntryList((propertyManager.getHelper(OTPropertyManager.EPropertyType.ChoiceEntryList) as OTChoiceEntryListPropertyHelper).previewChoiceEntries)
            else -> null
        }
    }


    override fun getPropertyTitle(propertyKey: String): String {
        return when (propertyKey) {
            PROPERTY_MULTISELECTION -> context.applicationContext.getString(R.string.property_choice_allow_multiple_selections)
            PROPERTY_ALLOW_APPENDING_FROM_VIEW -> context.applicationContext.getString(R.string.msg_allow_appending_from_view)
            PROPERTY_ENTRIES -> context.applicationContext.getString(R.string.property_choice_entries)
            else -> ""
        }
    }

    fun getIsMultiSelectionAllowed(attribute: OTAttributeDAO): Boolean? {
        return getDeserializedPropertyValue<Boolean>(PROPERTY_MULTISELECTION, attribute)
    }

    fun getIsAppendingFromViewAllowed(attribute: OTAttributeDAO): Boolean {
        return getDeserializedPropertyValue<Boolean>(PROPERTY_ALLOW_APPENDING_FROM_VIEW, attribute)
                ?: false
    }

    fun getChoiceEntries(attribute: OTAttributeDAO): UniqueStringEntryList? {
        return getDeserializedPropertyValue(PROPERTY_ENTRIES, attribute)
    }

    override fun makeRecommendedChartModels(attribute: OTAttributeDAO, realm: Realm): Array<ChartModel<*>> {
        return arrayOf(ChoiceCategoricalBarChartModel(attribute, realm, context))
    }

    private fun getChoiceTexts(attribute: OTAttributeDAO, value: IntArray): List<String> {
        val entries = getChoiceEntries(attribute)
        val list = ArrayList<String>()
        if (entries != null) {
            for (idEntry in value.withIndex()) {

                val indexInEntries = entries.indexOf(idEntry.value)
                if (indexInEntries >= 0) {
                    list.add(entries[indexInEntries].text)
                }
            }
        }
        return list
    }

    override fun formatAttributeValue(attribute: OTAttributeDAO, value: Any): CharSequence {
        if (value is IntArray) {
            return getChoiceTexts(attribute, value).joinToString(",")
        } else return super.formatAttributeValue(attribute, value)
    }

    override fun onAddValueToTable(attribute: OTAttributeDAO, value: Any?, out: MutableList<String?>, uniqKey: String?) {
        if (value is IntArray) {
            out.add(getChoiceTexts(attribute, value).joinToString(","))
        } else out.add(null)
    }
}