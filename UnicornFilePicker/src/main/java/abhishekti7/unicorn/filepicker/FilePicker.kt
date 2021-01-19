package abhishekti7.unicorn.filepicker

import abhishekti7.unicorn.filepicker.models.Config
import abhishekti7.unicorn.filepicker.ui.FilePickerActivity
import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class FilePicker private constructor() {

	private lateinit var _activity: WeakReference<Activity>
	private lateinit var _context: WeakReference<Fragment>

	private constructor(activity: Activity): this() {
		_activity = WeakReference(activity)
		_context = WeakReference(null)
	}
	private constructor(fragment: Fragment): this() {
		_activity = WeakReference(null)
		_context = WeakReference(fragment)
	}
	private constructor(activity: Activity, fragment: Fragment): this() {
		_activity = WeakReference(activity)
		_context = WeakReference(fragment)
	}


	/**
	 * Start FilePicker activity and wait for result
	 * @param requestCode Integer identity for Activity or Fragment request
	 */
	fun forResult(requestCode: Int) {
		Config.getInstance().reqCode = requestCode
		val activity = _activity.get()
		activity ?: return
		val intent = Intent(activity, FilePickerActivity::class.java)
		val fragment = _context.get()
		if (fragment == null) {
			activity.startActivityForResult(intent, requestCode)
		} else {
			fragment.startActivityForResult(intent, requestCode)
		}
	}

	fun addConfigBuilder() = ConfigBuilder(this)


	companion object {
		/**
		 * Start UnicornFilePicker from an activity
		 *
		 * @param activity Activity instance
		 * @return UnicornFilePicker instance
		 */
		fun from(activity: Activity): FilePicker {
			return FilePicker(activity)
		}

		/**
		 * Start UnicornFilePicker from a fragment
		 *
		 * @param fragment Fragment instance
		 * @return UnicornFilePicker instance
		 */
		fun from(fragment: Fragment): FilePicker {
			return FilePicker(fragment)
		}
	}

}
