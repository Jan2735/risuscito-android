package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.simple_sub_item.view.*

fun simpleSubItem(block: SimpleSubItem.() -> Unit): SimpleSubItem = SimpleSubItem().apply(block)

class SimpleSubItem : AbstractExpandableItem<SimpleSubItem.ViewHolder>(), IExpandable<SimpleSubItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }

    var page: StringHolder? = null
        private set
    var setPage: Any? = null
        set(value) {
            page = helperSetString(value)
        }

    var source: StringHolder? = null
        private set
    var setSource: Any? = null
        set(value) {
            source = helperSetString(value)
        }

    var color: ColorHolder? = null
        private set
    var setColor: Any? = null
        set(value) {
            color = helperSetColor(value)
        }

    var id: Int = 0

    var isHasDivider = false

    override val type: Int
        get() = R.id.fastadapter_sub_item_id

    override val layoutRes: Int
        get() = R.layout.simple_sub_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(private var view: View) : FastAdapter.ViewHolder<SimpleSubItem>(view) {

        private var mTitle: TextView? = null
        private var mPage: TextView? = null
        private var mPageSelected: View? = null
        private var mId: TextView? = null
        private var mItemDivider: View? = null

        override fun bindView(item: SimpleSubItem, payloads: List<Any>) {
            val ctx = itemView.context

            StringHolder.applyTo(item.title, mTitle)
            StringHolder.applyToOrHide(item.page, mPage)
            view.background = FastAdapterUIUtils.getSelectableBackground(
                    ctx,
                    ctx.themeColor(R.attr.colorSecondaryLight),
                    true)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)
            mPage?.isVisible = true
            mPageSelected?.isVisible = false

            mId?.text = item.id.toString()

            mItemDivider?.isVisible = item.isHasDivider
        }

        override fun unbindView(item: SimpleSubItem) {
            mTitle?.text = null
            mPage?.text = null
            mId?.text = null
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mId = view.text_id_canto
            mItemDivider = view.item_divider
        }
    }
}
