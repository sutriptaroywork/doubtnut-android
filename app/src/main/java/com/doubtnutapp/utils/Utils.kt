package com.doubtnutapp.utils

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.text.*
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.Px
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.bottomnavigation.BottomNavCustomView
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.utils.BranchIOUtils.TAG
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import io.branch.referral.util.BRANCH_STANDARD_EVENT
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.random.Random

object Utils {

    fun sendClassLanguageSpecificEvent(eventName: String) {
        var studentClass = UserUtil.getStudentClass()
        if (studentClass.isBlank()) {
            studentClass = "UNKNOWN_STD_CLS"
        }
        var language = UserUtil.getUserLanguage()
        if (language.isBlank()) {
            language = "UNKNOWN_STD_LANG"
        }
        val newEventName = "${eventName}_${studentClass}_$language"
        val enableFirebase = isFirebaseEnabled(newEventName)
        if (!isProhibitedFromBranch(newEventName))
            DoubtnutApp.INSTANCE.analyticsPublisher.get()
                .publishBranchIoEvent(
                    AnalyticsEvent(newEventName)
                )

        if (enableFirebase) {
            DoubtnutApp.INSTANCE.analyticsPublisher.get()
                .publishFirebaseEvent(
                    AnalyticsEvent(newEventName)
                )
        }
    }

    fun sendClassLangEvents(eventName: String) {
        var studentClass = ""
        when (UserUtil.getStudentClass().toIntOrNull() ?: -1) {
            9, 10 -> studentClass = "_9-10_"
            in 11..13 -> studentClass = "_11-13_"
        }
        val newEventName = "$eventName$studentClass${UserUtil.getUserLanguage()}"
        val enableFirebase = isFirebaseEnabled(newEventName)
        if (studentClass.isNotEmpty() && !isProhibitedFromBranch(newEventName))
            DoubtnutApp.INSTANCE.analyticsPublisher.get()
                .publishBranchIoEvent(
                    AnalyticsEvent(newEventName)
                )

        if (studentClass.isNotEmpty()) {
            if (enableFirebase) {
                DoubtnutApp.INSTANCE.analyticsPublisher.get()
                    .publishFirebaseEvent(
                        AnalyticsEvent(newEventName.replace("-", "_"))
                    )
            }

            var board = UserUtil.getUserBoard()
            if (board == "CBSE"
                || board == "Bihar Board"
                || board == "UP Board"
                || board == "Madhya Pradesh Board"
                || board == "Rajasthan Board" && isFirebaseEnabledForBoardsEvent(newEventName)
            ) {
                board = board.replace(" ", "_")
                DoubtnutApp.INSTANCE.analyticsPublisher.get()
                    .publishFirebaseEvent(
                        AnalyticsEvent((newEventName + "_" + board).replace("-", "_"))
                    )
            }

            if (studentClass == "_11-13_"
                || studentClass == "11"
                || studentClass == "12"
                || studentClass == "13"
            ) {

                if (UserUtil.getUserExams().contains("IIT JEE")) {
                    DoubtnutApp.INSTANCE.analyticsPublisher.get()
                        .publishFirebaseEvent(
                            AnalyticsEvent((newEventName + "_" + "iit_jee").replace("-", "_"))
                        )
                }

                if (UserUtil.getUserExams().contains("NEET")) {
                    DoubtnutApp.INSTANCE.analyticsPublisher.get()
                        .publishFirebaseEvent(
                            AnalyticsEvent((newEventName + "_" + "neet").replace("-", "_"))
                        )
                }
            }
        }
    }

    private fun isFirebaseEnabledForBoardsEvent(eventName: String): Boolean {
        val requiredEvents = hashSetOf(
            "app_open_dn_11-13_English",
            "app_open_dn_11-13_हिंदी",
            "app_open_dn_9-10_English",
            "app_open_dn_9-10_हिंदी"
        )
        return requiredEvents.contains(eventName)
    }

    private fun isFirebaseEnabled(eventName: String): Boolean {
        val requiredEvents = hashSetOf(
            "app_open_dn_11_English",
            "app_open_dn_11_हिंदी",
            "app_open_dn_11-13_English",
            "app_open_dn_11-13_हिंदी",
            "app_open_dn_12_English",
            "app_open_dn_12_हिंदी",
            "app_open_dn_13_English",
            "app_open_dn_13_हिंदी",
            "app_open_dn_9-10_English",
            "app_open_dn_9-10_हिंदी",
            "FindSolutionButtonClick_10_English",
            "FindSolutionButtonClick_10_हिंदी",
            "FindSolutionButtonClick_11_English",
            "FindSolutionButtonClick_11_हिंदी",
            "FindSolutionButtonClick_11-13_English",
            "FindSolutionButtonClick_11-13_हिंदी",
            "FindSolutionButtonClick_12_English",
            "FindSolutionButtonClick_12_हिंदी",
            "FindSolutionButtonClick_13_English",
            "FindSolutionButtonClick_13_हिंदी",
            "FindSolutionButtonClick_9_English",
            "FindSolutionButtonClick_9_हिंदी",
            "FindSolutionButtonClick_9-10_English",
            "FindSolutionButtonClick_9-10_हिंदी",
            "PlayVideoClick_10_English",
            "PlayVideoClick_10_हिंदी",
            "PlayVideoClick_11_English",
            "PlayVideoClick_11_हिंदी",
            "PlayVideoClick_11-13_English",
            "PlayVideoClick_11-13_हिंदी",
            "PlayVideoClick_12_English",
            "PlayVideoClick_12_हिंदी",
            "PlayVideoClick_13_English",
            "PlayVideoClick_13_हिंदी",
            "PlayVideoClick_9_English",
            "PlayVideoClick_9_हिंदी",
            "PlayVideoClick_9-10_English",
            "PlayVideoClick_9-10_हिंदी"

        )
        return requiredEvents.contains(eventName)
    }

