package com.doubtnutapp.liveclass.ui.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import java.nio.ByteBuffer

class ScratchTextView : AppCompatTextView {
    interface IRevealListener {
        fun onRevealed(tv: ScratchTextView?)
        fun onRevealPercentChangedListener(stv: ScratchTextView?, percent: Float)
    }

    private var mX = 0f
    private var mY = 0f
    private var shouldReveal = false

    /**
     * Bitmap holding the scratch region.
     */
    private var mScratchBitmap: Bitmap? = null

    /**
     * Drawable canvas area through which the scratchable area is drawn.
     */
    private var mCanvas: Canvas? = null

    /**
     * Path holding the erasing path done by the user.
     */
    private var mErasePath: Path? = null

    /**
     * Path to indicate where the user have touched.
     */
    private var mTouchPath: Path? = null

    /**
     * Paint properties for drawing the scratch area.
     */
    private var mBitmapPaint: Paint? = null

    /**
     * Paint properties for erasing the scratch region.
     */
    private var mErasePaint: Paint? = null

    /**
     * Gradient paint properties that lies as a background for scratch region.
     */
    private var mGradientBgPaint: Paint? = null

    /**
     * Sample Drawable bitmap having the scratch pattern.
     */
    private var mDrawable: BitmapDrawable? = null

    /**
     * Listener object callback reference to send back the callback when the text has been revealed.
     */
    private var mRevealListener: IRevealListener? = null

    /**
     * Reveal percent value.
     */
    private var mRevealPercent = 0f

