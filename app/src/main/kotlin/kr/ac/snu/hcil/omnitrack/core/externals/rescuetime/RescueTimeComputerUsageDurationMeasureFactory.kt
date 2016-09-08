package kr.ac.snu.hcil.omnitrack.core.externals.rescuetime

import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.OTItemBuilder
import kr.ac.snu.hcil.omnitrack.core.OTTimeRangeQuery
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttribute
import kr.ac.snu.hcil.omnitrack.core.externals.OTExternalService
import kr.ac.snu.hcil.omnitrack.core.externals.OTMeasureFactory
import kr.ac.snu.hcil.omnitrack.utils.serialization.SerializableTypedQueue
import kr.ac.snu.hcil.omnitrack.utils.serialization.TypeStringSerializationHelper
import java.util.*

/**
 * Created by Young-Ho Kim on 2016-09-02.
 */
object RescueTimeComputerUsageDurationMeasureFactory : OTMeasureFactory() {

    override val supportedConditionerTypes: IntArray = CONDITIONERS_FOR_SINGLE_NUMERIC_VALUE

    override val service: OTExternalService = RescueTimeService

    override fun isAttachableTo(attribute: OTAttribute<out Any>): Boolean {
        return attribute.typeId == OTAttribute.TYPE_NUMBER
    }

    override val isRangedQueryAvailable: Boolean = true
    override val isDemandingUserInput: Boolean = false

    override fun makeMeasure(): OTMeasure {
        return ComputerUsageDurationMeasure()
    }

    override fun makeMeasure(serialized: String): OTMeasure {
        return ComputerUsageDurationMeasure(serialized)
    }

    override val nameResourceId: Int = R.string.measure_rescuetime_computer_usage_name
    override val descResourceId: Int = R.string.measure_rescuetime_computer_usage_desc


    class ComputerUsageDurationMeasure : OTRangeQueriedMeasure {
        override val dataTypeName: String = TypeStringSerializationHelper.TYPENAME_DOUBLE
        override val factory: OTMeasureFactory = RescueTimeComputerUsageDurationMeasureFactory

        override fun awaitRequestValue(query: OTTimeRangeQuery?): Any {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun requestValueAsync(start:Long, end: Long, handler: (Any?) -> Unit) {

            val apiKey = RescueTimeService.getStoredApiKey()

            if (apiKey != null) {
                RescueTimeApi.queryUsageDuration(RescueTimeApi.Mode.ApiKey, Date(start), Date(end-1))
                {
                    result ->
                    println(result)
                    handler.invoke(result)
                }
            } else {
                handler.invoke(null)
            }
        }

        override fun onSerialize(typedQueue: SerializableTypedQueue) {
        }

        override fun onDeserialize(typedQueue: SerializableTypedQueue) {
        }

        constructor() : super()
        constructor(serialized: String) : super(serialized)

    }

}