package com.doubtnutapp.liveclass.ui.views

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnutapp.R
import com.doubtnutapp.base.OnNotesFilterClicked
import com.doubtnutapp.course.widgets.NotesFilterItem

class FilterTagView : RelativeLayout {

    companion object {
        private const val TAG = "FilterTagView"
    }

    private var mContext: Context? = null
    private val mTagList: MutableList<NotesFilterItem>? = ArrayList()
    private var mTagWidth = 0f
    private var mTagPadding = 0f
    private var mAvailableWidth = 0f
    private var tagTextColorId = 0
    private var tagBackgroundId = 0
    private var taglayoutbackgroundId = 0
    private var tagItemBackgroundColor = 0
    private var actionPerformer: ActionPerformer? = null
    private var isMultiSelect: Boolean = false

    constructor(context: Context?) : super(context) {
        mContext = context
        initialize(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initialize(attrs)
    }

    @TargetApi(21)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mContext = context
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val typeArray = mContext!!.obtainStyledAttributes(attrs, R.styleable.FilterTagView)
        tagTextColorId = typeArray.getInt(R.styleable.FilterTagView_filter_tag_color,
                ContextCompat.getColor(mContext!!, R.color.black))
        tagBackgroundId = typeArray.getResourceId(R.styleable.FilterTagView_filter_tag_background, R.drawable.bg_filter_tags)
        taglayoutbackgroundId = typeArray.getInt(R.styleable.FilterTagView_filter_layout_background,
                ContextCompat.getColor(mContext!!, R.color.blue_e0eaff))
        tagItemBackgroundColor = typeArray.getInt(R.styleable.FilterTagView_filter_tag_item_background,
                ContextCompat.getColor(mContext!!, R.color.color_fcfcfc))
        isMultiSelect = typeArray.getBoolean(R.styleable.FilterTagView_filter_tag_multi_select, false)
        typeArray.recycle()
    }

    fun addTags(tagList: List<NotesFilterItem>?, actionPerformer: ActionPerformer) {
        if (tagList == null || tagList.isEmpty()) {
            return
        }

        this.actionPerformer = actionPerformer
        mTagList!!.clear()
        for (tag in tagList) {
            mTagList.add(tag)
        }
        mAvailableWidth = screenWidth.toFloat()
        if (mAvailableWidth > 0) {
            drawMultipleLineTags()
        }
    }