    private fun isProhibitedFromBranch(eventName: String): Boolean {
        val requiredEvents = hashSetOf(
            "app_open_dn_11-13_English",
            "FindSolutionButtonClick_11-13_English",
            "PlayVideoClick_11-13_English",
            "FindSolutionButtonClick_9-10_English",
            "PlayVideoClick_9-10_English",
            "app_open_dn_11-13_বাঙালি",
            "FindSolutionButtonClick_11-13_বাঙালি",
            "app_open_dn_11-13_తెలుగు",
            "app_open_dn_9-10_বাঙালি",
            "FindSolutionButtonClick_11-13_ગુજરતી",
            "PlayVideoClick_11-13_বাঙালি",
            "FindSolutionButtonClick_11-13_తెలుగు",
            "app_open_dn_11-13_ગુજરતી",
            "app_open_dn_9-10_తెలుగు",
            "FindSolutionButtonClick_11-13_हिंदी",
            "FindSolutionButtonClick_9-10_हिंदी",
            "app_open_dn_UNKNOWN_STD_CLS_UNKNOWN_STD_LANG",
            "app_open_dn_12_English",
            "click_notification",
            "app_open_dn_9-10_English",
            "app_open_dn_11_English",
            "PlayVideoClick",
            "FindSolutionButtonClick_12_English",
            "app_open_dn_10_English",
            "new_user_registered_UNKNOWN_STD_CLS_english",
            "app_open_dn_14_English",
            "PlayVideoClick_12_English",
            "new_user_registered_UNKNOWN_STD_CLS_hindi",
            "FindSolutionButtonClick_11_English",
            "app_open_dn_9_English",
            "app_open_dn_13_English",
            "app_open_dn_14_हिंदी",
            "FindSolutionButtonClick_10_English",
            "PlayVideoClick_11_English",
            "app_open_dn_8_English",
            "lfet_5m_1d",
            "VideoTabFragmentpage_view_",
            "FindSolutionButtonClick_14_English",
            "app_open_dn_6_English",
            "app_open_dn_UNKNOWN_STD_CLS_English",
            "new_user_registered_language_en",
            "FindSolutionButtonClick_9_English"
        )
        return requiredEvents.contains(eventName)
    }

    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    fun getVersionCode(context: Context): Int {
        val pm = context.packageManager
        try {
            val pi = pm.getPackageInfo(context.packageName, 0)
            return pi.versionCode
        } catch (ex: PackageManager.NameNotFoundException) {
        }

        return 0
    }

    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun parseColor(colorString: String?, default: Int = Color.WHITE): Int {
        if (colorString.isNullOrBlank()) return default
        try {
            return Color.parseColor(colorString)
        } catch (e: IllegalArgumentException) {
            return default
        }
    }

    fun is21(): Boolean {
        return true
    }

    fun getColorGradientPair(index: Int): Int {
        val colorArray = arrayOf(
            R.drawable.gradient_one, R.drawable.gradient_two, R.drawable.gradient_three,
            R.drawable.gradient_four, R.drawable.gradient_five, R.drawable.gradient_six
        )

        val random = getGradientColorIndex(index)
        return colorArray[random]
    }

    fun getColorPair(index: Int): Array<Int> {
        val colorArray = arrayOf(R.color.blue, R.color.red, R.color.colorSecondary, R.color.library)
        val colorArrayDark = arrayOf(
            R.color.blueDark,
            R.color.redDark,
            R.color.colorSecondaryTemp,
            R.color.libraryDark
        )

        val random = getColorIndex(index)
        return arrayOf(colorArray[random], colorArrayDark[random])
    }

    fun getMatchColorPair(index: Int): Int {
        val colorArray = arrayOf(
            R.color.match1,
            R.color.match2,
            R.color.match3,
            R.color.match4,
            R.color.match5,
            R.color.match6
        )

        val random = getGradientColorIndex(index)
        return colorArray[random]
    }

    fun getColorIndex(index: Int): Int {
        return if (index > 3) {
            rangedIndex(index)
        } else {
            index
        }
    }

    fun rangedIndex(index: Int): Int {
        val range = 3
        var i = index
        while (i > range) {
            i -= (range + 1)
        }
        return i
    }

