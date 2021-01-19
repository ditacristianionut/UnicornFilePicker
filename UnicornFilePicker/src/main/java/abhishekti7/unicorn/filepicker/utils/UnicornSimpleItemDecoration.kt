package abhishekti7.unicorn.filepicker.utils

import abhishekti7.unicorn.filepicker.R.drawable
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.State

/**
 * Created by Abhishek Tiwari on 09-01-2021.
 */
class UnicornSimpleItemDecoration(context: Context?) : ItemDecoration() {
	private val mDivider: Drawable?
	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: State) {
		val left = parent.paddingLeft
		val right = parent.width - parent.paddingRight
		val childCount = parent.childCount
		for (i in 0 until childCount) {
			val child = parent.getChildAt(i)
			val params = child.layoutParams as LayoutParams
			val top = child.bottom + params.bottomMargin
			val bottom = top + mDivider !!.intrinsicHeight
			mDivider.setBounds(left, top, right, bottom)
			mDivider.draw(c)
		}
	}

	init {
		mDivider = ContextCompat.getDrawable(context !!, drawable.unicorn_item_layout_divider)
	}
}
