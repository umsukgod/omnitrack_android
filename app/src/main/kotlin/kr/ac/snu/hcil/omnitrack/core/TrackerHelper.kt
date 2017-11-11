package kr.ac.snu.hcil.omnitrack.core

import io.realm.Realm
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttributeManager
import kr.ac.snu.hcil.omnitrack.core.database.local.models.OTTrackerDAO
import kr.ac.snu.hcil.omnitrack.core.visualization.ChartModel
import kr.ac.snu.hcil.omnitrack.core.visualization.models.DailyCountChartModel
import kr.ac.snu.hcil.omnitrack.core.visualization.models.LoggingHeatMapModel
import kr.ac.snu.hcil.omnitrack.core.visualization.models.TimelineComparisonLineChartModel
import java.util.*

/**
 * Created by younghokim on 2017. 10. 16..
 */
object TrackerHelper {

    fun makeRecommendedChartModels(trackerDao: OTTrackerDAO, realm: Realm): Array<ChartModel<*>> {

        val list = ArrayList<ChartModel<*>>()


        //generate tracker-level charts

        list += DailyCountChartModel(trackerDao, realm)
        list += LoggingHeatMapModel(trackerDao, realm)

        //add line timeline if numeric variables exist
        val numberAttrs = trackerDao.makeAttributesQuery(false,false).equalTo("type", OTAttributeManager.TYPE_NUMBER).findAll()
        if (numberAttrs.isNotEmpty()) {
            list.add(TimelineComparisonLineChartModel(numberAttrs, trackerDao, realm))
        }


        for (attribute in trackerDao.attributes.filter { it.isHidden == false && it.isInTrashcan == false }) {
            list.addAll(attribute.getHelper().makeRecommendedChartModels(attribute, realm))
        }

        return list.toTypedArray()
    }
}