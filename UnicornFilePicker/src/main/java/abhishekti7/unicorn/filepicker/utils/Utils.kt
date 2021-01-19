package abhishekti7.unicorn.filepicker.utils

import java.util.Calendar

/**
 * Created by Abhishek Tiwari on 07-01-2021.
 */
object Utils {
	var mapOfMonths = hashMapOf<Int, String>().apply {
		put(1, "Jan")
		put(2, "Feb")
		put(3, "Mar")
		put(4, "Apr")
		put(5, "May")
		put(6, "Jun")
		put(7, "Jul")
		put(8, "Aug")
		put(9, "Sep")
		put(10, "Oct")
		put(11, "Nov")
		put(12, "Dec")
	}

	@JvmStatic
	fun longToReadableDate(time: Long): String {
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = time
		return mapOfMonths[calendar[Calendar.MONTH] + 1].toString() + " " +
				calendar[Calendar.DAY_OF_MONTH] + ", " + calendar[Calendar.YEAR]
	}
}
