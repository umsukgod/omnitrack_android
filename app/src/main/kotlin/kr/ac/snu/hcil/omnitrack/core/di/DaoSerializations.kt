package kr.ac.snu.hcil.omnitrack.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.realm.Realm
import kr.ac.snu.hcil.omnitrack.core.database.local.DaoSerializationManager
import kr.ac.snu.hcil.omnitrack.core.database.local.OTAttributeDAO
import kr.ac.snu.hcil.omnitrack.core.database.local.OTTriggerDAO
import kr.ac.snu.hcil.omnitrack.core.database.local.typeadapters.AttributeTypeAdapter
import kr.ac.snu.hcil.omnitrack.core.database.local.typeadapters.TriggerTypeAdapter
import javax.inject.Provider
import javax.inject.Qualifier

/**
 * Created by younghokim on 2017-11-02.
 */
@Module
class DaoSerializationModule {
    @Provides
    @ApplicationScope
    @ForTrigger
    fun getTriggerGson(realm: Provider<Realm>): Gson {
        return GsonBuilder().registerTypeAdapter(OTTriggerDAO::class.java, TriggerTypeAdapter(realm)).create()
    }

    @Provides
    @ApplicationScope
    @ForAttribute
    fun getAttributeGson(): Gson {
        return GsonBuilder().registerTypeAdapter(OTAttributeDAO::class.java, AttributeTypeAdapter()).create()
    }

}

@ApplicationScope
@Subcomponent(modules = arrayOf(DaoSerializationModule::class))
interface DaoSerializationComponent {

    @Subcomponent.Builder
    interface Builder {
        fun setModule(module: DaoSerializationModule): Builder
        fun build(): DaoSerializationComponent
    }

    fun manager(): Lazy<DaoSerializationManager>
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME) annotation class ForTrigger

@Qualifier
@Retention(AnnotationRetention.RUNTIME) annotation class ForTracker

@Qualifier
@Retention(AnnotationRetention.RUNTIME) annotation class ForAttribute

@Qualifier
@Retention(AnnotationRetention.RUNTIME) annotation class ForItem