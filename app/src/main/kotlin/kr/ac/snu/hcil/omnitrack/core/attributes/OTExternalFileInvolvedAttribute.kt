package kr.ac.snu.hcil.omnitrack.core.attributes

import kr.ac.snu.hcil.omnitrack.core.OTTracker
import rx.Single
import java.io.OutputStream

/**
 * Created by Young-Ho Kim on 2017-03-09.
 */
abstract class OTExternalFileInvolvedAttribute<T>(objectId: String?, localKey: Int?, parentTracker: OTTracker?, columnName: String, isRequired: Boolean, typeId: Int, propertyData: Map<String, Any?>?, connectionData: String?)
    : OTAttribute<T>(objectId, localKey, parentTracker, columnName, isRequired, typeId, propertyData, connectionData) {

    override val isExternalFile: Boolean = true

    abstract fun storeValueFile(value: Any?, outputStream: OutputStream): Single<Void>

    override fun onAddColumnToTable(out: MutableList<String>) {
        out.add("${getAttributeUniqueName()}_filepath")
    }

    protected abstract fun isValueContainingFileInfo(value: Any?): Boolean

    abstract fun makeRelativeFilePathFromValue(value: Any?, uniqKey: String?): String

    override fun onAddValueToTable(value: Any?, out: MutableList<String?>, uniqKey: String?) {
        if (isValueContainingFileInfo(value)) {
            out.add(makeRelativeFilePathFromValue(value, uniqKey))
        } else out.add(null)
    }
}