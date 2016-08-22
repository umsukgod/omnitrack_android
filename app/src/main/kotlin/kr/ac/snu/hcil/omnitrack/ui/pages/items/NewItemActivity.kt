package kr.ac.snu.hcil.omnitrack.ui.pages.items

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kr.ac.snu.hcil.omnitrack.OmniTrackApplication
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.OTItemBuilder
import kr.ac.snu.hcil.omnitrack.core.OTTracker
import kr.ac.snu.hcil.omnitrack.core.attributes.OTAttribute
import kr.ac.snu.hcil.omnitrack.ui.activities.MultiButtonActionBarActivity
import kr.ac.snu.hcil.omnitrack.ui.components.common.LockableFrameLayout
import kr.ac.snu.hcil.omnitrack.ui.components.decorations.HorizontalImageDividerItemDecoration
import kr.ac.snu.hcil.omnitrack.ui.components.inputs.attributes.AAttributeInputView
import kr.ac.snu.hcil.omnitrack.ui.components.inputs.properties.AInputView
import java.util.*

/**
 * Created by younghokim on 16. 7. 24..
 */
class NewItemActivity : MultiButtonActionBarActivity(R.layout.activity_new_item), OTItemBuilder.AttributeStateChangedListener {

    companion object {

        fun makeIntent(trackerId: String, context: Context): Intent {
            val intent = Intent(context, NewItemActivity::class.java)
            intent.putExtra(OmniTrackApplication.INTENT_EXTRA_OBJECT_ID_TRACKER, trackerId)
            return intent
        }
    }

    private val attributeListAdapter = AttributeListAdapter()

    private var tracker: OTTracker? = null

    private lateinit var builder: OTItemBuilder

    private lateinit var attributeListView: RecyclerView
    private val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    private val attributeValueExtractors = Hashtable<String, () -> Any>()

    private var skipViewValueCaching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rightActionBarButton?.visibility = View.VISIBLE
        leftActionBarButton?.visibility = View.VISIBLE
        leftActionBarButton?.setImageResource(R.drawable.back_rhombus)
        rightActionBarButton?.setImageResource(R.drawable.done)

        attributeListView = findViewById(R.id.ui_attribute_list) as RecyclerView
        attributeListView.layoutManager = layoutManager
        attributeListView.addItemDecoration(HorizontalImageDividerItemDecoration(R.drawable.horizontal_separator_pattern, this))

