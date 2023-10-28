@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.doubtnutapp

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.*
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.webkit.WebView
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.ParcelCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.analytics.addEventNames2
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.Utils.not
import com.doubtnutapp.data.remote.models.ActiveFeedback
import com.doubtnutapp.data.remote.models.QuestionMeta
import com.doubtnutapp.model.AppActiveFeedback
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.model.ForumQueue
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.mathview.MathViewSimilar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.roundToInt

class doAsyncPost(val handler: () -> Unit, val postHandler: () -> Unit) :
    AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }

    override fun onPostExecute(result: Void?) {
        Log.d("onPostExecute", "onPostExecute")
        postHandler()

    }
}

class doAsyncPostWithResult<T>(val handler: () -> T?, val postHandler: (bitmap: T?) -> Unit) :
    AsyncTask<Void, Void, T>() {
    override fun doInBackground(vararg params: Void?): T? {
        return handler()
    }

    override fun onPostExecute(result: T?) {
        postHandler(result)
    }
}

fun RecyclerView.addOnItemClick(listener: RecyclerItemClickListener.OnClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, listener, null))
}

fun RecyclerView.clearAllItemClickListener() {
    this.clearOnChildAttachStateChangeListeners()
}

fun RecyclerView.addOnLongItemClick(listener: RecyclerItemClickListener.OnLongClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, null, listener))
}

@SuppressLint("ClickableViewAccessibility")
inline fun WebView.addOnWebViewClickListener(crossinline listener: (v: View) -> Unit) {
    this.setOnTouchListener { v, e ->
        if (e.action == MotionEvent.ACTION_UP) {
            listener(v)
        }
        true

    }
}

fun RecyclerView.addReadyCallback(listener: () -> Unit) {

    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            listener()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun Fragment.navigateTo(fragment: Fragment, tag: String) {
    activity?.let {

        val manager = it.supportFragmentManager
        val ft = manager.beginTransaction()

        ft.replace(R.id.container, fragment, tag)

        if (ft.isAddToBackStackAllowed) {
            ft.addToBackStack(null)
        }

        ft.commit()

    }

}

fun AppCompatActivity.replaceFragment(fragment: Fragment, tag: String) {
    val manager = supportFragmentManager
    val ft = manager.beginTransaction()
    ft.replace(R.id.container, fragment, tag)
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    ft.commit()
}

fun HashMap<String, Any>.toRequestBody(): RequestBody {
    return JSONObject(this as Map<*, *>).toString()
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
}

fun defaultPrefs(context: Context = DoubtnutApp.INSTANCE.applicationContext) =
    PreferenceManager.getDefaultSharedPreferences(context)!!

fun authToken(context: Context): String =
    defaultPrefs(context).getString(Constants.XAUTH_HEADER_TOKEN, "").orDefaultValue()

fun Fragment.apiErrorToast(e: Throwable, duration: Int = Toast.LENGTH_LONG) {
    if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
        activity?.let {
            ToastUtils.makeText(it, e.message ?: "Api error.. please try later", duration).show()
        }
    } else {
        toast(R.string.api_error, duration)
    }
}

fun AppCompatActivity.apiErrorToast(e: Throwable, duration: Int = Toast.LENGTH_LONG) {
    if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
        ToastUtils.makeText(this, e?.message ?: "Api error.. please try later", duration).show()
    } else {
        ToastUtils.makeText(this, R.string.api_error, duration).show()
    }
}

fun getQuestionImage(id: String) =
    "https://doubtnutvideobiz.blob.core.windows.net/q-thumbnail/" + id + ".png"

@Deprecated("Use setStatusBarColor from ActivityUtils")
fun statusbarColor(activity: Activity?, color: Int) {
    activity.setStatusBarColor(color)
}

fun windowBackground(activity: Activity?, background: Int) {
    if (activity != null && Build.VERSION.SDK_INT >= 21) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
        window.setBackgroundDrawableResource(background)
    }
}

/**
 * marginRight and marginLeft are deprecated.
 * supply only the values of marginStart and marginEnd.
 */
fun View.updateMargins(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom,
    start: Int = marginStart,
    end: Int = marginEnd,
) = this.updateMargins2(left, top, right, bottom, start, end)

fun ClosedRange<Int>.random() =
    Random().nextInt(endInclusive - start) + start