    val screenWidth: Int
        get() {
            val displayMetrics = DisplayMetrics()
            (mContext as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels - ViewUtils.dpToPx(15f, mContext).toInt() as Int
        }

    private fun drawMultipleLineTags() {
        if (mContext == null || mTagList == null) {
            return
        }
        removeAllViews()
        var index = 1
        var indexBottom = 100
        var total = 0f
        setBackgroundColor(taglayoutbackgroundId)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        var linearLayout = LinearLayout(mContext)
        linearLayout.gravity = Gravity.START
        linearLayout.setBackgroundColor(taglayoutbackgroundId)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = indexBottom
        layoutParams.addRule(ALIGN_START)
        layoutParams.bottomMargin = ViewUtils.dpToPx(10f, mContext).toInt()
        addView(linearLayout, layoutParams)
        for (tag in mTagList) {
            if (!TextUtils.isEmpty(tag.text)) {
                var relativeParams: LayoutParams
                val linearParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                val tagLayout = inflateMultipleLineTag(index, tag.text.orEmpty(), tag.isSelected)

                tagLayout.setOnClickListener {
                    if (isMultiSelect) {
                        tagLayout.background = ContextCompat.getDrawable(mContext!!, R.drawable.bg_multiselect_filter_tags_selected)
                        val tagTv = tagLayout.findViewWithTag<TextView>(tag.text)
                        tagTv?.setTextColor(ContextCompat.getColor(mContext!!, R.color.color_eb532c))
                    } else {
                        tag.isSelected = !tag.isSelected
                        if (tag.isSelected) {
                            mTagList.forEachIndexed { index, it ->
                                if (it.text != tag.text) {
                                    it.isSelected = false
                                    val linearlayoutTag = findViewById<View>(index + 1)
                                    linearlayoutTag.background = ContextCompat.getDrawable(mContext!!, tagBackgroundId)
                                    val tagTv: TextView = (linearlayoutTag as ViewGroup).getChildAt(0) as TextView
                                    tagTv.setTextColor(ContextCompat.getColor(mContext!!, R.color.black))
                                } else {
                                    tagLayout.background = ContextCompat.getDrawable(mContext!!, R.drawable.bg_filter_tags_selected)
                                    val tagTv = tagLayout.findViewWithTag<TextView>(it.text)
                                    tagTv?.setTextColor(ContextCompat.getColor(mContext!!, R.color.color_eb532c))
                                }
                            }
                        } else {
                            tagLayout.background = ContextCompat.getDrawable(mContext!!, tagBackgroundId)
                            val tagTv: TextView = (tagLayout as ViewGroup).getChildAt(0) as TextView
                            tagTv.setTextColor(ContextCompat.getColor(mContext!!, R.color.black))
                        }
                    }
                    actionPerformer?.performAction(OnNotesFilterClicked(id = tag.id, isSelected = tag.isSelected, isMultiSelect = isMultiSelect))
                }

                if (mAvailableWidth - (mTagWidth + total + 2 * mTagPadding) >= ViewUtils.dpToPx(6f, mContext).toInt()) {
                    linearParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    linearParams.leftMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                } else {
                    linearParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    relativeParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams.bottomMargin = ViewUtils.dpToPx(10f, mContext).toInt()
                    relativeParams.leftMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams.addRule(BELOW, indexBottom)
                    relativeParams.addRule(ALIGN_START)
                    total = 0f
                    linearLayout = LinearLayout(mContext)
                    linearLayout.setBackgroundColor(taglayoutbackgroundId)
                    linearLayout.orientation = LinearLayout.HORIZONTAL
                    linearLayout.id = indexBottom + 1
                    addView(linearLayout, relativeParams)
                    indexBottom++
                }
                linearLayout.addView(tagLayout, linearParams)
                index++
                total += mTagWidth + ViewUtils.dpToPx(12f, mContext).toInt()
            }
        }
    }

    private fun inflateMultipleLineTag(tagId: Int, tagText: String, isSelected: Boolean): View {
        val textWidth: Float
        val tagLayout: View = (mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.multi_tag_item, null as ViewGroup?)
        tagLayout.id = tagId
        val tagItem = tagLayout.findViewById<TextView>(R.id.multi_tag_item)
        val tagImg = tagLayout.findViewById<ImageView>(R.id.imgDropDown)

        tagItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tagItem.textSize = 12f
        tagItem.tag = tagText
        if (isMultiSelect) {
            tagImg.visibility = View.VISIBLE
            tagImg?.setImageResource(R.drawable.ic_drop_down)
        }
        if (isSelected) {
            val background = if(isMultiSelect) R.drawable.bg_multiselect_filter_tags_selected else R.drawable.bg_multiselect_filter_tags_selected
            tagItem.setTextColor(ContextCompat.getColor(mContext!!, R.color.color_eb532c))
            tagLayout.background = ContextCompat.getDrawable(mContext!!, R.drawable.bg_multiselect_filter_tags_selected)
        } else {
            tagItem.setTextColor(tagTextColorId)
            tagLayout.background = ContextCompat.getDrawable(mContext!!, tagBackgroundId)
        }

        tagItem.text = tagText
        mTagPadding = tagLayout.paddingLeft.toFloat()
        textWidth = tagItem.paint.measureText(tagText)
        mTagWidth = textWidth + 2 * mTagPadding
        return tagLayout
    }
}