        attributeListView.adapter = attributeListAdapter
    }

    override fun onStart() {
        super.onStart()

        attributeValueExtractors.clear()

        val trackerId = intent.getStringExtra(OmniTrackApplication.INTENT_EXTRA_OBJECT_ID_TRACKER)
        if (trackerId != null) {
            tracker = OmniTrackApplication.app.currentUser.trackers.unObservedList.find { it.objectId == trackerId }
            if (tracker != null) {
                title = String.format(resources.getString(R.string.title_activity_new_item), tracker?.name)

                if (tryRestoreItemBuilderCache(tracker!!)) {
                    Toast.makeText(this, "Past inputs were restored.", Toast.LENGTH_SHORT).show()
                } else {
                    //new builder was created

                    builder.autoCompleteAsync(this) {
                        attributeListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (tracker == null) {
            tryRestoreItemBuilderCache(tracker!!)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!skipViewValueCaching) {
            storeItemBuilderCache()
            Toast.makeText(this, "Filled form content was stored.", Toast.LENGTH_SHORT).show()

        } else {
            skipViewValueCaching = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        for (inputView in attributeListAdapter.inputViews) {
            inputView.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (inputView in attributeListAdapter.inputViews) {
            inputView.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        for (inputView in attributeListAdapter.inputViews) {
            inputView.onLowMemory()
        }
    }

    override fun onToolbarLeftButtonClicked() {
        //back button
        finish()
    }

    override fun onToolbarRightButtonClicked() {
        //push item to db
        syncViewStateToBuilderAsync {
            val item = builder.makeItem()
            println("Will push $item")

            OmniTrackApplication.app.dbHelper.save(item, tracker!!)
            builder.clear()
            println(builder.getSerializedString())
            clearBuilderCache()
            skipViewValueCaching = true
            finish()
        }
    }

    private fun makeTrackerPreferenceKey(tracker: OTTracker): String {
        return "tracker_${tracker.objectId}"
    }

    private fun syncViewStateToBuilderAsync(finished: (() -> Unit)?) {
            var waitingAttributes = ArrayList<OTAttribute<out Any>>()
            for (attribute in tracker!!.attributes.unObservedList) {
                val valueExtractor = attributeValueExtractors[attribute.objectId]
                if (valueExtractor != null) {
                    builder.setValueOf(attribute, valueExtractor())
                } else {
                    waitingAttributes.add(attribute)
                }
            }

            var remain = waitingAttributes.size
        if (remain == 0) {
            finished?.invoke()
            return
        }

            for (attribute in waitingAttributes) {
                attribute.getAutoCompleteValueAsync {
                    result ->
                    remain--
                    builder.setValueOf(attribute, result)

                    if (remain == 0) {
                        //finish
                        finished?.invoke()
                    }
                }
            }
    }

    private fun clearBuilderCache() {
        if (tracker != null) {
            val preferences = getSharedPreferences(OmniTrackApplication.PREFERENCE_KEY_FOREGROUND_ITEM_BUILDER_STORAGE, Context.MODE_PRIVATE)
            val editor = preferences.edit();
            editor.remove(makeTrackerPreferenceKey(tracker!!))
            editor.apply()
        }
    }

    private fun storeItemBuilderCache() {
        if (tracker != null) {

            syncViewStateToBuilderAsync {
                if (!builder.isEmpty) {

                    val preferences = getSharedPreferences(OmniTrackApplication.PREFERENCE_KEY_FOREGROUND_ITEM_BUILDER_STORAGE, Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.putString(makeTrackerPreferenceKey(tracker!!), builder.getSerializedString())
                    editor.apply()
                }
            }

        }
    }

    private fun tryRestoreItemBuilderCache(tracker: OTTracker): Boolean {
        val preferences = getSharedPreferences(OmniTrackApplication.PREFERENCE_KEY_FOREGROUND_ITEM_BUILDER_STORAGE, Context.MODE_PRIVATE)
        val serialized = preferences.getString(makeTrackerPreferenceKey(tracker), null)
        if (serialized != null) {
            println("stored ItemBuilder was restored.")
            builder = OTItemBuilder(serialized)
            return true
        } else {
            println("new ItemBuilder created.")
            builder = OTItemBuilder(tracker, OTItemBuilder.MODE_FOREGROUND)
            return false
        }
    }

    override fun onAttributeStateChanged(attribute: OTAttribute<*>, position: Int, state: OTItemBuilder.EAttributeValueState) {
        println("attribute ${attribute.name} state was changed to $state")
        attributeListAdapter.notifyItemChanged(position)
    }


    private fun onAttributeValueChangedHandler(attributeId: String, newVal: Any) {
        println("Attribute $attributeId was changed to $newVal")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val inputView = attributeListAdapter.inputViews.find { it.position == AAttributeInputView.getPositionFromRequestCode(requestCode) }
            inputView?.setValueFromActivityResult(data, AAttributeInputView.getRequestTypeFromRequestCode(requestCode))
        }
    }

    inner class AttributeListAdapter : RecyclerView.Adapter<AttributeListAdapter.ViewHolder>() {

        val inputViews = HashSet<AAttributeInputView<*>>()

        fun getItem(position: Int): OTAttribute<out Any> {
            return tracker?.attributes?.get(position) ?: throw IllegalAccessException("Tracker is not attached to the activity.")
        }

        override fun getItemViewType(position: Int): Int {
            return getItem(position).getInputViewType(false)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

            val frame = LayoutInflater.from(this@NewItemActivity).inflate(R.layout.attribute_input_frame, parent, false);

            val inputView = AAttributeInputView.makeInstance(viewType, this@NewItemActivity)
            inputViews.add(inputView)

            inputView.onCreate(null)

            return ViewHolder(inputView, frame)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
            holder.inputView.position = position
        }

        override fun getItemCount(): Int {
            return tracker?.attributes?.size ?: 0
        }

        inner class ViewHolder(val inputView: AAttributeInputView<out Any>, val frame: View) : RecyclerView.ViewHolder(frame) {

            private val columnNameView: TextView
            private val attributeTypeView: TextView

            private val container: LockableFrameLayout

            private val indicatorInContainer: View

            private var attributeId: String? = null


            init {
                columnNameView = frame.findViewById(R.id.title) as TextView
                attributeTypeView = frame.findViewById(R.id.ui_attribute_type) as TextView
                container = frame.findViewById(R.id.ui_input_view_container) as LockableFrameLayout
                container.addView(inputView, 0)

                indicatorInContainer = frame.findViewById(R.id.ui_container_indicator)

                inputView.valueChanged += {
                    sender, args ->
                    onInputViewValueChanged(sender as AInputView<out Any>, args)
                }
            }

            private fun onInputViewValueChanged(sender: AInputView<out Any>, newVal: Any) {
                if (attributeId != null) {
                    onAttributeValueChangedHandler(attributeId!!, newVal)
                }
            }

            fun bind(attribute: OTAttribute<out Any>) {
                attribute.refreshInputViewUI(inputView)

                attributeId = attribute.objectId
                columnNameView.text = attribute.name
                attributeTypeView.text = resources.getString(attribute.typeNameResourceId)

                if (builder.hasValueOf(attribute)) {

                    inputView.valueChanged.suspend = true
                    inputView.setAnyValue(builder.getValueOf(attribute)!!)
                    inputView.valueChanged.suspend = false
                }

                attributeValueExtractors[attributeId] = {
                    inputView.value
                }

                val state = builder.getAttributeValueState(adapterPosition)

                if (state == OTItemBuilder.EAttributeValueState.Idle) {
                    container.locked = false
                    inputView.alpha = 1.0f
                } else {
                    container.locked = true
                    inputView.alpha = 0.12f
                }

                when (builder.getAttributeValueState(adapterPosition)) {
                    OTItemBuilder.EAttributeValueState.Idle -> {
                        indicatorInContainer.visibility = View.GONE
                    }
                    OTItemBuilder.EAttributeValueState.Processing -> {
                        indicatorInContainer.visibility = View.VISIBLE
                    }
                    OTItemBuilder.EAttributeValueState.GettingExternalValue -> {
                        indicatorInContainer.visibility = View.VISIBLE
                    }
                }

                inputView.onResume()
            }
        }
    }
}