fun ArrayList<QuestionMeta>.toForumQuestions(): ArrayList<ForumQueue> {
    val queue: ArrayList<ForumQueue> = arrayListOf()

    for (question in this) {
        queue.add(ForumQueue(question.chapter, question.subtopic, question.upvote_count))
    }
    return queue
}

fun ArrayList<ActiveFeedback>.toDBAppActiveFeedback(): ArrayList<AppActiveFeedback> {
    val feedbacks: ArrayList<AppActiveFeedback> = arrayListOf()

    for (feedback in this) {
        feedbacks.add(
            AppActiveFeedback(
                feedback.type,
                feedback.title,
                feedback.question,
                feedback.options,
                feedback.submit,
                feedback.count,
                feedback.id,
                "inactive"
            )
        )

    }
    return feedbacks
}

fun ArrayList<com.doubtnutapp.data.remote.models.AppEvents>.toDBAppInappFeedback(): ArrayList<AppEvent> {
    val inappNotifications: ArrayList<AppEvent> = arrayListOf()
    var count: Long = 0
    for (inapps in this) {
        inappNotifications.add(
            AppEvent(
                count,
                inapps.event,
                inapps.status,
                inapps.title,
                inapps.message,
                inapps.image,
                inapps.button_text,
                inapps.data,
                inapps.sub_title,
                inapps.trigger,
                inapps.deeplink_url
            )
        )
        ++count
    }
    return inappNotifications
}

fun Uri.isValid(): Boolean {
    return this != Uri.EMPTY
}

fun View.hide() {
    if (this.visibility not View.GONE)
        this.visibility = View.GONE
}

fun View.show() {
    if (this.visibility not View.VISIBLE)
        this.visibility = View.VISIBLE
}

fun View.invisible() {
    if (this.visibility not View.INVISIBLE)
        this.visibility = View.INVISIBLE
}

fun View.setVisibleState(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setVisibleInvisibleState(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun View.toggleVisibility() {
    isVisible = isNotVisible
}

fun TextView.toggleVisibilityAndSetText(text: String?) {
    this.visibility = if (!text.isNullOrEmpty()) View.VISIBLE else View.GONE
    if (text != null) setText(text)
}

fun Context?.isDeviceOrientationOn(): Boolean {
    if (this == null) {
        return false
    } else {
        return android.provider.Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }
}

fun Int.inValid() = this < 0

fun Tracker.addEventNames(eventNameFb: String, eventNameBr: String = eventNameFb): Tracker =
    this.addEventNames2(eventNameFb, eventNameBr) as Tracker

operator fun String.minus(suffix: String) = removeSuffix(".$suffix")

fun String.ifEmptyThenNull(str: String): String? {
    return if (str.isEmpty())
        null
    else
        str
}

fun String?.ifEmptyThenNull(): String? {
    return if (this.isNullOrBlank())
        null
    else
        this
}

fun Long.toSeconds() = this * 1000

fun Long.millisToMinutes() = (this / (60f * 1000)).roundToInt()

fun ImageView.loadImageEtx(
    url: String?,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
    onLoadFailed: ((Boolean) -> Unit)? = null,
    onResourceReady: ((Boolean) -> Unit)? = null
) {
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(diskCacheStrategy)
        .format(format)
        .addListener(object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?, model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean,
            ): Boolean {
                onLoadFailed?.invoke(true)
                return true
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?, isFirstResource: Boolean,
            ): Boolean {
                onResourceReady?.invoke(true)
                return false
            }
        })
        .into(this)
}

fun ImageView.loadImageFromCache(
    url: String?,
    @DrawableRes placeholder: Int,
    onLoadFailed: ((Boolean) -> Unit)? = null,
    onResourceReady: ((Boolean) -> Unit)? = null
) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholder)
        .onlyRetrieveFromCache(true)
        .addListener(object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?, model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean,
            ): Boolean {
                onLoadFailed?.invoke(true)
                return true
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?, isFirstResource: Boolean,
            ): Boolean {
                onResourceReady?.invoke(true)
                return false
            }
        }).into(this)
}

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes errorDrawable: Int? = null,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
) = this.loadImage2(
    url, placeholder, errorDrawable, diskCacheStrategy, format
)

fun ImageView.load(
    @DrawableRes resId: Int,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
) = this.load2(
    resId, diskCacheStrategy, format
)