    fun getGradientColorPair(index: Int): Array<Int> {
        val colorArrayStart = arrayOf(
            Color.parseColor("#74276c"),
            Color.parseColor("#274b74"),
            Color.parseColor("#8929ad"),
            Color.parseColor("#f8ae4d"),
            Color.parseColor("#00c288"),
            Color.parseColor("#ea5a6f")
        )
        val colorArrayMiddle = arrayOf(
            Color.parseColor("#c53364"),
            Color.parseColor("#8233c5"),
            Color.parseColor("#436aac"),
            Color.parseColor("#e84b76"),
            Color.parseColor("#01992a"),
            Color.parseColor("#de791e")
        )
        val colorArrayEnd = arrayOf(
            Color.parseColor("#fd8263"),
            Color.parseColor("#e963fd"),
            Color.parseColor("#43b7b8"),
            Color.parseColor("#a11adc"),
            Color.parseColor("#01325e"),
            Color.parseColor("#fccf3a")
        )

        val random = getGradientColorIndex(index)
        return arrayOf(colorArrayStart[random], colorArrayMiddle[random], colorArrayEnd[random])
    }

    fun getPath(context: Context, uri: Uri): String? {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null) {
                    val columnIndex = cursor.getColumnIndexOrThrow("_data")
                    if (cursor.moveToFirst()) {
                        return cursor.getString(columnIndex)
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getGradientColorIndex(index: Int): Int {
        return if (index > 5) {
            rangedGradientIndex(index)
        } else {
            index
        }
    }

    fun rangedGradientIndex(index: Int): Int {
        val range = 5
        var i = index
        while (i > range) {
            i -= (range + 1)
        }
        return i
    }

    fun getChapterSubstring(chapter: String): String {
        return if (chapter.contains(" ")) {
            chapter.substring(0, 1) + chapter.substring(
                chapter.indexOf(" ") + 1,
                chapter.indexOf(" ") + 2
            )
        } else {
            chapter.substring(0, 1)
        }
    }

    fun getEmail(context: Context): String {
        var possibleEmail = ""
        val emailPattern = Patterns.EMAIL_ADDRESS // API level 8+
        val accounts = AccountManager.get(context).accounts
        for (account in accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name
            }
        }
        return possibleEmail
    }

    fun getEncodedImage(bmp: Bitmap): String {
        val byteCount = bmp.byteCount.toFloat()
        val bitWidth = bmp.width.toFloat()
        val bitHeight = bmp.height.toFloat()
        val bitper = byteCount * 8 / (bitWidth * bitHeight)

        val baos = ByteArrayOutputStream()
        if (bitper >= 16.0) {
            bmp.compress(
                Bitmap.CompressFormat.JPEG,
                (100.0 * (16.0 / bitper).toFloat()).toInt(),
                baos
            )
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }

        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun getUsername(userName: String): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        if (userName != null) params["username"] = userName
        return params
    }

    fun getNPSFeedbackBody(type: String, id: String, selectedRating: String): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        params["type"] = type
        params["campaign_id"] = id
        params["rating"] = selectedRating
        return params
    }

