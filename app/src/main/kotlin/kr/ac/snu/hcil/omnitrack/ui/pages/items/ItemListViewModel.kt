package kr.ac.snu.hcil.omnitrack.ui.pages.items

import android.support.v7.util.DiffUtil
import io.realm.OrderedCollectionChangeSet
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kr.ac.snu.hcil.omnitrack.OTApplication
import kr.ac.snu.hcil.omnitrack.core.OTItem
import kr.ac.snu.hcil.omnitrack.core.attributes.logics.ItemComparator
import kr.ac.snu.hcil.omnitrack.core.database.local.OTAttributeDAO
import kr.ac.snu.hcil.omnitrack.core.database.local.OTItemDAO
import kr.ac.snu.hcil.omnitrack.core.database.local.OTTrackerDAO
import kr.ac.snu.hcil.omnitrack.utils.RealmViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

/**
 * Created by Young-Ho on 10/12/2017.
 */
class ItemListViewModel : RealmViewModel(), OrderedRealmCollectionChangeListener<RealmResults<OTItemDAO>> {

    private var currentItemQueryResults: RealmResults<OTItemDAO>? = null
    private lateinit var trackerDao: OTTrackerDAO
    private lateinit var managedTrackerDao: OTTrackerDAO
    private lateinit var managedAttributeList: RealmResults<OTAttributeDAO>

    val trackerId: String get() = trackerDao.objectId!!

    val trackerNameObservable = BehaviorSubject.create<String>("")
    var trackerName: String
        get() = trackerNameObservable.value
        private set(value) {
            if (trackerNameObservable.value != value) {
                trackerNameObservable.onNext(value)
            }
        }

    val attributes: List<OTAttributeDAO> get() = trackerDao.attributes.toList()

    //localKey / Type
    val onSchemaChanged = PublishSubject.create<List<OTAttributeDAO>>()

    val sorterSetObservable = BehaviorSubject.create<List<ItemComparator>>()
    private val currentSorterSet = ArrayList<ItemComparator>()

    val currentSorterObservable = BehaviorSubject.create<ItemComparator>(ItemComparator.TIMESTAMP_SORTER)
    var currentSorter: ItemComparator
        get() {
            return currentSorterObservable.value
        }
        set(value) {
            if (currentSorterObservable.value != value) {
                currentSorterObservable.onNext(value)
            }
        }

    private val itemsInTimestampDescendingOrder = ArrayList<ItemViewModel>()


    private val itemsSortedList = ArrayList<ItemViewModel>()

    val sortedItemsObservable = BehaviorSubject.create<List<ItemViewModel>>()

    private val itemComparerMethod = object : Comparator<ItemViewModel> {
        override fun compare(p0: ItemViewModel?, p1: ItemViewModel?): Int {
            return currentSorter.compare(p0?.itemDao, p1?.itemDao)
        }
    }

    fun init(trackerId: String) {
        val dao = OTApplication.app.databaseManager.getTrackerQueryWithId(trackerId, realm).findFirst()

        if (dao != null) {
            managedTrackerDao = OTApplication.app.databaseManager.getTrackerQueryWithId(trackerId, realm).findFirstAsync()
            managedTrackerDao.addChangeListener<OTTrackerDAO> { snapshot ->
                println("tracker db changed in background")
            }
            managedAttributeList = OTApplication.app.databaseManager.getAttributeListQuery(trackerId, realm).findAllAsync()
            managedAttributeList.addChangeListener { snapshot, changeSet ->
                println("tracker attribute list db changed in background")
            }

            refreshTracker(realm.copyFromRealm(dao))
        }
    }

    private fun refreshTracker(trackerDao: OTTrackerDAO) {
        this.trackerDao = trackerDao
        trackerName = trackerDao.name

        currentSorterSet.clear()
        currentSorterSet.add(ItemComparator.TIMESTAMP_SORTER)
        this.trackerDao.attributes.forEach {
            currentSorterSet += it.getHelper().getSupportedSorters(it)
        }

        sorterSetObservable.onNext(currentSorterSet)
        onSchemaChanged.onNext(trackerDao.attributes.toList())

        currentItemQueryResults?.removeAllChangeListeners()
        currentItemQueryResults = OTApplication.app.databaseManager
                .makeItemsQuery(trackerDao.objectId, null, null, realm)
                .findAllSortedAsync("timestamp", Sort.DESCENDING)
        currentItemQueryResults?.addChangeListener(this)
    }

    override fun onChange(snapshot: RealmResults<OTItemDAO>, changeSet: OrderedCollectionChangeSet?) {
        if (changeSet == null) {
            //initial fetch
            itemsInTimestampDescendingOrder.clear()
            itemsInTimestampDescendingOrder.addAll(snapshot.map { ItemViewModel(it) })

            itemsSortedList.clear()
            itemsSortedList.addAll(itemsInTimestampDescendingOrder)
            itemsSortedList.sortWith(itemComparerMethod)

        } else {
            //TODO
        }

        sortedItemsObservable.onNext(itemsSortedList)
    }

    fun setSorter(itemComparator: ItemComparator) {
        if (currentSorter != itemComparator) {
            currentSorter = itemComparator
            itemsSortedList.sortWith(itemComparerMethod)
            sortedItemsObservable.onNext(itemsSortedList)
        }
    }

    class ItemViewModel(val itemDao: OTItemDAO) {
        val itemId: String? get() = itemDao.objectId
        val isSynchronized: Boolean get() = itemDao.synchronizedAt != null

        val timestampObservable = BehaviorSubject.create<Long>()
        var timestamp: Long
            get() = timestampObservable.value
            private set(value) {
                if (timestampObservable.value != value) {
                    timestampObservable.onNext(value)
                }
            }

        val loggingSourceObservable = BehaviorSubject.create<OTItem.LoggingSource>()
        var loggingSource: OTItem.LoggingSource
            get() = loggingSourceObservable.value
            set(value) {
                if (loggingSourceObservable.value != value) {
                    loggingSourceObservable.onNext(value)
                }
            }

        init {
            timestamp = itemDao.timestamp
            loggingSource = itemDao.loggingSource
        }
    }

    class ItemViewModelListDiffUtilCallback(val oldList: List<ItemViewModel>, val newList: List<ItemViewModel>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].itemId == newList[newItemPosition].itemId
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areItemsTheSame(oldItemPosition, newItemPosition)
        }


    }
}