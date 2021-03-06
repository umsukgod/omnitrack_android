package kr.ac.snu.hcil.omnitrack.core.attributes.helpers

import android.content.Context
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import kr.ac.snu.hcil.android.common.containers.Nullable
import kr.ac.snu.hcil.android.common.time.TimeHelper
import kr.ac.snu.hcil.omnitrack.OTAndroidApp
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.attributes.FallbackPolicyResolver
import kr.ac.snu.hcil.omnitrack.core.attributes.NumericCharacteristics
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.AFieldValueSorter
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.TimeSpanIntervalSorter
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.TimeSpanPivotalSorter
import kr.ac.snu.hcil.omnitrack.core.attributes.properties.OTPropertyHelper
import kr.ac.snu.hcil.omnitrack.core.attributes.properties.OTPropertyManager
import kr.ac.snu.hcil.omnitrack.core.database.models.OTAttributeDAO
import kr.ac.snu.hcil.omnitrack.core.database.models.OTItemValueEntryDAO
import kr.ac.snu.hcil.omnitrack.core.serialization.TypeStringSerializationHelper
import kr.ac.snu.hcil.omnitrack.core.types.TimeSpan
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Young-Ho on 10/7/2017.
 */
class OTTimeSpanAttributeHelper(context: Context) : OTAttributeHelper(context) {

    companion object {
        const val PROPERTY_GRANULARITY = "granularity"
        const val PROPERTY_TYPE = "type"

        const val FALLBACK_POLICY_ID_TIMESPAN_CONNECT_PREVIOUS = 11

        const val GRANULARITY_DAY = 0
        const val GRANULARITY_MINUTE = 1
    }

    val formats = mapOf(
            Pair(OTTimeAttributeHelper.GRANULARITY_DAY, SimpleDateFormat(context.getString(R.string.property_time_format_granularity_day))),
            Pair(OTTimeAttributeHelper.GRANULARITY_MINUTE, SimpleDateFormat(context.getString(R.string.property_time_format_granularity_minute)))
    )

    private val app = context.applicationContext as OTAndroidApp

    override fun getValueNumericCharacteristics(attribute: OTAttributeDAO): NumericCharacteristics = NumericCharacteristics(true, false)

    override fun getTypeNameResourceId(attribute: OTAttributeDAO): Int {
        return R.string.type_timespan_name
    }

    override fun getTypeSmallIconResourceId(attribute: OTAttributeDAO): Int {
        return R.drawable.icon_small_timer
    }

    override fun getSupportedSorters(attribute: OTAttributeDAO): Array<AFieldValueSorter> {
        return arrayOf(
                TimeSpanPivotalSorter("${attribute.name} start", true, attribute.localId),
                TimeSpanPivotalSorter("${attribute.name} end", false, attribute.localId),
                TimeSpanIntervalSorter("${attribute.name} interval", attribute.localId)
        )
    }

    override val typeNameForSerialization: String = TypeStringSerializationHelper.TYPENAME_TIMESPAN

    override val propertyKeys: Array<String> = arrayOf(PROPERTY_GRANULARITY)

    override fun <T> getPropertyHelper(propertyKey: String): OTPropertyHelper<T> {
        return when (propertyKey) {
        //PROPERTY_TYPE->OTPropertyManager.getHelper(OTPropertyManager.EPropertyType.Selection).parseValue(serializedValue)
            PROPERTY_GRANULARITY -> propertyManager.getHelper(OTPropertyManager.EPropertyType.Selection)
            else -> throw IllegalArgumentException("Unsupported property type $propertyKey")
        } as OTPropertyHelper<T>
    }


    fun getGranularity(attribute: OTAttributeDAO): Int {
        return getDeserializedPropertyValue<Int>(PROPERTY_GRANULARITY, attribute) ?: GRANULARITY_MINUTE
    }

    override fun getPropertyTitle(propertyKey: String): String {
        return when (propertyKey) {
            PROPERTY_GRANULARITY -> context.applicationContext.getString(R.string.property_time_granularity)
            else -> ""
        }
    }

    override fun getPropertyInitialValue(propertyKey: String): Any? {
        return when (propertyKey) {
            PROPERTY_GRANULARITY -> GRANULARITY_DAY
            else -> null
        }
    }