fun View.loadBackgroundImage(
    url: String?,
    @DrawableRes placeholder: Int,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
) {
    Glide.with(context)
        .asDrawable()
        .placeholder(placeholder)
        .load(url)
        .diskCacheStrategy(diskCacheStrategy)
        .format(format)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                background = placeholder
            }
        })
}

fun Map<String, Any?>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {

        val key = it.key

        val value = it.value

        if (value == null) putString(key, null)

        when (value) {
            // Scalars
            is Boolean -> putBoolean(key, value)
            is Byte -> putByte(key, value)
            is Char -> putChar(key, value)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Short -> putShort(key, value)

            // References
            is Bundle -> putBundle(key, value)
            is CharSequence -> putCharSequence(key, value)
            is Parcelable -> putParcelable(key, value)

            // Scalar arrays
            is BooleanArray -> putBooleanArray(key, value)
            is ByteArray -> putByteArray(key, value)
            is CharArray -> putCharArray(key, value)
            is DoubleArray -> putDoubleArray(key, value)
            is FloatArray -> putFloatArray(key, value)
            is IntArray -> putIntArray(key, value)
            is LongArray -> putLongArray(key, value)
            is ShortArray -> putShortArray(key, value)

            // Reference arrays
            is Array<*> -> {
                val componentType = value::class.java.componentType!!
                @Suppress("UNCHECKED_CAST") // Checked by reflection.
                when {
                    Parcelable::class.java.isAssignableFrom(componentType) -> {
                        putParcelableArray(key, value as Array<Parcelable>)
                    }
                    String::class.java.isAssignableFrom(componentType) -> {
                        putStringArray(key, value as Array<String>)
                    }
                    CharSequence::class.java.isAssignableFrom(componentType) -> {
                        putCharSequenceArray(key, value as Array<CharSequence>)
                    }
                    Serializable::class.java.isAssignableFrom(componentType) -> {
                        putSerializable(key, value)
                    }
                    else -> {
                        val valueType = componentType.canonicalName
                        throw IllegalArgumentException(
                            "Illegal value array type $valueType for key \"$key\""
                        )
                    }
                }
            }

            // Last resort. Also we must check this after Array<*> as all arrays are serializable.
            is Serializable -> putSerializable(key, value)

            else -> {
                if (Build.VERSION.SDK_INT >= 18 && value is Binder) {
                    putBinder(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is Size) {
                    putSize(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is SizeF) {
                    putSizeF(key, value)
                } else {

                    val valueType = value?.javaClass?.canonicalName
                    throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
                }
            }
        }
    }
}

operator fun CompositeDisposable.plus(disposable: Disposable) {
    this.add(disposable)
}

fun <X, Y> LiveData<X>.map(body: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, body)
}

fun View.isRtl() = layoutDirection == View.LAYOUT_DIRECTION_RTL

/**
 * Linearly interpolate between two values.
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

private fun createStateForView(view: View) = ViewPaddingState(
    view.paddingLeft,
    view.paddingTop, view.paddingRight, view.paddingBottom, view.paddingStart, view.paddingEnd
)

data class ViewPaddingState(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val start: Int,
    val end: Int
)

/** Write a boolean to a Parcel. */
fun Parcel.writeBooleanUsingCompat(value: Boolean) = ParcelCompat.writeBoolean(this, value)

/** Read a boolean from a Parcel. */
fun Parcel.readBooleanUsingCompat() = ParcelCompat.readBoolean(this)

fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, ViewPaddingState) -> Unit) {
    // Create a snapshot of the view's padding state
    val paddingState = createStateForView(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, paddingState)
        insets
    }
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

fun String?.orDefaultValue(defaultValue: String = "") = this ?: defaultValue

fun String?.getAsViewsCountString(): String? {
    var stringText: String? = null
    this?.toIntOrNull()?.let {
        if (it > 1000000000) {
            stringText = "1B"
        } else if (it > 100000000) {
            stringText = "100M"
        } else if (it > 10000000) {
            stringText = "10M"
        } else if (it > 1000000) {
            stringText = "1M"
        } else if (it > 100000) {
            stringText = "100K"
        } else if (it > 10000) {
            stringText = "10K"
        } else if (it > 1000) {
            stringText = "1K"
        } else {
            stringText = it.toString()
        }
    }
    return stringText
}

fun userProfileImage(context: Context) = defaultPrefs(context).getString("image_url", "") ?: ""