    /**
     * Thread Count
     */
    private var mThreadCount = 0

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, set: AttributeSet?) : super(context!!, set) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Set the strokes width based on the parameter multiplier.
     *
     * @param multiplier can be 1,2,3 and so on to set the stroke width of the paint.
     */
    fun setStrokeWidth(multiplier: Int) {
        mErasePaint!!.strokeWidth = multiplier * STROKE_WIDTH
    }

    /**
     * Initialises the paint drawing elements.
     */
    private fun init() {
        mTouchPath = Path()
        gravity = Gravity.CENTER
        mErasePaint = Paint()
        mErasePaint!!.isAntiAlias = true
        mErasePaint!!.isDither = true
        mErasePaint!!.color = -0x10000
        mErasePaint!!.style = Paint.Style.STROKE
        mErasePaint!!.strokeJoin = Paint.Join.BEVEL
        mErasePaint!!.strokeCap = Paint.Cap.ROUND
        mErasePaint!!.xfermode = PorterDuffXfermode(
            PorterDuff.Mode.CLEAR
        )
        setStrokeWidth(6)
        mGradientBgPaint = Paint()
        mErasePath = Path()
        mBitmapPaint = Paint(Paint.DITHER_FLAG)
        val scratchBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.background_blur_image)
        mDrawable = BitmapDrawable(resources, scratchBitmap)
        mDrawable!!.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mScratchBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mScratchBitmap!!)
        val rect = Rect(0, 0, mScratchBitmap?.width ?: 0, mScratchBitmap?.height ?: 0)
        mDrawable!!.bounds = rect
        val startGradientColor = ContextCompat.getColor(context, R.color.scratch_start_gradient)
        val endGradientColor = ContextCompat.getColor(context, R.color.scratch_end_gradient)
        mGradientBgPaint!!.shader = LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            startGradientColor,
            endGradientColor,
            Shader.TileMode.MIRROR
        )
        mCanvas?.drawRect(rect, mGradientBgPaint!!)
        mDrawable!!.draw(mCanvas!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mScratchBitmap!!, 0f, 0f, mBitmapPaint)
        canvas.drawPath(mErasePath!!, mErasePaint!!)
    }

    private fun touch_start(x: Float, y: Float) {
        mErasePath!!.reset()
        mErasePath!!.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mErasePath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
            drawPath()
        }
        mTouchPath!!.reset()
        mTouchPath!!.addCircle(mX, mY, 30f, Path.Direction.CW)
    }

    private fun drawPath() {
        mErasePath!!.lineTo(mX, mY)
        // commit the path to our offscreen
        mCanvas!!.drawPath(mErasePath!!, mErasePaint!!)
        // kill this so we don't double draw
        mTouchPath!!.reset()
        mErasePath!!.reset()
        mErasePath!!.moveTo(mX, mY)
        checkRevealed()
    }

    /**
     * Reveals the hidden text by erasing the scratch area.
     */
    fun reveal() {
        isRevealed = true
        val bounds = getTextBounds(1.5f)
        val left = bounds[0]
        val top = bounds[1]
        val right = bounds[2]
        val bottom = bounds[3]
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(
            PorterDuff.Mode.CLEAR
        )
        mCanvas!!.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        checkRevealed()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                drawPath()
                invalidate()
            }
            else -> {
            }
        }
        return true
    }

    val color: Int
        get() = mErasePaint!!.color

    fun setRevealListener(listener: IRevealListener?) {
        mRevealListener = listener
    }

    var isRevealed: Boolean = false
        get() = mRevealPercent == 1f

    private fun checkRevealed() {
        if (!isRevealed && mRevealListener != null) {
            val bounds = textBounds
            val left = bounds[0]
            val top = bounds[1]
            val width = bounds[2] - left
            val height = bounds[3] - top

            // Do not create multiple calls to compare.
            if (mThreadCount > 1) {
                return
            }
            mThreadCount++
            object : AsyncTask<Int?, Void?, Float>() {
                protected override fun doInBackground(vararg params: Int?): Float {
                    return try {
                        val left = params[0] ?: 0
                        val top = params[1] ?: 0
                        val width = params[2] ?: 0
                        val height = params[3] ?: 0
                        val croppedBitmap =
                            Bitmap.createBitmap(mScratchBitmap!!, left, top, width, height)
                        getTransparentPixelPercent(croppedBitmap)
                    } catch (e: Exception) {
                        shouldReveal = true
                        return .05f
                    } finally {
                        mThreadCount--
                    }
                }

                public override fun onPostExecute(percentRevealed: Float) {

                    // check if not revealed before.
                    if (!isRevealed) {
                        val oldValue = mRevealPercent
                        mRevealPercent = percentRevealed
                        if (oldValue != percentRevealed) {
                            mRevealListener!!.onRevealPercentChangedListener(
                                this@ScratchTextView,
                                percentRevealed
                            )
                        }

                        // if now revealed.
                        if (isRevealed || shouldReveal) {
                            mRevealListener!!.onRevealed(this@ScratchTextView)
                        }
                    }
                }
            }.execute(left, top, width, height)
        }
    }

    /**
     * Finds the percentage of pixels that do are empty.
     *
     * @param bitmap input bitmap
     * @return a value between 0.0 to 1.0 . Note the method will return 0.0 if either of bitmaps are null nor of same size.
     */
    fun getTransparentPixelPercent(bitmap: Bitmap?): Float {
        if (bitmap == null) {
            return 0f
        }
        val buffer = ByteBuffer.allocate(bitmap.height * bitmap.rowBytes)
        bitmap.copyPixelsToBuffer(buffer)
        val array = buffer.array()
        val len = array.size
        var count = 0
        for (i in 0 until len) {
            if (array[i] == 0.toByte()) {
                count++
            }
        }
        return count.toFloat() / len
    }

    private val textBounds: IntArray
        private get() = getTextBounds(1f)

    private fun getTextBounds(scale: Float): IntArray {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val vwidth = width
        val vheight = height
        val centerX = vwidth / 2
        val centerY = vheight / 2
        val paint = paint
        val text = text.toString()
        val dimens = getTextDimens(text, paint)
        var width = dimens[0]
        var height = dimens[1]
        val lines = lineCount
        height = height * lines
        width = width / lines
        var left = 0
        var top = 0
        height = if (height > vheight) {
            vheight - (paddingBottom + paddingTop)
        } else {
            (height * scale).toInt()
        }
        width = if (width > vwidth) {
            vwidth - (paddingLeft + paddingRight)
        } else {
            (width * scale).toInt()
        }
        val gravity = gravity

        if (gravity and Gravity.START == Gravity.START) {
            left = paddingLeft
        } else if (gravity and Gravity.END == Gravity.END) {
            left = vwidth - paddingRight - width
        } else if (gravity and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL) {
            left = centerX - width / 2
        }
        if (gravity and Gravity.TOP == Gravity.TOP) {
            top = paddingTop
        } else if (gravity and Gravity.BOTTOM == Gravity.BOTTOM) {
            top = vheight - paddingBottom - height
        } else if (gravity and Gravity.CENTER_VERTICAL == Gravity.CENTER_VERTICAL) {
            top = centerY - height / 2
        }

        return intArrayOf(
            left - 120,
            top - 50,
            left + width + 120,
            top + height + 30
        )
    }

    companion object {
        const val STROKE_WIDTH = 12f
        private const val TOUCH_TOLERANCE = 4f
        private fun getTextDimens(text: String, paint: Paint): IntArray {
            val end = text.length
            val bounds = Rect()
            paint.getTextBounds(text, 0, end, bounds)
            val width = bounds.left + bounds.width()
            val height = bounds.bottom + bounds.height()
            return intArrayOf(width, height)
        }
    }
}