    fun getFeedbackBody(
        type: String?,
        id: String?,
        options: String?,
        parent_ques_id: String
    ): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        if (type != null) params["type"] = type
        if (id != null) params["campaign_id"] = id
        if (options != null) params["rating"] = options
        params["question_id"] = parent_ques_id
        return params
    }

    fun getUpdateProfileClassBody(studentClass: String, email: String): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        if (studentClass != null) params["student_class"] = studentClass
        if (email != null) params["email"] = email
        return params
    }

    fun getDoubtFields(doubt: String): String {
        var doubtString = ""
        if (doubt.isNotEmpty()) {
            val words = doubt.split("_")
            if (words.size > 2) {
                doubtString = when {
                    words[2] == "SLV" -> {
                        Constants.NCERT_SLV + Constants.NCERT_QN + doubt.split("_").last()
                    }
                    words[2] == "MEX" -> {
                        Constants.NCERT_MEX + Constants.NCERT_QN + doubt.split("_").last()
                    }
                    else -> {
                        Constants.NCERT_EX + words[2].substring(1) + Constants.NCERT_QN + words.last()
                    }
                }
            }
        }
        return doubtString

    }

    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }

    @SuppressLint("WrongConstant")
    fun getAllInstalledAppsAndSendToServer(context: Context): String {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        try {
            val packageManager = context.packageManager
            val apps = packageManager.getInstalledPackages(PackageManager.SIGNATURE_MATCH)
            apps.forEach { app ->
                val jsonNestedObject = JSONObject()
                try {
                    jsonNestedObject.put("appName", getAppNameFromPkgName(context, app.packageName))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                try {
                    jsonNestedObject.put("appPackageName", app.packageName)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                jsonArray.put(jsonNestedObject)
            }
            try {
                jsonObject.put(
                    "deviceId",
                    defaultPrefs(context).getString(Constants.DEVICE_NAME, "")
                )
                jsonObject.put(
                    "appVersion",
                    defaultPrefs(context).getString(Constants.APP_VERSION, "")
                )
                jsonObject.put("sdkVersion", Build.VERSION.RELEASE)
                jsonObject.put("installedAppList", jsonArray)
            } catch (e: JSONException) {
                Log.d(TAG, e.toString())
            }

            return """'$jsonObject'"""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getAppNameFromPkgName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(info) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun getAccountUsername(context: Context): String {
        val manager = AccountManager.get(context)
        val accounts = manager.getAccountsByType("com.google")
        val possibleEmails = LinkedList<String>()

        for (account in accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.
            possibleEmails.add(account.name)
        }

        if (!possibleEmails.isEmpty()) {
            val email = possibleEmails[0]
            val parts = email.split("@".toRegex())
            if (parts.size > 0 && parts[0] != null) {
                return parts[0]
            } else
                return ""
        } else
            return ""
    }

    fun getTestResponseBody(
        testId: Int,
        actionType: String,
        isReview: Int?,
        optionCode: String,
        sectionCode: String,
        testSubcriptionId: Int,
        questionbankId: Int,
        questionType: String,
        isEligible: String,
        timeTake: Int?,
        subjectCode: String?,
        chapterCode: String?,
        subtopicCode: String?,
        classCode: String?,
        mcCode: String?
    ): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        if (testId != null) params["test_id"] = testId
        if (actionType != null) params["action_type"] = actionType
        if (isReview != null) params["is_review"] = isReview
        if (optionCode != null) params["option_code"] = optionCode
        if (sectionCode != null) params["section_code"] = sectionCode
        if (testSubcriptionId != null) params["test_subscription_id"] = testSubcriptionId
        if (questionbankId != null) params["questionbank_id"] = questionbankId
        if (questionType != null) params["question_type"] = questionType
        if (isEligible != null) params["is_eligible"] = isEligible
        if (timeTake != null) params["time_taken"] = timeTake
        if (subjectCode != null) params["subject_code"] = subjectCode
        if (chapterCode != null) params["chapter_code"] = chapterCode
        if (subtopicCode != null) params["subtopic_code"] = subtopicCode
        if (classCode != null) params["class_code"] = classCode
        if (mcCode != null) params["mc_code"] = mcCode
        return params
    }

    fun getMockTestResponseBody(
        testId: Int,
        actionType: String,
        isReview: Int?,
        optionCode: String,
        sectionCode: String,
        testSubcriptionId: String,
        questionbankId: String,
        questionType: String,
        isEligible: String,
        timeTake: Int?,
        subjectCode: String?,
        chapterCode: String?,
        subtopicCode: String?,
        classCode: String?,
        mcCode: String?,
        reviewStatus: String?
    ): HashMap<String, Any> {
        val params: HashMap<String, Any> = HashMap()
        if (testId != null) params["test_id"] = testId
        if (actionType != null) params["action_type"] = actionType
        if (isReview != null) params["is_review"] = isReview
        if (optionCode != null) params["option_code"] = optionCode
        if (sectionCode != null) params["section_code"] = sectionCode
        if (testSubcriptionId != null) params["test_subscription_id"] = testSubcriptionId
        if (questionbankId != null) params["questionbank_id"] = questionbankId
        if (questionType != null) params["question_type"] = questionType
        if (isEligible != null) params["is_eligible"] = isEligible
        if (timeTake != null) params["time_taken"] = timeTake
        if (subjectCode != null) params["subject_code"] = subjectCode
        if (chapterCode != null) params["chapter_code"] = chapterCode
        if (subtopicCode != null) params["subtopic_code"] = subtopicCode
        if (classCode != null) params["class_code"] = classCode
        if (mcCode != null) params["mc_code"] = mcCode
        if (reviewStatus != null) params["review_status"] = reviewStatus
        return params
    }

    fun getPackFromURL(url: String): String {

        return if (url.isNullOrBlank() && url.contains("/pdf_download/")) {
            val pdfPackage = url.split("/pdf_download/".toRegex())
            if (pdfPackage.isNotEmpty() && pdfPackage[1].isNullOrBlank()) {
                pdfPackage[1]
            } else
                ""
        } else if (url.isNullOrBlank() && url.contains("/pdf_open/")) {
            val pdfPackage = url.split("/pdf_open/".toRegex())
            if (pdfPackage.isNotEmpty() && pdfPackage[1].isNullOrBlank()) {
                pdfPackage[1]
            } else
                ""
        } else ""
    }

    fun pullLinks(input: String): ArrayList<String> {
        val containedUrls = ArrayList<String>()
        val urlRegex = "(?:(?:https?|ftp):\\/\\/)?[\\w/\\-?=%.]+\\.[\\w/\\-?=%.]+"
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val urlMatcher = pattern.matcher(input)
        while (urlMatcher.find()) {
            containedUrls.add(
                input.substring(
                    urlMatcher.start(0),
                    urlMatcher.end(0)
                )
            )
        }
        return containedUrls
    }

    fun currentTime(): String {

        val tsLong = System.currentTimeMillis() / 1000
        val ts = tsLong.toString()
        return ts
    }

    fun executeIfContextNotNull(context: Context?, block: (Context) -> Unit) {
        context?.let {
            block(context)
        }
    }

    fun getSecondsToString(pTime: Int): String {
        return String.format("%02d:%02d", pTime / 60, pTime % 60)
    }

    fun getMinutesDurationToString(pTime: Int): String {
        val hour = pTime / 60
        val mins = pTime % 60
        var time = ""
        if (hour > 0) {
            time = String.format("%01d h", pTime / 60)
        }
        if (mins > 0) {
            time += " " + String.format("%02d m", pTime % 60)
        }
        return time
    }

    fun getMilisecondTimeString(timeInMilis: Long): String? {
        val seconds = timeInMilis / 1000 % 3600 % 60
        val minutes = timeInMilis / 1000 % 3600 / 60
        val hour = timeInMilis / 1000 / 3600
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minutes, seconds)
        }
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getSpannableNumberString(textString: String): SpannableStringBuilder {

        val updatedString = SpannableStringBuilder(textString)
        val regex = "\\d{10,11}"

        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(textString)

        var count = 0
        while (matcher.find()) {
            count++
            System.out.println(
                "found: " + count + " : "
                        + matcher.start() + " - " + matcher.end()
            )

            updatedString.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return updatedString
    }

    fun playerStateToString(state: PlayerConstants.PlayerState): String {
        when (state) {
            PlayerConstants.PlayerState.UNKNOWN -> return "UNKNOWN"
            PlayerConstants.PlayerState.UNSTARTED -> return "UNSTARTED"
            PlayerConstants.PlayerState.ENDED -> return "ENDED"
            PlayerConstants.PlayerState.PLAYING -> return "PLAYING"
            PlayerConstants.PlayerState.PAUSED -> return "PAUSED"
            PlayerConstants.PlayerState.BUFFERING -> return "BUFFERING"
            PlayerConstants.PlayerState.VIDEO_CUED -> return "VIDEO_CUED"
            else -> return "status unknown"
        }
    }

    fun convertToMilis(date: String?): Long {
        if (date == null) return -1
        val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date)

        val calendar = Calendar.getInstance()
        calendar.time = dateTime

        return calendar.timeInMillis
    }

    fun formatTime(context: Context, date: String?): String {
        if (date == null) return ""
        val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date)

        val calendar = Calendar.getInstance()
        calendar.time = dateTime

        return getTimeAgo(context, calendar.timeInMillis)
    }

    private fun getTimeAgo(context: Context, givenTime: Long): String {
        val SECOND_MILLIS = 1000
        val MINUTE_MILLIS = 60 * SECOND_MILLIS
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS

        var time = givenTime
        //if timestamp given in seconds, convert to millis
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return context.getString(R.string.string_timeline_justNow)
        }

        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> {
                context.getString(R.string.string_timeline_justNow)
            }
            diff < 2 * MINUTE_MILLIS -> {
                context.getString(R.string.string_timeline_aMinuteAgo)
            }
            diff < 50 * MINUTE_MILLIS -> {
                String.format(
                    context.getString(R.string.string_timeline_minutesAgo),
                    diff / MINUTE_MILLIS
                )
            }
            diff < 90 * MINUTE_MILLIS -> {
                context.getString(R.string.string_timeline_anHourAgo)
            }
            diff < 24 * HOUR_MILLIS -> {
                String.format(
                    context.getString(R.string.string_timeline_hoursAgo),
                    diff / HOUR_MILLIS
                )
            }
            diff < 48 * HOUR_MILLIS -> {
                context.getString(R.string.string_yesterday)
            }
            else -> {
                val days = diff / DAY_MILLIS
                return if (days < 365) {
                    String.format(context.getString(R.string.string_timeline_daysAgo), days)
                } else {
                    String.format(
                        context.getString(R.string.string_timeline_yearsAgo),
                        (days / 365)
                    )
                }
            }
        }
    }

    fun getCountToSend(hashMapString: String, key: String): Int {
        val map: HashMap<String, Any>? =
            Gson().fromJson(hashMapString, object : TypeToken<HashMap<String, Any>>() {}.type)
        var countToSend = 0
        if (map != null && map.containsKey(key)) {
            val actualValue: Double = map[key] as Double
            countToSend = floor(actualValue).toInt()
            val diff: Double = (actualValue - countToSend) * 100
            val randomValue: Int = Random.nextInt(0, 100)
            if (diff > randomValue) {
                countToSend += 1
            }
        }
        return countToSend
    }

    fun getAllSelectedExams(apiOnBoardingStatus: ApiOnBoardingStatus): String =
        apiOnBoardingStatus.selectedExamBoards?.filter {
            it.category == "exam"
        }?.joinToString(",") {
            it.course
        }.orEmpty()

    fun getSelectedBoard(apiOnBoardingStatus: ApiOnBoardingStatus): String =
        apiOnBoardingStatus.selectedExamBoards?.filter {
            it.category == "board"
        }?.joinToString(",") {
            it.course
        }.orEmpty()

    fun checkValidClassExamForEvent(userClass: String, selectedExams: List<String>): Boolean {
        val validClass = userClass == "11" || userClass == "12" || userClass == "13"
        val validExam = selectedExams.contains("IIT JEE") || selectedExams.contains("NEET")
        return validClass && validExam
    }

    private val scrollSizeRegex = Regex("([a-z])")

    fun getWidthFromScrollSize(context: Context, scrollSize: String): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val sizeNew = scrollSizeRegex.replace(scrollSize, "")
        val outValue = TypedValue()
        context.resources.getValue(R.dimen.spacing, outValue, true)
        val value = outValue.float
        val devicewidth =
            ((displayMetrics.widthPixels - (2 * convertDpToPixel(value))) / sizeNew.toFloat())
        return devicewidth.toInt()
    }

    fun getRatioFromScrollSize(scrollSize: String): String {
        val sizeNew = scrollSizeRegex.replace(scrollSize, "")
        val ratio = when {
            sizeNew.equals("2") || sizeNew.equals("2.5") -> "1:1"
            sizeNew.equals("1") || sizeNew.equals("1.5") -> "16:9"
            else -> "1:1"
        }
        return ratio
    }

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) return false
        if (context is Activity) {
            if (context.isDestroyed || context.isFinishing) {
                return false
            }
        }
        return true
    }

    @SuppressLint("HardwareIds")
    fun saveIsEmulatorAndSafetyNetResponseToPref() {
        if (isProbablyAnEmulator()) {
            defaultPrefs().edit().putBoolean(Constants.IS_EMULATOR, true).apply()
        }
        //return if already have
        if (!defaultPrefs().getString(Constants.GAME_TOKEN, "").isNullOrBlank()) return

        val context = DoubtnutApp.INSTANCE.applicationContext
        val udid = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        val nonceString = udid + "::" + System.currentTimeMillis()
        val nonce = nonceString.toByteArray()

        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) ==
            ConnectionResult.SUCCESS
        ) {
            SafetyNet.getClient(context)
                .attest(
                    nonce,
                    context.resources.getString(R.string.google_api_key)
                )
                .addOnSuccessListener { response ->
                    // Indicates communication with the service was successful.
                    // Use response.getJwsResult() to get the result data.
                    val jwsResult = response.jwsResult
                    defaultPrefs().edit()
                        .putString(Constants.GAME_TOKEN, encrypt(jwsResult.orEmpty()))
                        .apply()
                }
                .addOnFailureListener { e ->
                    Log.e(e)
                    // An error occurred while communicating with the service.
                    if (e is ApiException) {
                        // An error with the Google Play services API contains some
                        // additional details.
                        val apiException = e
                        // You can retrieve the status code using the
                        // apiException.statusCode property.
                    } else {
                        // A different, unknown type of error occurred.
                    }
                }
        } else {
            defaultPrefs().edit().putBoolean(Constants.HAS_PLAY_SERVICE, false).apply()
        }
    }

    fun decrypt(strToDecrypt: String?): String? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, setKey("d0ub2nut12435689"))
            String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun encrypt(strToEncrypt: String): String? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, setKey("d0ub2nut12435689"))
            Base64.encodeToString(
                cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))),
                Base64.DEFAULT
            )
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun setKey(myKey: String): SecretKeySpec? {
        var secretKey: SecretKeySpec? = null
        try {
            val key = myKey.toByteArray(charset("UTF-8"))
            secretKey = SecretKeySpec(key, "AES")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return secretKey
    }

    private fun isProbablyAnEmulator() = Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.BOARD == "QC_Reference_Phone" //bluestacks
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build") //MSI App Player
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT

    fun sendRegistrationBranchEvents(
        analyticsPublisher: AnalyticsPublisher,
        userSelectedExamsList: List<String>,
        userClass: String
    ) {
        if (checkValidClassExamForEvent(userClass, userSelectedExamsList)) {
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_REGISTRATION_11to13,
                    hashMapOf()
                )
            )
            if (userSelectedExamsList.contains("IIT JEE")) {
                when (userClass) {
                    "11" -> {
                        analyticsPublisher.publishBranchIoEvent(
                            AnalyticsEvent(
                                EventConstants.DN_REG_JEE_11,
                                hashMapOf()
                            )
                        )
                    }
                    "12" -> {
                        analyticsPublisher.publishBranchIoEvent(
                            AnalyticsEvent(
                                EventConstants.DN_REG_JEE_12,
                                hashMapOf()
                            )
                        )
                    }
                    "13" -> {
                        analyticsPublisher.publishBranchIoEvent(
                            AnalyticsEvent(
                                EventConstants.DN_REG_JEE_13,
                                hashMapOf()
                            )
                        )
                    }
                }
            }
        }
    }

    fun sendLoginBranchEvents(
        analyticsPublisher: AnalyticsPublisher,
        userSelectedExamsList: List<String>,
        userClass: String
    ) {
        if (userClass.isNotEmpty()) {
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    BRANCH_STANDARD_EVENT.LOGIN.name,
                    hashMapOf()
                )
            )
        }

    }

    fun setWidthBasedOnPercentage(
        context: Context,
        view: View,
        size: String,
        @AnyRes spacing: Int
    ) = ViewUtils.setWidthBasedOnPercentage(context, view, size, spacing)

    fun getShape(
        colorString: String,
        strokeColor: String,
        cornerRadius: Float = 8f,
        strokeWidth: Int = 3,
        shape: Int = GradientDrawable.RECTANGLE
    ): GradientDrawable {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = shape
        shapeDrawable.cornerRadii = floatArrayOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius
        )
        shapeDrawable.setColor(parseColor(colorString))
        shapeDrawable.setStroke(strokeWidth, parseColor(strokeColor))
        return shapeDrawable
    }

    fun getShape(
        colorString: String,
        strokeColor: String,
        topLeftRadius: Float = 8f,
        topRightRadius: Float = 8f,
        bottomRightRadius: Float = 8f,
        bottomLeftRadius: Float = 8f,
        strokeWidth: Int = 3,
        shape: Int = GradientDrawable.RECTANGLE
    ): GradientDrawable {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = shape
        shapeDrawable.cornerRadii = floatArrayOf(
            topLeftRadius,
            topLeftRadius,
            topRightRadius,
            topRightRadius,
            bottomRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
            bottomLeftRadius
        )
        shapeDrawable.setColor(parseColor(colorString))
        shapeDrawable.setStroke(strokeWidth, parseColor(strokeColor))
        return shapeDrawable
    }

    fun getShape(
        colorString: String,
        strokeColor: String,
        radiusArray: FloatArray,
        strokeWidth: Int = 3,
        shape: Int = GradientDrawable.RECTANGLE
    ): GradientDrawable {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = shape
        shapeDrawable.cornerRadii = radiusArray
        shapeDrawable.setColor(parseColor(colorString))
        shapeDrawable.setStroke(strokeWidth, parseColor(strokeColor))
        return shapeDrawable
    }

    fun getMaterialShapeDrawable(
        colorString: String,
        @Px cornerRadius: Float = 0f
    ): MaterialShapeDrawable {
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
            .build()
        return MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ColorStateList.valueOf(parseColor(colorString))
        }
    }

    fun getGradientView(
        startGradient: String, midGradient: String, endGradient: String,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM,
        cornerRadius: Float = 0f
    ): GradientDrawable {
        val colors =
            intArrayOf(parseColor(startGradient), parseColor(midGradient), parseColor(endGradient))
        val gd = GradientDrawable(orientation, colors)
        gd.cornerRadius = cornerRadius
        return gd
    }

    fun getInstallationDays(context: Context = DoubtnutApp.INSTANCE): Int {
        val installationDate =
            Date(context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime)
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installationDate.time)
            .toInt()
    }

    fun getDeviceOrientation(context: Context) = context.resources.configuration.orientation

    fun isDeviceOrientationPortrait(context: Context?): Boolean =
        if (context != null) {
            getDeviceOrientation(context) == Configuration.ORIENTATION_PORTRAIT
        } else {
            true
        }

    fun progressBarColor(progress: Int): Int {
        return when (progress) {
            in 0..20 -> Color.rgb(255, 77, 77)
            in 21..40 -> Color.rgb(77, 166, 255)
            in 41..60 -> Color.rgb(255, 99, 71)
            in 61..80 -> Color.rgb(255, 255, 0)
            in 81..100 -> Color.rgb(102, 255, 51)
            else -> Color.rgb(102, 255, 51)
        }
    }

    fun getTimeSpentForEventFromActualDuration(durationActual: Long): String {
        return if (durationActual > 3601) {
            "3600+"
        } else if (durationActual > 3600) {
            "3600"
        } else if (durationActual > 3000) {
            "3000"
        } else if (durationActual > 2700) {
            "2700"
        } else if (durationActual > 1800) {
            "1800"
        } else if (durationActual > 1200) {
            "1200"
        } else if (durationActual > 600) {
            "600"
        } else if (durationActual > 300) {
            "300"
        } else if (durationActual > 100) {
            "100"
        } else if (durationActual > 60) {
            "60"
        } else if (durationActual > 30) {
            "30"
        } else if (durationActual > 15) {
            "15"
        } else if (durationActual > 5) {
            "5"
        } else {
            durationActual.toString()
        }
    }

    fun setMaxLinesEditText(editText: EditText, maxLines: Int) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (null != editText.layout && editText.layout
                        .lineCount > maxLines
                ) {
                    editText.text
                        .delete(editText.text.length - 1, editText.text.length)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    fun setVerifiedTickTextView(textView: TextView) {
        textView.setCompoundDrawablesWithIntrinsicBounds(
            null, null,
            textView.context.getDrawable(R.drawable.ic_verify_tick), null
        )
        textView.compoundDrawablePadding = 12
    }

    fun getUDID(): String {
        val context = DoubtnutApp.INSTANCE.applicationContext
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels

    val screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels

    fun getStringFormJsonObject(jsonObject: JSONObject, key: String): String? {
        if (jsonObject.has(key)) {
            return jsonObject.getString(key)
        }
        return null
    }

    fun hasPipModePermission(): Boolean =
        DoubtnutApp.INSTANCE.run {
            if (deviceSupportsPipMode()) {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps?.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        Process.myUid(),
                        packageName
                    )
                } else {
                    appOps?.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        Process.myUid(),
                        packageName
                    )
                } == AppOpsManager.MODE_ALLOWED
            } else {
                false
            }
        }

    /**
     * @param currentTaskId Do not remove tasks with this id
     */
    fun removeTasksWithAnyOfGivenActivitiesAsRoot(
        currentTaskId: Int,
        vararg activityClassNames: String
    ) {
        // Need to check this because Android s*cks
        // TaskInfo.baseActivity is not found below Android 6, and very gracefully,
        // no warning is given by Android Studio, resulting in crash below Android 6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DoubtnutApp.INSTANCE.apply {
                (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.appTasks?.filterNotNull()
                    ?.forEach { task ->
                        if (currentTaskId != task.taskInfo?.getId()) {
                            val baseActivityClassName = task.taskInfo?.baseActivity?.className
                            if (activityClassNames.any { it == baseActivityClassName }) {
                                task.finishAndRemoveTask()
                            }
                        }
                    }
            }
        }
    }

    inline val pipSupportedActivities: Array<String>
        get() = arrayOf(VideoPageActivity::class.java.name, LiveClassActivity::class.java.name)

    fun anyAppExitCoreActionNotDone(): Boolean =
        CoreActions.appExitCoreActions.any {
            defaultPrefs().getBoolean(it, false).not()
        }

    fun isInForeGround(): Boolean {
        return try {
            val appProcessInfo = ActivityManager.RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(appProcessInfo)
            (appProcessInfo.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE)
        } catch (e: Exception) {
            false
        }
    }

    fun isActivityOnTop(list: List<String>): Boolean {
        var isOnTop = false
        try {
            val manager: ActivityManager =
                DoubtnutApp.INSTANCE.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskList: List<ActivityManager.RunningTaskInfo> = manager.getRunningTasks(10)
            if (list.contains(taskList[0].topActivity?.className)) {
                isOnTop = true
            }
        } catch (e: Exception) {

        }
        return isOnTop
    }

    fun getFileExtension(filePath: String): String {
        if (!TextUtils.isEmpty(filePath)) {
            val extension: Int = filePath.lastIndexOf('.')
            if (extension > 0 && extension < filePath.length - 1) {
                return filePath.substring(extension + 1)
            }
        }
        return ""
    }

    fun getMimeType(uri: Uri): String = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cr: ContentResolver = DoubtnutApp.INSTANCE.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
            uri.toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.lowercase(Locale.getDefault())
        )
    }.orEmpty()

    fun openPlayStore(context: Context) {
        val packageName = if (BuildConfig.DEBUG) "com.doubtnutapp" else BuildConfig.APPLICATION_ID
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        try {
            context.startActivity(Intent(intent).setPackage("com.android.vending"))
        } catch (exception: ActivityNotFoundException) {
            context.startActivity(intent)
        }
    }

    fun getLibraryBottomText(context: Context): String? {
        var text: String? = null
        if (FeaturesManager.isFeatureEnabled(context, Features.LIVE_CLASS_BOTTOM_ICON_NAME)) {
            val liveClassBottomIconPayload =
                FeaturesManager.getFeaturePayload(context, Features.LIVE_CLASS_BOTTOM_ICON_NAME)
            if (liveClassBottomIconPayload != null) {
                text =
                    if (defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "en") == "hi") {
                        liveClassBottomIconPayload["hi"] as? String
                    } else {
                        liveClassBottomIconPayload["en"] as? String
                    }
            }
        }
        return text
    }

    fun hasAudioExtension(url: String?): Boolean {
        if (!url.isNullOrBlank()) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension == "m2a"
                || extension == "m3u"
                || extension == "mp2"
                || extension == "mp3"
                || extension == "mpga"
                || extension == "wav"
            ) {
                return true
            }
        }
        return false
    }

    fun publishBottomNavTabClickEvent(
        analyticsPublisher: AnalyticsPublisher,
        title: String,
        position: String
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${BottomNavCustomView.EVENT_TAG_BOTTOM_NAVIGATION}_${EventConstants.TAB_CLICK}",
                hashMapOf(
                    EventConstants.TAB_TITLE to title,
                    EventConstants.ITEM_POSITION to position,
                ),
                ignoreSnowplow = true
            )
        )
    }

    fun isBottomNavigationIconsApiDataAvailable(response: String): Boolean {
        val bottomNavigationTabsData =
            Gson().fromJson(response, BottomNavigationTabsData::class.java)
                ?: return false
        return bottomNavigationTabsData.tab1 != null &&
                bottomNavigationTabsData.tab2 != null &&
                bottomNavigationTabsData.tab3 != null &&
                bottomNavigationTabsData.tab4 != null
    }
}