fun Int.toBoolean(): Boolean {
    return this == 1
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

val View?.isOnScreen: Boolean
    get() {
        if (this?.isShown != true) return false
        val actualPosition = Rect().also { getGlobalVisibleRect(it) }
        val screen = Resources.getSystem().displayMetrics.run {
            Rect(0, 0, widthPixels, heightPixels)
        }
        return actualPosition.intersect(screen)
    }

fun EditText.enable() {
    this.isEnabled = true
    this.isFocusable = true
    this.isFocusableInTouchMode = true
    this.requestFocus()
}

fun EditText.disable() {
    isFocusable = false
    isEnabled = false
    isCursorVisible = false
    keyListener = null
}

fun Button.enable() {
    isEnabled = true
    background.clearColorFilter()
}

fun Button.disable() {
    isEnabled = false
    background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
        Color.GRAY,
        BlendModeCompat.SRC_IN
    )
}

inline var Button.isDisabled: Boolean
    get() = isEnabled.not()
    set(value) {
        if (value) disable() else enable()
    }

fun Bitmap.toBase64String(): String = BitmapUtils.getEncodedImage(this)

fun RecyclerView.getVisibleItemsCount(): Int {
    val layoutManager = layoutManager as LinearLayoutManager?
    val firstVisibleItem = layoutManager?.findFirstVisibleItemPosition() ?: 0
    val lastVisibleItem = layoutManager?.findLastVisibleItemPosition() ?: 0
    return (lastVisibleItem - firstVisibleItem) + 1
}

inline fun <reified T> T?.forceUnWrap(): T = (this as T)!!

fun Activity.getScreenWidth() = this.getScreenWidth2()

fun Activity.getScreenHeight() = this.getScreenHeight2()

fun FragmentActivity.getCurrentFragment(): Fragment? =
    when (val fragmentCount = supportFragmentManager.backStackEntryCount) {
        0 -> null
        else -> {
            val fragmentTag =
                this.supportFragmentManager.getBackStackEntryAt(fragmentCount - 1).name
            supportFragmentManager.findFragmentByTag(fragmentTag)
        }
    }

fun Activity.showSnackbar(
    message: Int, actionText: Int, duration: Int, id: String = "",
    action: ((idToPost: String) -> Unit)? = null
) {
    showSnackbar(getString(message), getString(actionText), duration, id, action)
}

fun Activity.showSnackbar(
    message: String, actionText: String, duration: Int, id: String = "",
    action: ((idToPost: String) -> Unit)? = null
) {
    Snackbar.make(
        this.findViewById(android.R.id.content),
        message,
        duration
    ).apply {
        setAction(actionText) {
            action?.invoke(id)
        }
        view.background = AppCompatResources.getDrawable(context, R.drawable.bg_capsule_dark_blue)
        setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
        val textView =
            this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        show()
    }
}

fun Fragment.showSnackBarMessage(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    activity?.let {
        Snackbar.make(
            it.findViewById(android.R.id.content),
            message,
            duration
        ).apply {
            this.view.background = context.getDrawable(R.drawable.bg_capsule_dark_blue)
            setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
            val textView =
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            show()
        }
    }
}

fun Fragment.showSnackBarMessage(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    showSnackBarMessage(getString(resId), duration)
}

fun AssetManager.readAssetsFile(fileName: String): String =
    open(fileName).bufferedReader().use { it.readText() }

inline val View.isNotVisible: Boolean
    get() = isVisible.not()

/**
 * Compatibility extension to check for PIP mode with build version check
 */
inline val Activity.isInPipMode: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        isInPictureInPictureMode
    } else {
        false
    }

fun Context.bringLauncherTaskToFront() {
    val activityManager: ActivityManager =
        getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return
    activityManager.appTasks.forEach {
        val baseIntent = it?.taskInfo?.baseIntent
        val categories = baseIntent?.categories
        if (categories != null && (Intent.CATEGORY_LAUNCHER in categories)) {
            it.moveToFront()
            return@forEach
        }
    }
}

