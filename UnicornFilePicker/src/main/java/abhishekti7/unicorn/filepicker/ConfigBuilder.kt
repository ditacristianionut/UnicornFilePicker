package abhishekti7.unicorn.filepicker

import abhishekti7.unicorn.filepicker.R.style
import abhishekti7.unicorn.filepicker.models.Config
import androidx.annotation.StyleRes
import java.util.ArrayList
import java.util.Arrays

/**
 * Created by Abhishek Tiwari on 09-01-2021.
 */
class ConfigBuilder(private val filePicker: FilePicker) {
	private var rootDir: String? = null
	private var showHidden = false
	private var selectMultiple = false
	private var addDivider = false
	private var showOnlyDir = false

	@StyleRes private var themeId = style.UnicornFilePicker_Default
	private var extensionFilters: ArrayList<String>? = null
	private val config: Config = Config.getCleanInstance()


	fun setRootDirectory(dirPath: String?): ConfigBuilder {
		rootDir = dirPath
		return this
	}

	fun showHiddenFiles(value: Boolean): ConfigBuilder {
		showHidden = value
		return this
	}

	fun selectMultipleFiles(value: Boolean): ConfigBuilder {
		selectMultiple = value
		return this
	}

	fun setFilters(filters: Array<String?>): ConfigBuilder {
		extensionFilters = ArrayList(Arrays.asList(*filters))
		return this
	}

	fun addItemDivider(value: Boolean): ConfigBuilder {
		addDivider = value
		return this
	}

	fun theme(@StyleRes theme: Int): ConfigBuilder {
		themeId = theme
		return this
	}

	fun showOnlyDirectory(value: Boolean): ConfigBuilder {
		showOnlyDir = value
		return this
	}

	fun build(): FilePicker {
		config.rootDir = rootDir
		config.isSelectMultiple = selectMultiple
		config.setShowHidden(showHidden)
		config.extensionFilters = extensionFilters
		config.setAddItemDivider(addDivider)
		config.themeId = themeId
		config.setShowOnlyDirectory(showOnlyDir)
		return filePicker
	}

}
