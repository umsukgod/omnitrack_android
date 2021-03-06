package kr.ac.snu.hcil.omnitrack.core.dependency

import android.content.Context
import androidx.fragment.app.FragmentActivity
import io.reactivex.Single
import kr.ac.snu.hcil.android.common.TextHelper
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.externals.OTExternalService

/**
 * Created by Young-Ho on 5/27/2017.
 */
class ServiceActivationDependencyResolver(val service: OTExternalService) : OTSystemDependencyResolver() {
    override fun checkDependencySatisfied(context: Context, selfResolve: Boolean): Single<DependencyCheckResult> {
        return Single.defer {
            if (service.state == OTExternalService.ServiceState.ACTIVATED) {
                return@defer Single.just(DependencyCheckResult(DependencyState.Passed, TextHelper.formatWithResources(context, R.string.msg_service_is_activated_format, service.nameResourceId), ""))
            } else {
                return@defer Single.just(DependencyCheckResult(DependencyState.FatalFailed, TextHelper.formatWithResources(context, R.string.msg_service_is_not_activated_format, service.nameResourceId), ""))
            }
        }
    }

    override fun tryResolve(activity: FragmentActivity): Single<Boolean> {
        return service.startActivationActivityAsync(activity).firstOrError()
    }
}