package kr.ac.snu.hcil.omnitrack.core.externals

import android.content.Context
import android.content.SharedPreferences
import kr.ac.snu.hcil.omnitrack.core.di.global.ExternalService
import kr.ac.snu.hcil.omnitrack.core.externals.fitbit.FitbitService
import kr.ac.snu.hcil.omnitrack.core.externals.google.fit.GoogleFitService
import kr.ac.snu.hcil.omnitrack.core.externals.jawbone.JawboneUpService
import kr.ac.snu.hcil.omnitrack.core.externals.misfit.MisfitService
import kr.ac.snu.hcil.omnitrack.core.externals.rescuetime.RescueTimeService
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OTExternalServiceManager @Inject constructor(
        val context: Context,
        @ExternalService val preferences: SharedPreferences
) {

    private val factoryCodeDict = HashMap<String, OTMeasureFactory>()

    val availableServices: Array<OTExternalService> by lazy {
        val list =
                arrayOf(
                        //AndroidDeviceService,
                        GoogleFitService(context),
                        FitbitService(context),
                        JawboneUpService(context),
                        MisfitService(context),
                        RescueTimeService(context)
                        //,MicrosoftBandService
                        //,MiBandService
                ).filter { service -> service.isSupportedInSystem() }.toTypedArray()

        for (service in list) {
            service.initialize()
            for (factory in service.measureFactories) {
                factoryCodeDict.put(factory.typeCode, factory)
            }
        }
        list
    }

    fun findServiceByIdentifier(identifier: String): OTExternalService? {
        return availableServices.find { it.identifier == identifier }
    }

    fun getFilteredMeasureFactories(filter: (OTMeasureFactory) -> Boolean): List<OTMeasureFactory> {

        val list = ArrayList<OTMeasureFactory>()
        for (service in availableServices) {
            for (factory in service.measureFactories) {
                if (filter(factory)) {
                    list.add(factory)
                }
            }
        }

        return list
    }

    fun getMeasureFactoryByCode(typeCode: String): OTMeasureFactory? {
        if (availableServices.size > 0)
            return factoryCodeDict[typeCode]
        else return null
    }

    /***
     * Get whether the service's activation state stored in system
     */
    fun getIsActivatedFlag(service: OTExternalService): Boolean {
        return getIsActivatedFlag(service.identifier)
    }

    fun getIsActivatedFlag(serviceIdentifier: String): Boolean {
        return preferences.getBoolean(serviceIdentifier + "_activated", false)
    }

    fun setIsActivatedFlag(service: OTExternalService, isActivated: Boolean) {
        preferences.edit().putBoolean(service.identifier + "_activated", isActivated).apply()
    }
}