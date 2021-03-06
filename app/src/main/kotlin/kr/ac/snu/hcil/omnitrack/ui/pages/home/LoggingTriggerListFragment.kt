package kr.ac.snu.hcil.omnitrack.ui.pages.home

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Single
import kr.ac.snu.hcil.omnitrack.core.database.models.OTTriggerDAO
import kr.ac.snu.hcil.omnitrack.ui.pages.trigger.ATriggerListFragment

/**
 * Created by younghokim on 2017. 10. 21..
 */
class LoggingTriggerListFragment : ATriggerListFragment<LoggingTriggerListViewModel>() {
    override fun initializeNewViewModel(savedInstanceState: Bundle?): Single<LoggingTriggerListViewModel> {
        return Single.just(ViewModelProviders.of(this).get(LoggingTriggerListViewModel::class.java))
    }

    override fun onViewModelMounted(viewModel: LoggingTriggerListViewModel, savedInstanceState: Bundle?) {
        val parentViewModel = ViewModelProviders.of(activity!!).get(HomeScreenViewModel::class.java)
        creationSubscriptions.add(
                parentViewModel.userIdObservable.subscribe { (userId) ->
                    if (userId != null)
                        viewModel.init(userId)
            }
        )

        super.onViewModelMounted(viewModel, savedInstanceState)
    }

    override fun onProcessNewDefaultTrigger(dao: OTTriggerDAO) {
        super.onProcessNewDefaultTrigger(dao)
        dao.actionType = OTTriggerDAO.ACTION_TYPE_LOG
    }
}