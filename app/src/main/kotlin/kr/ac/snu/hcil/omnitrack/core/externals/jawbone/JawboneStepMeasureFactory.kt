package kr.ac.snu.hcil.omnitrack.core.externals.jawbone

import com.google.gson.JsonObject
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttribute
import kr.ac.snu.hcil.omnitrack.core.connection.OTTimeRangeQuery
import kr.ac.snu.hcil.omnitrack.core.externals.OTExternalService
import kr.ac.snu.hcil.omnitrack.core.externals.OTMeasureFactory
import kr.ac.snu.hcil.omnitrack.utils.serialization.SerializableTypedQueue
import kr.ac.snu.hcil.omnitrack.utils.serialization.TypeStringSerializationHelper

/**
 * Created by younghokim on 2017. 1. 25..
 */
object JawboneStepMeasureFactory : OTMeasureFactory("step") {
    override val exampleAttributeType: Int = OTAttribute.TYPE_NUMBER

    override fun getExampleAttributeConfigurator(): IExampleAttributeConfigurator {
        return CONFIGURATOR_STEP_ATTRIBUTE
    }

    override fun isAttachableTo(attribute: OTAttribute<out Any>): Boolean {
        return attribute.typeId == OTAttribute.TYPE_NUMBER
    }

    override val isRangedQueryAvailable: Boolean = true
    override val minimumGranularity: OTTimeRangeQuery.Granularity = OTTimeRangeQuery.Granularity.Minute
    override val isDemandingUserInput: Boolean = false


    override fun makeMeasure(): OTMeasure {
        return JawboneStepMeasure()
    }

    override fun makeMeasure(serialized: String): OTMeasure {
        return JawboneStepMeasure(serialized)
    }

    override val service: OTExternalService = JawboneUpService

    override val descResourceId: Int = R.string.measure_steps_desc
    override val nameResourceId: Int = R.string.measure_steps_name

    class JawboneStepMeasure : AJawboneMoveMeasure {

        override fun extractValueFromItem(item: JsonObject): Float {
            return item.getAsJsonObject("details").get("steps").asFloat
        }

        override val dataTypeName: String = TypeStringSerializationHelper.TYPENAME_FLOAT

        override val factory: OTMeasureFactory = JawboneStepMeasureFactory

        constructor() : super()
        constructor(serialized: String) : super(serialized)

        override fun onSerialize(typedQueue: SerializableTypedQueue) {
        }

        override fun onDeserialize(typedQueue: SerializableTypedQueue) {
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            else return other is JawboneStepMeasure
        }

        override fun hashCode(): Int {
            return factoryCode.hashCode()
        }
    }
}