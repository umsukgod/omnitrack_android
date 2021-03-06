package kr.ac.snu.hcil.android.common.file

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opencsv.CSVWriter
/*
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Colour
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont*/
import java.io.OutputStream
import java.util.*

/**
 * Created by Young-Ho on 3/8/2017.
 */
class StringTableSheet {
    val columns: MutableList<String> = ArrayList()
    val rows: MutableList<MutableList<String?>> = ArrayList()

    override fun toString(): String {
        return "table with ${columns.size} columns, ${rows.size} rows."
    }

    fun storeCsvToStream(outputStream: OutputStream) {
        val csvWriter = CSVWriter(outputStream.writer(Charsets.UTF_8))

        csvWriter.writeNext(columns.toTypedArray())
        csvWriter.writeAll(rows.map { it.toTypedArray() })
        csvWriter.close()
    }

    /*
        fun storeExcelToStream(outputStream: OutputStream) {
            val workbookSettings = WorkbookSettings()
            workbookSettings.locale = Locale.ENGLISH

            val workbook = Workbook.createWorkbook(outputStream, workbookSettings)
            val tableSheet = workbook.createSheet("table", 0)

            val headerFormat = WritableCellFormat(WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE))
            headerFormat.wrap = false
            headerFormat.setBackground(Colour.GREY_25_PERCENT)

            //header
            for (headerText in columns.withIndex()) {
                val cell = Label(headerText.index, 0, headerText.value, headerFormat)
                tableSheet.addCell(cell)
            }

            val normalCellFormat = WritableCellFormat(WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE))
            normalCellFormat.wrap = true

            for (row in rows.withIndex()) {
                val rowIndex = row.index + 1
                for (cellContent in row.value.withIndex()) {
                    val colIndex = cellContent.index
                    val cell = Label(colIndex, rowIndex, cellContent.value, normalCellFormat)

                    tableSheet.addCell(cell)
                }
            }

            for (colIndex in columns.indices) {
                tableSheet.getColumnView(colIndex).isAutosize = true
            }

            workbook.write()
            workbook.close()
        }
    */
    fun storeJsonToStream(outputStream: OutputStream) {
        val jsonArray = JsonArray(rows.size)
        rows.forEach { row ->
            val rowJson = JsonObject()
            columns.forEachIndexed { index, columnName ->
                rowJson.addProperty(columnName, row[index])
            }
            jsonArray.add(rowJson)
        }

        outputStream.write(GsonBuilder().setPrettyPrinting().create().toJson(jsonArray).toByteArray())
        outputStream.close()
    }
}