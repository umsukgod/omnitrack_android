package kr.ac.snu.hcil.omnitrack.core.visualization

import android.content.Context
import android.text.format.DateUtils
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.core.datatypes.TimeSpan
import kr.ac.snu.hcil.omnitrack.utils.TimeHelper
import kr.ac.snu.hcil.omnitrack.utils.getYear
import java.util.*

/**
 * Created by Young-Ho Kim on 2016-09-08.
 */
interface ITimelineChart {
    enum class Granularity(val nameId: Int) {

        DAY(R.string.granularity_day) {
            override fun convertToRange(time: Long, out: TimeSpan) {
                val cal = GregorianCalendar.getInstance()
                cal.timeInMillis = time
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.HOUR, 0)
                cal.set(Calendar.AM_PM, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                out.from = cal.timeInMillis
                out.duration = DateUtils.DAY_IN_MILLIS
            }

            override fun getIntervalMillis(directionToNext: Boolean, pivot: Long): Long {
                return DateUtils.DAY_IN_MILLIS
            }

            override fun getFormattedCurrentScope(time: Long, context: Context): String {
                return TimeHelper.getDateText(time, context)
            }

        },

        WEEK(R.string.granularity_week) {
            override fun convertToRange(time: Long, out: TimeSpan) {
                DAY.convertToRange(time, out)

                val cal = GregorianCalendar.getInstance()
                cal.timeInMillis = out.from
                cal.set(Calendar.DAY_OF_WEEK, 1)

                out.from = cal.timeInMillis
                out.duration = DateUtils.WEEK_IN_MILLIS
            }

            override fun getIntervalMillis(directionToNext: Boolean, pivot: Long): Long {
                return DateUtils.WEEK_IN_MILLIS
            }

            override fun getFormattedCurrentScope(time: Long, context: Context): String {
                val ts = TimeSpan()
                convertToRange(time, ts)
                return "${TimeHelper.FORMAT_DAY.format(ts.from)} ~ ${TimeHelper.FORMAT_DAY.format(ts.to - 1)} "
            }

        },

        MONTH(R.string.granularity_month) {
            override fun convertToRange(time: Long, out: TimeSpan) {
                val cal = GregorianCalendar.getInstance()
                cal.timeInMillis = time
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR, 0)
                cal.set(Calendar.AM_PM, 0)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                out.from = cal.timeInMillis

                out.duration = (cal.getMaximum(Calendar.DAY_OF_MONTH) * DateUtils.DAY_IN_MILLIS)
            }

            override fun getIntervalMillis(directionToNext: Boolean, pivot: Long): Long {
                val cal = Calendar.getInstance()
                cal.timeInMillis = pivot
                cal.add(Calendar.MONTH, if (directionToNext) 1 else -1)

                /*
                val pivotZeroBasedMonth = cal.getZeroBasedMonth()
                val pivotDayOfMonth = cal.getDayOfMonth()

                val monthToMove = if(directionToNext)
                {
                    (pivotZeroBasedMonth + 1)%12
                }
                else{
                    if(pivotZeroBasedMonth==0) 11
                    else pivotZeroBasedMonth-1
                }

                if(cal.getMaxi)
                */


                return cal.timeInMillis - pivot
            }

            override fun getFormattedCurrentScope(time: Long, context: Context): String {
                return TimeHelper.FORMAT_MONTH.format(Date(time))
            }

        },

        YEAR(R.string.granularity_year) {
            override fun convertToRange(time: Long, out: TimeSpan) {
                val cal = GregorianCalendar.getInstance()
                cal.timeInMillis = time
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.HOUR, 0)
                cal.set(Calendar.AM_PM, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                out.from = cal.timeInMillis
                out.duration = DateUtils.YEAR_IN_MILLIS
            }

            override fun getIntervalMillis(directionToNext: Boolean, pivot: Long): Long {
                return DateUtils.YEAR_IN_MILLIS
            }

            override fun getFormattedCurrentScope(time: Long, context: Context): String {
                val cal = Calendar.getInstance()
                cal.timeInMillis = time
                return cal.getYear().toString()
            }


        };


        abstract fun convertToRange(time: Long, out: TimeSpan)
        abstract fun getIntervalMillis(directionToNext: Boolean, pivot: Long): Long

        abstract fun getFormattedCurrentScope(time: Long, context: Context): String
    }

    val isScopeControlSupported: Boolean

    fun setTimeScope(time: Long, scope: Granularity)

    fun getTimeScope(): TimeSpan


}