    override val supportedFallbackPolicies: LinkedHashMap<Int, FallbackPolicyResolver>
        get() = super.supportedFallbackPolicies.apply {
            this[OTAttributeDAO.DEFAULT_VALUE_POLICY_FILL_WITH_INTRINSIC_VALUE] = object : FallbackPolicyResolver(context.applicationContext, R.string.msg_intrinsic_time, isValueVolatile = true) {
                override fun getFallbackValue(attribute: OTAttributeDAO, realm: Realm): Single<Nullable<out Any>> {
                    return Single.just(Nullable(TimeSpan().apply { this.timeZone = app.applicationComponent.getPreferredTimeZone() }))
                }
            }

            this[FALLBACK_POLICY_ID_TIMESPAN_CONNECT_PREVIOUS] = object : FallbackPolicyResolver(context.applicationContext, R.string.msg_attribute_fallback_policy_timespan_connect_previous, isValueVolatile = true) {
                override fun getFallbackValue(attribute: OTAttributeDAO, realm: Realm): Single<Nullable<out Any>> {
                    return Single.defer {
                        val previousNotNullEntry = try {
                            realm.where(OTItemValueEntryDAO::class.java)
                                    .equalTo("key", attribute.localId)
                                    .equalTo("items.trackerId", attribute.trackerId)
                                    .equalTo("items.removed", false)
                                    .isNotNull("value")
                                    .beginsWith("value", "${TypeStringSerializationHelper.TYPENAME_TIMESPAN.length}${TypeStringSerializationHelper.TYPENAME_TIMESPAN}")
                                    .findAll().sortedByDescending { (it.items?.firstOrNull()?.getValueOf(attribute.localId) as TimeSpan).to }.firstOrNull()
                        } catch (ex: NoSuchElementException) {
                            null
                        }

                        val previousTimespan = previousNotNullEntry?.value?.let { TypeStringSerializationHelper.deserialize(it) as? TimeSpan }
                        return@defer if (previousTimespan != null) {
                            Single.just<Nullable<out Any>>(
                                    Nullable(TimeSpan.fromPoints(previousTimespan.to, System.currentTimeMillis())))
                        } else Single.just<Nullable<out Any>>(Nullable(null))
                    }.subscribeOn(AndroidSchedulers.mainThread())
                }
            }

            this.remove(OTAttributeDAO.DEFAULT_VALUE_POLICY_NULL)
        }

    override fun initialize(attribute: OTAttributeDAO) {
        attribute.fallbackValuePolicy = OTAttributeDAO.DEFAULT_VALUE_POLICY_FILL_WITH_INTRINSIC_VALUE
    }

    override fun formatAttributeValue(attribute: OTAttributeDAO, value: Any): CharSequence {
        return (value as? TimeSpan)?.let {
            val granularity = getGranularity(attribute)
            val format = when (granularity) {
                GRANULARITY_DAY -> formats[GRANULARITY_DAY]!!
                GRANULARITY_MINUTE -> formats[GRANULARITY_MINUTE]!!
                else -> formats[GRANULARITY_MINUTE]!!
            }

            val from = format.format(Date(it.from))
            val to = format.format(Date(it.to))

            val overlapUntil = 0
            /*
            while( from[overlapUntil] == to[overlapUntil] )
            {
                overlapUntil++
                if(overlapUntil>=from.length || overlapUntil >= to.length){break;}
            }*/

            val builder = StringBuilder(from)
            builder.append(" ~ ").append(to.subSequence(overlapUntil, to.length)).toString()

        } ?: ""
    }

    /*
    override fun makeRecommendedChartModels(attribute: OTAttributeDAO, realm: Realm): Array<ChartModel<*>> {
        return arrayOf(DurationTimelineModel(attribute, realm))
    }*/

    override fun onAddColumnToTable(attribute: OTAttributeDAO, out: MutableList<String>) {
        out.add("${getAttributeUniqueName(attribute)}_start_epoch")
        out.add("${getAttributeUniqueName(attribute)}_end_epoch")
        out.add("${getAttributeUniqueName(attribute)}_duration_millis")
        out.add("${getAttributeUniqueName(attribute)}_timezone")
    }

    override fun onAddValueToTable(attribute: OTAttributeDAO, value: Any?, out: MutableList<String?>, uniqKey: String?) {
        if (value is TimeSpan) {

            val formatter = (TimeHelper.FORMAT_ISO_8601.clone() as SimpleDateFormat).apply { timeZone = value.timeZone }
            val fromFormatted = formatter.format(Date(value.from))
            val toFormatted = formatter.format(Date(value.to))
            out.add(fromFormatted)
            out.add(toFormatted)
            out.add(value.duration.toString())
            out.add(value.timeZone.getDisplayName(Locale.ENGLISH))

        } else {
            out.add(null)
            out.add(null)
            out.add(null)
            out.add(null)
        }
    }
}