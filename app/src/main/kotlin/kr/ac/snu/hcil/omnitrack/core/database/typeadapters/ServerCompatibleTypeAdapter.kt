package kr.ac.snu.hcil.omnitrack.core.database.typeadapters

import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.realm.RealmObject
import kr.ac.snu.hcil.omnitrack.core.database.JsonObjectApplier

/**
 * Created by younghokim on 2017. 11. 6..
 */
abstract class ServerCompatibleTypeAdapter<T : RealmObject>(val isServerMode: Boolean) : TypeAdapter<T>(), JsonObjectApplier<T> {
    final override fun read(reader: JsonReader): T {
        return read(reader, isServerMode)
    }

    final override fun write(out: JsonWriter, value: T) {
        write(out, value, isServerMode)
    }

    abstract fun read(reader: JsonReader, isServerMode: Boolean): T
    abstract fun write(writer: JsonWriter, value: T, isServerMode: Boolean)

    override fun decodeToDao(json: JsonObject): T {
        return this.fromJsonTree(json)
    }
}