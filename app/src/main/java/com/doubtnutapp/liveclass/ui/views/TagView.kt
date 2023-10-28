package com.doubtnutapp.liveclass.ui.views

import android.annotation.TargetApi
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ViewUtils
import com.doubtnutapp.R
import com.doubtnutapp.base.OnFeedbackTagClicked
import com.doubtnutapp.data.remote.models.RatingTagsData
import com.doubtnutapp.findActivity

class TagView : RelativeLayout {
    private var mContext: Context? = null
    private val mTagList: MutableList<RatingTagsData>? = ArrayList()
    private var mTagWidth = 0f
    private var mTagPadding = 0f
    private var mAvailableWidth = 0f
    private var tagTextColorId = 0
    private var tagBackgroundId = 0
    private var taglayoutbackgroundId = 0
    private var tagItemBackgroundColor = 0
    private var actionPerformer: ActionPerformer? = null

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
        val typeArray = mContext!!.obtainStyledAttributes(attrs, R.styleable.TagView)
        tagTextColorId = typeArray.getInt(R.styleable.TagView_tag_color,
                ContextCompat.getColor(mContext!!, R.color.black))
        tagBackgroundId = typeArray.getResourceId(R.styleable.TagView_tag_background, R.drawable.bg_feedback_tags)
        taglayoutbackgroundId = typeArray.getInt(R.styleable.TagView_layout_background,
                ContextCompat.getColor(mContext!!, R.color.color_fcfcfc))
        tagItemBackgroundColor = typeArray.getInt(R.styleable.TagView_tag_item_background,
                ContextCompat.getColor(mContext!!, R.color.color_fcfcfc))
        typeArray.recycle()
    }

    fun addTags(tagList: List<RatingTagsData>?, actionPerformer: ActionPerformer) {
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

            (mContext?.findActivity())?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            return displayMetrics.widthPixels - ViewUtils.dpToPx(60f, mContext).toInt() as Int
        }

    private fun drawMultipleLineTags() {
        if (mContext == null || mTagList == null) {
            return
        }
        removeAllViews()
        var index = 1
        var indexBottom = 1
        var total = 0f
        setBackgroundColor(taglayoutbackgroundId)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        var linearLayout = LinearLayout(mContext)
        linearLayout.gravity = Gravity.CENTER_HORIZONTAL
        linearLayout.setBackgroundColor(ContextCompat.getColor(mContext!!, R.color.color_fcfcfc))
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.id = indexBottom
        layoutParams.addRule(CENTER_HORIZONTAL)
        addView(linearLayout, layoutParams)
        for (tag in mTagList) {
            if (!TextUtils.isEmpty(tag.tag)) {
                var relativeParams: LayoutParams
                val linearParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                val tagLayout = inflateMultipleLineTag(index, tag.tag.orEmpty())
                tagLayout.setOnClickListener {
                    tag.isSelected = !tag.isSelected
                    if (tag.isSelected) {
                        tagLayout.background = ContextCompat.getDrawable(mContext!!, R.drawable.bg_feedback_tags_selected)
                        val tagTv = tagLayout.findViewWithTag<TextView>(tagLayout.id)
                        tagTv?.setTextColor(ContextCompat.getColor(mContext!!, R.color.color_eb532c))
                    } else {
                        tagLayout.background = ContextCompat.getDrawable(mContext!!, tagBackgroundId)
                        val tagTv = tagLayout.findViewWithTag<TextView>(tagLayout.id)
                        tagTv?.setTextColor(ContextCompat.getColor(mContext!!, R.color.black))
                    }
                    actionPerformer?.performAction(OnFeedbackTagClicked(tag.tag.orEmpty(),
                            tag.isTextBoxRequired?.toInt() == 1, tag.isSelected))
                }
                if (mAvailableWidth - (mTagWidth + total + 2 * mTagPadding) >= ViewUtils.dpToPx(6f, mContext).toInt()) {
                    linearParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    linearParams.leftMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                } else {
                    linearParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    relativeParams.topMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams.leftMargin = ViewUtils.dpToPx(6f, mContext).toInt()
                    relativeParams.addRule(BELOW, indexBottom)
                    relativeParams.addRule(CENTER_HORIZONTAL)
                    total = 0f
                    linearLayout = LinearLayout(mContext)
                    linearLayout.setBackgroundColor(ContextCompat.getColor(mContext!!, R.color.color_fcfcfc))
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

    private fun inflateMultipleLineTag(tagId: Int, tagText: String): View {
        val textWidth: Float
        val tagLayout: View = (mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.tag_item, null as ViewGroup?)
        tagLayout.id = tagId
        tagLayout.background = ContextCompat.getDrawable(mContext!!, tagBackgroundId)
        val tagItem = tagLayout.findViewById<TextView>(R.id.tag_item)
        tagItem.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tagItem.textSize = 12f
        tagItem.tag = tagId
        tagItem.setTextColor(tagTextColorId)
        tagItem.setBackgroundColor(tagItemBackgroundColor)
        tagItem.text = tagText
        mTagPadding = tagLayout.paddingLeft.toFloat()
        textWidth = tagItem.paint.measureText(tagText)
        mTagWidth = textWidth + 2 * mTagPadding
        return tagLayout
    }
}