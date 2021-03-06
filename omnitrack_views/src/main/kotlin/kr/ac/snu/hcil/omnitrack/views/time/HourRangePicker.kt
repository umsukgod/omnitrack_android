package kr.ac.snu.hcil.omnitrack.views.time

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.component_hour_range_picker.view.*
import kr.ac.snu.hcil.android.common.events.Event
import kr.ac.snu.hcil.omnitrack.views.R

/**
 * Created by Young-Ho on 8/25/2016.
 */
class HourRangePicker : ConstraintLayout {

    companion object {
        val oClockFormatter = { value: Int ->
            String.format("%02d", value) + ":00"
        }

        fun getTimeText(context: Context, hourOfDay: Int, wrapNextDay: Boolean = false): String {
            return when (hourOfDay) {
                0 -> context.resources.getString(R.string.msg_midnight_0)
                12 -> context.resources.getString(R.string.msg_noon)
                24 -> context.resources.getString(R.string.msg_midnight_24)
                else -> {
                    val amPm = if (hourOfDay / 12 == 0) "AM" else "PM"
                    val hour = hourOfDay % 12
                    String.format("%02d", hour) + ":00 $amPm"
                }
            }.let {
                if (wrapNextDay) {
                    String.format(context.resources.getString(R.string.msg_format_next_day), it)
                } else it
            }
        }
    }

    var fromHourOfDay: Int
        get() = ui_picker_from.value
        set(value) {
            ui_picker_from.setValue(value)
        }

    var toHourOfDay: Int
        get() = ui_picker_to.value
        set(value) {
            ui_picker_to.setValue(value)
        }


    val onRangeChanged = Event<Pair<Int, Int>>()


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.component_hour_range_picker, this, true)

        ui_picker_from.minValue = 0
        ui_picker_from.maxValue = 23
        ui_picker_to.minValue = 1
        ui_picker_to.maxValue = 24
        ui_picker_from.formatter = oClockFormatter
        ui_picker_to.formatter = oClockFormatter

        val handler = { sender: Any, args: INumericUpDown.ChangeArgs ->
            onRangeChanged.invoke(this, Pair(fromHourOfDay, toHourOfDay))

            ui_description.text = if (ui_picker_to.value == ui_picker_from.value || (ui_picker_from.value == 0 && ui_picker_to.value == 24)) {
                resources.getString(R.string.msg_full_day)
            } else {
                val toNextDay = ui_picker_to.value < ui_picker_from.value
                val durationLength = if (toNextDay) {
                    24 - ui_picker_from.value + ui_picker_to.value
                } else ui_picker_to.value - ui_picker_from.value
                "${getTimeText(context, ui_picker_from.value)} - ${getTimeText(context, ui_picker_to.value, toNextDay)} (${resources.getQuantityString(R.plurals.time_duration_hour_short, durationLength, durationLength)})"
            }
        }

        ui_picker_from.valueChanged += handler

        ui_picker_to.valueChanged += handler
    }

}