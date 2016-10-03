package kr.ac.snu.hcil.omnitrack.core.externals.rescuetime

import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttribute
import kr.ac.snu.hcil.omnitrack.core.connection.OTTimeRangeQuery
import kr.ac.snu.hcil.omnitrack.core.externals.OTExternalService
import kr.ac.snu.hcil.omnitrack.core.externals.OTMeasureFactory
import kr.ac.snu.hcil.omnitrack.utils.serialization.SerializableTypedQueue
import kr.ac.snu.hcil.omnitrack.utils.serialization.TypeStringSerializationHelper
import org.json.JSONObject
import java.util.*

/**
 * Created by Young-Ho Kim on 2016-09-02.
 */
object RescueTimeProductivityMeasureFactory : OTMeasureFactory() {
    override val service: OTExternalService = RescueTimeService

    override fun isAttachableTo(attribute: OTAttribute<out Any>): Boolean {
        return attribute.typeId == OTAttribute.TYPE_NUMBER
    }

    override val isRangedQueryAvailable: Boolean = true

    override val isDemandingUserInput: Boolean = false

    override fun makeMeasure(): OTMeasure {
        return ProductivityMeasure()
    }

    override fun makeMeasure(serialized: String): OTMeasure {
        return ProductivityMeasure(serialized)
    }

    override val nameResourceId: Int = R.string.measure_rescuetime_productivity_name
    override val descResourceId: Int = R.string.measure_rescuetime_productivity_desc

    val productivityCalculator = object : RescueTimeService.ISummaryCalculator<Double> {
        override fun calculate(list: List<JSONObject>, startDate: Date, endDate: Date): Double? {
            return if (list.size > 0) {
                list.map { it.getDouble(RescueTimeService.SUMMARY_VARIABLE_PRODUCTIVITY) }.sum() / list.size
            } else null
        }

    }

    class ProductivityMeasure : OTRangeQueriedMeasure {
        override val dataTypeName: String = TypeStringSerializationHelper.TYPENAME_DOUBLE
        override val factory: OTMeasureFactory = RescueTimeProductivityMeasureFactory

        constructor() : super()
        constructor(serialized: String) : super(serialized)

        override fun awaitRequestValue(query: OTTimeRangeQuery?): Any {
            throw NotImplementedError("")
        }

        override fun requestValueAsync(start:Long, end: Long, handler: (Any?) -> Unit) {

            RescueTimeService.requestSummary(Date(start), Date(end - 1), productivityCalculator) {
                result ->
                handler.invoke(result)
            }
            /*
            val apiKey = RescueTimeService.getStoredApiKey()

            if (apiKey != null) {
                RescueTimeApi.queryProductivityScore(RescueTimeApi.Mode.ApiKey, Date(start), Date(end-1))
                {
                    result ->

                    handler.invoke(result)
                }
            } else {
                handler.invoke(null)
            }
*/
        }

        override fun onSerialize(typedQueue: SerializableTypedQueue) {

        }

        override fun onDeserialize(typedQueue: SerializableTypedQueue) {

        }

    }

}