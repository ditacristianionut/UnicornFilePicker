package abhishekti7.unicorn.filepicker.ui

import abhishekti7.unicorn.filepicker.R
import abhishekti7.unicorn.filepicker.R.attr
import abhishekti7.unicorn.filepicker.R.color
import abhishekti7.unicorn.filepicker.R.id
import abhishekti7.unicorn.filepicker.adapters.DirectoryAdapter
import abhishekti7.unicorn.filepicker.adapters.DirectoryAdapter.onFilesClickListener
import abhishekti7.unicorn.filepicker.adapters.DirectoryStackAdapter
import abhishekti7.unicorn.filepicker.databinding.UnicornActivityFilePickerBinding
import abhishekti7.unicorn.filepicker.models.Config
import abhishekti7.unicorn.filepicker.models.DirectoryModel
import abhishekti7.unicorn.filepicker.ui.FilePickerActivity
import abhishekti7.unicorn.filepicker.utils.UnicornSimpleItemDecoration
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * Created by Abhishek Tiwari on 06-01-2021.
 */
class FilePickerActivity : AppCompatActivity() {
	private var filePickerBinding: UnicornActivityFilePickerBinding? = null
	private var root_dir: File? = null
	private var selected_files: ArrayList<String>? = null
	private var arr_dir_stack: ArrayList<DirectoryModel>? = null
	private var arr_files: ArrayList<DirectoryModel>? = null
	private var stackAdapter: DirectoryStackAdapter? = null
	private var directoryAdapter: DirectoryAdapter? = null
	private val REQUIRED_PERMISSIONS = arrayOf(
			"android.permission.WRITE_EXTERNAL_STORAGE",
			"android.permission.READ_EXTERNAL_STORAGE"
	)
	private var config: Config? = null
	private var filters: ArrayList<String>? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		config = Config.getInstance()
		setTheme(config!!.getThemeId())
		filePickerBinding = UnicornActivityFilePickerBinding.inflate(layoutInflater)
		val view: View = filePickerBinding !!.root
		setContentView(view)
		initConfig()
	}

	private fun initConfig() {
		filters = config !!.extensionFilters
		setSupportActionBar(filePickerBinding !!.toolbar)
		supportActionBar !!.setDisplayHomeAsUpEnabled(true)
		supportActionBar !!.setDisplayShowTitleEnabled(false)
		root_dir = if (config !!.rootDir != null) {
			File(config !!.rootDir)
		} else {
			Environment.getExternalStorageDirectory()
		}
		selected_files = ArrayList()
		arr_dir_stack = ArrayList()
		arr_files = ArrayList()
		setUpDirectoryStackView()
		setUpFilesView()
		if (allPermissionsGranted()) {
			fetchDirectory(
					DirectoryModel(
							true,
							root_dir !!.absolutePath,
							root_dir !!.name,
							root_dir !!.lastModified(),
							if (root_dir !!.listFiles() == null) 0 else root_dir !!.listFiles().size
					)
			)
		} else {
			Log.e(TAG, "Storage permissions not granted. You have to implement it before starting the file picker")
			finish()
		}
		filePickerBinding !!.fabSelect.setOnClickListener { v: View? ->
			val intent = Intent()
			if (config !!.showOnlyDirectory()) {
				selected_files !!.clear()
				selected_files !!.add(arr_dir_stack !![arr_dir_stack !!.size - 1].path)
			}
			intent.putStringArrayListExtra("filePaths", selected_files)
			setResult(config !!.reqCode, intent)
			setResult(RESULT_OK, intent)
			finish()
		}
		val typedValue = TypedValue()
		val theme = theme
		theme.resolveAttribute(attr.unicorn_fabColor, typedValue, true)
		if (typedValue.data != 0) {
			filePickerBinding !!.fabSelect.backgroundTintList = ColorStateList.valueOf(typedValue.data)
		} else {
			filePickerBinding !!.fabSelect.backgroundTintList = ColorStateList.valueOf(resources.getColor(color.unicorn_colorAccent))
		}
	}

	private fun setUpFilesView() {
		val layoutManager = LinearLayoutManager(this@FilePickerActivity)
		filePickerBinding !!.rvFiles.layoutManager = layoutManager
		directoryAdapter = DirectoryAdapter(this@FilePickerActivity, arr_files, false, object : onFilesClickListener {
			override fun onClicked(model: DirectoryModel) {
				fetchDirectory(model)
			}

			override fun onFileSelected(fileModel: DirectoryModel) {
				if (config !!.isSelectMultiple) {
					if (selected_files !!.contains(fileModel.path)) {
						selected_files !!.remove(fileModel.path)
					} else {
						selected_files !!.add(fileModel.path)
					}
				} else {
					selected_files !!.clear()
					selected_files !!.add(fileModel.path)
				}
			}
		})
		filePickerBinding !!.rvFiles.adapter = directoryAdapter
		directoryAdapter !!.notifyDataSetChanged()
		if (config !!.addItemDivider()) {
			filePickerBinding !!.rvFiles.addItemDecoration(UnicornSimpleItemDecoration(this@FilePickerActivity))
		}
	}

	private fun setUpDirectoryStackView() {
		val layoutManager = LinearLayoutManager(this@FilePickerActivity, RecyclerView.HORIZONTAL, false)
		filePickerBinding !!.rvDirPath.layoutManager = layoutManager
		stackAdapter = DirectoryStackAdapter(this@FilePickerActivity, arr_dir_stack) { model: DirectoryModel ->
			Log.e(TAG, model.toString())
			arr_dir_stack = ArrayList(arr_dir_stack !!.subList(0, arr_dir_stack !!.indexOf(model) + 1))
			setUpDirectoryStackView()
			fetchDirectory(arr_dir_stack !!.removeAt(arr_dir_stack !!.size - 1))
		}
		filePickerBinding !!.rvDirPath.adapter = stackAdapter
		stackAdapter !!.notifyDataSetChanged()
	}

	/**
	 * Fetches list of files in a folder and filters files if filter present
	 */
	private fun fetchDirectory(model: DirectoryModel) {
		filePickerBinding !!.rlProgress.visibility = View.VISIBLE
		selected_files !!.clear()
		arr_files !!.clear()
		val dir = File(model.path)
		val files_list = dir.listFiles()
		if (files_list != null) {
			for (file in files_list) {
				val directoryModel = DirectoryModel()
				directoryModel.isDirectory = file.isDirectory
				directoryModel.name = file.name
				directoryModel.path = file.absolutePath
				directoryModel.last_modif_time = file.lastModified()
				if (config !!.showHidden() || ! config !!.showHidden() && ! file.isHidden) {
					if (file.isDirectory) {
						if (file.listFiles() != null) directoryModel.num_files = file.listFiles().size
						arr_files !!.add(directoryModel)
					} else {
						if (! config !!.showOnlyDirectory()) {
							// Filter out files if filters specified
							if (filters != null) {
								try {
									// Extract the file extension
									val fileName = file.name
									val extension = fileName.substring(fileName.lastIndexOf("."))
									for (filter in filters !!) {
										if (extension.toLowerCase().contains(filter)) {
											arr_files !!.add(directoryModel)
										}
									}
								} catch (e: Exception) {
//                                Log.e(TAG, "Encountered a file without an extension: ", e);
								}
							} else {
								arr_files !!.add(directoryModel)
							}
						}
					}
				}
			}
			Collections.sort(arr_files, CustomFileComparator())
			arr_dir_stack !!.add(model)
			filePickerBinding !!.rvDirPath.scrollToPosition(arr_dir_stack !!.size - 1)
			filePickerBinding !!.toolbar.title = model.name
		}
		if (arr_files !!.size == 0) {
			filePickerBinding !!.rlNoFiles.visibility = View.VISIBLE
		} else {
			filePickerBinding !!.rlNoFiles.visibility = View.GONE
		}
		filePickerBinding !!.rlProgress.visibility = View.GONE
		stackAdapter !!.notifyDataSetChanged()
		directoryAdapter !!.notifyDataSetChanged()
	}

	// Custom Comparator to sort the list of files in lexicographical order
	class CustomFileComparator : Comparator<DirectoryModel> {
		override fun compare(o1: DirectoryModel, o2: DirectoryModel): Int {
			return if (o1.isDirectory && o2.isDirectory) {
				o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
			} else if (o1.isDirectory && ! o2.isDirectory) {
				- 1
			} else if (! o1.isDirectory && o2.isDirectory) {
				1
			} else {
				o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val menuInflater = menuInflater
		menuInflater.inflate(R.menu.unicorn_menu_file_picker, menu)
		val item_search = menu.findItem(id.action_search)
		val searchView = item_search.actionView as SearchView
		searchView.imeOptions = EditorInfo.IME_ACTION_DONE
		searchView.setOnQueryTextListener(object : OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				directoryAdapter !!.filter.filter(newText)
				return false
			}
		})
		return true
	}

	/**
	 * This method checks whether STORAGE permissions are granted or not
	 */
	private fun allPermissionsGranted(): Boolean {
		for (permission in REQUIRED_PERMISSIONS) {
			if (ContextCompat.checkSelfPermission(this@FilePickerActivity, permission) != PackageManager.PERMISSION_GRANTED) {
				return false
			}
		}
		return true
	}

	override fun onBackPressed() {
		if (arr_dir_stack !!.size > 1) {
			// pop off top value and display
			arr_dir_stack !!.removeAt(arr_dir_stack !!.size - 1)
			val model = arr_dir_stack !!.removeAt(arr_dir_stack !!.size - 1)
			fetchDirectory(model)
		} else {
			// Nothing left in stack so exit
			val intent = Intent()
			setResult(config !!.reqCode, intent)
			setResult(RESULT_CANCELED, intent)
			finish()
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		if (id == android.R.id.home) {
			onBackPressed()
		}
		return true
	}

	companion object {
		private const val TAG = "FilePickerActivity"
	}
}