fun Context.deviceSupportsPipMode(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

tailrec fun Context.findActivity(): Activity {
    if (this is Activity) return this

    val parent = (this as? ContextWrapper)?.baseContext
        ?: throw IllegalArgumentException("No activity found")

    return parent.findActivity()
}

fun ActivityManager.RecentTaskInfo.getId(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        taskId
    } else {
        id
    }

fun Float.ceil(): Int = toDouble().ceil()

fun Double.ceil(): Int = kotlin.math.ceil(this).toInt()

fun TabLayout.addOnTabSelectedListener(onTabSelected: (TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null) {
                onTabSelected(tab)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
}

// Disable user dragging thumb
fun SeekBar.disableSeek() {
    setOnTouchListener { _, _ -> true }
}

/**
 * Removes all recyclerview item decorations
 */
fun RecyclerView.removeItemDecorations() {
    while (this.itemDecorationCount > 0) {
        this.removeItemDecorationAt(0)
    }
}

fun ImageView.loadImageOrMathView(
    url: String,
    ocrViewId: MathViewSimilar? = null,
    ocrText: String? = null,
    placeholderId: Int? = null,
    diskCacheStrategy: DiskCacheStrategy = CoreRemoteConfigUtils.getDiskCacheStrategy(),
    format: DecodeFormat = CoreRemoteConfigUtils.getDecodeFormat(),
) {
    var isPlaceHolderShown = false
    val glideVal = Glide.with(this.context).load(url)
        .addListener(object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?, model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                if (ocrViewId != null) {
                    isPlaceHolderShown = false
                    ocrViewId.isVisible = true
                    ocrViewId.text = ocrText.orEmpty()
                } else {
                    isPlaceHolderShown = true
                }
                return true
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?, isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
    if (placeholderId != null && isPlaceHolderShown) {
        glideVal.placeholder(placeholderId)
            .diskCacheStrategy(diskCacheStrategy)
            .format(format)
            .into(this)
    } else {
        glideVal
            .diskCacheStrategy(diskCacheStrategy)
            .format(format)
            .into(this)
    }
}

fun Bitmap.toGrayscale(): Bitmap? {
    try {
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(this, 0f, 0f, paint)
        return bmpGrayscale
    } catch (e: Exception) {
        return null
    }
}

fun AppBarLayout.onTopReachedListener(): Observable<Boolean> {
    val reachedTop = PublishSubject.create<Boolean>()
    addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        reachedTop.onNext(
            verticalOffset == 0
        )
    })
    return reachedTop
}

fun AppBarLayout.onTopReachedAfterScrollDownListener(): Observable<Boolean> {
    val reachedTop = PublishSubject.create<Boolean>()
    addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
        var isScrollDown = false
        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
            if (verticalOffset <= -50) {
                isScrollDown = true
            }
            if (isScrollDown) {
                reachedTop.onNext(verticalOffset == 0)
            }
        }

    })
    return reachedTop
}

fun LottieAnimationView.addAnimatorEndListener(action: (Animator) -> (Unit)) {
    addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            animation?.let { action(animation) }
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    })
}

fun Animation.addAnimationEndListener(action: (Animation) -> (Unit)) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            animation?.let { action(animation) }
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    })
}

inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun BottomNavigationView.uncheckAllItems() {
    menu.apply {
        setGroupCheckable(0, true, false)
        forEach {
            it.isChecked = false
        }
        setGroupCheckable(0, true, true)
    }
}

fun Activity.hasStoragePermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.hasAudioRecordingPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
}

/**
 * This method return question id if in correct format
 * ex - ##12345624##
 * @param comment - text in comment box
 * @return - if valid - question id else null
 */
fun String.returnIfValidQuestionId(): String? {
    val isMatched = matches(Regex("^##[0-9]+##$"))
    if (isMatched.not()) return null
    else return substring(2, length - 2)
}

@SuppressLint("DefaultLocale")
fun Long.getHumanTimeText(): String =
    java.lang.String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this),
        TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    )

