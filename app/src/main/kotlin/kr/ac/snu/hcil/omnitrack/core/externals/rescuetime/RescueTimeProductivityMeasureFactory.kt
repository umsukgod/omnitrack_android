package kr.ac.snu.hcil.omnitrack.core.externals.rescuetime

import android.content.Context
import com.google.gson.stream.JsonReader
import io.reactivex.Single
import kr.ac.snu.hcil.android.common.containers.Nullable
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttributeManager
import kr.ac.snu.hcil.omnitrack.core.connection.OTTimeRangeQuery
import kr.ac.snu.hcil.omnitrack.core.externals.OTServiceMeasureFactory
import kr.ac.snu.hcil.omnitrack.core.serialization.TypeStringSerializationHelper
import org.json.JSONObject
import java.util.*

/**
 * Created by Young-Ho Kim on 2016-09-02.
 */
class RescueTimeProductivityMeasureFactory(context: Context, service: RescueTimeService) : OTServiceMeasureFactory(context, service, "prd") {

    override val dataTypeName: String = TypeStringSerializationHelper.TYPENAME_DOUBLE

    override fun getAttributeType() = OTAttributeManager.TYPE_NUMBER

    override val isRangedQueryAvailable: Boolean = true

    override val minimumGranularity: OTTimeRangeQuery.Granularity? = OTTimeRangeQuery.Granularity.Hour

    override val isDemandingUserInput: Boolean = false

    override fun makeMeasure(): OTMeasure {
        return ProductivityMeasure(this)
    }

    override fun makeMeasure(reader: JsonReader): OTMeasure {
        return ProductivityMeasure(this)
    }

    override fun makeMeasure(serialized: String): OTMeasure {
        return ProductivityMeasure(this)
    }

    override fun serializeMeasure(measure: OTMeasure): String {
        return "{}"
    }

    override val nameResourceId: Int = R.string.measure_rescuetime_productivity_name
    override val descResourceId: Int = R.string.measure_rescuetime_productivity_desc

    val productivityCalculator = object : RescueTimeService.ISummaryCalculator<Double> {
        override fun calculate(list: List<JSONObject>, startDate: Date, endDate: Date): Double? {
            return if (list.isNotEmpty()) {
                list.asSequence().map { it.getDouble(RescueTimeService.SUMMARY_VARIABLE_PRODUCTIVITY) }.sum() / list.size
            } else null
        }

    }

    class ProductivityMeasure(factory: RescueTimeProductivityMeasureFactory) : OTRangeQueriedMeasure(factory) {


        override fun getValueRequest(start: Long, end: Long): Single<Nullable<out Any>> {
            val factory = getFactory<RescueTimeProductivityMeasureFactory>()
            return factory.getService<RescueTimeService>().getSummaryRequest(Date(start), Date(end - 1), factory.productivityCalculator) as Single<Nullable<out Any>>
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            else return other is ProductivityMeasure
        }

        override fun hashCode(): Int {
            return factoryCode.hashCode()
        }

    }

}