fun RecyclerView.clearDecorations() {
    invalidateItemDecorations()
    if (itemDecorationCount > 0) {
        for (i in itemDecorationCount - 1 downTo 0) {
            removeItemDecorationAt(i)
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun Long.setTheDate(): String {
    val curDateTime = Date(this)
    val formatter = SimpleDateFormat("dd'/'MM'/'y, hh:mm a")
    return formatter.format(curDateTime)
}

fun PackageManager.isAppInstalled(packageName: String): Boolean = try {
    getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    true
} catch (e: Exception) {
    false
}

fun Handler.removeAllCallbacksAndMessages() {
    removeCallbacksAndMessages(null)
}

fun NavController.navigateUpOrFinish(activity: Activity?): Boolean {
    return if (navigateUp()) {
        true
    } else {
        activity?.finish()
        true
    }
}

fun Uri.getFileSize(context: Context): Long? {
    val mediaColumns = arrayOf(MediaStore.Video.Media.SIZE)
    val cursor = context.contentResolver.query(this, mediaColumns, null, null, null)
    cursor?.moveToFirst()
    val sizeColInd: Int = cursor?.getColumnIndex(mediaColumns[0]) ?: return null
    val fileSize: Long = cursor.getLong(sizeColInd)
    cursor.close()
    return fileSize
}

fun View.setWidthFromScrollSize(scrollSize: String?) {
    if (scrollSize != null) {
        updateLayoutParams {
            width = when (scrollSize) {
                "match_parent" -> ViewGroup.LayoutParams.MATCH_PARENT
                "wrap_content" -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> Utils.getWidthFromScrollSize(
                    context,
                    scrollSize
                ) - (marginStart + marginEnd)
            }
        }
    }
}

fun CharSequence?.isNotNullAndNotEmpty(): Boolean = isNullOrEmpty().not()

fun ContentResolver.getFileName(fileUri: Uri): String {

    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}

fun Activity.shareOnWhatsApp(
    imageUrl: String,
    imageFilePath: String?,
    sharingMessage: String?
) {
    Intent(Intent.ACTION_SEND).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        `package` = "com.whatsapp"
        putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")
        if (imageFilePath == null) {
            type = "text/plain"
        } else {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
        }
    }.also {
        if (AppUtils.isCallable(this, it)) {
            startActivity(it)
        } else {
            toast(R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
        }
    }

}

fun RecyclerView.setLayoutOrientation(
    context: Context,
    layoutOrientation: Int,
    spanCount: Int = 2
) {
    when (layoutOrientation) {
        Constants.ORIENTATION_TYPE_GRID -> {
            layoutManager = GridLayoutManager(context, spanCount)
        }
        Constants.ORIENTATION_TYPE_HORIZONTAL_LIST -> {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }
        Constants.ORIENTATION_TYPE_VERTICAL_LIST -> {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }
}

private const val HEX_COLOR_PATTERN = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$"

fun String?.isValidColorCode(): Boolean {
    return when {
        isNullOrEmpty() -> false
        else -> {
            val colorPattern = Pattern.compile(HEX_COLOR_PATTERN)
            val matcher = colorPattern.matcher(this)
            matcher.matches()
        }
    }
}

fun ImageView.setScaleType(
    imageScaleType: String?,
    defaultScaleType: ImageView.ScaleType? = ImageView.ScaleType.FIT_CENTER
) {
    scaleType = when (imageScaleType) {
        "FIT_CENTER" -> ImageView.ScaleType.FIT_CENTER
        "FIT_XY" -> ImageView.ScaleType.FIT_XY
        "MATRIX" -> ImageView.ScaleType.MATRIX
        "FIT_START" -> ImageView.ScaleType.FIT_START
        "FIT_END" -> ImageView.ScaleType.FIT_END
        "CENTER" -> ImageView.ScaleType.CENTER
        "CENTER_CROP" -> ImageView.ScaleType.CENTER_CROP
        "CENTER_INSIDE" -> ImageView.ScaleType.CENTER_INSIDE
        else -> defaultScaleType
    }
}

fun <K, V> Map<K, V?>?.filterValuesNotNull(): Map<K, V> {
    if (this == null) return hashMapOf()
    return filterValues { it != null } as Map<K, V>
}

fun HashMap<String, MutableList<String>>.toMapOfStringVString(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    this.forEach { (s, list) ->
        map[s] = list.joinToString(",")
    }
    return map
}

fun HashMap<String, MutableList<String>>.toHashMapOfStringVString(): HashMap<String, String> {
    val map = hashMapOf<String, String>()
    this.forEach { (s, list) ->
        map[s] = list.joinToString(",")
    }
    return map
}

fun Map<String, List<String>>.toHashMapOfStringVString(): HashMap<String, String> {
    val map = hashMapOf<String, String>()
    this.forEach { (s, list) ->
        map[s] = list.joinToString(",")
    }
    return map
}

fun <T> MutableList<T>.addIfNotPresentElseRemove(element: T) {
    if (this.contains(element))
        this.remove(element)
    else
        this.add(element)
}

fun <T> HashMap<String, T>.toHashmapOfStringVList(): HashMap<String, MutableList<T>> {
    val newMap = hashMapOf<String, MutableList<T>>()
    this.forEach { (s, any) -> newMap[s] = mutableListOf(any) }
    return newMap
}