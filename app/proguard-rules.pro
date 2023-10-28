# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-keepattributes SourceFile,LineNumberTable

#Retrofit
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**

#Branch.io
-dontwarn com.google.firebase.appindexing.**
-dontwarn com.android.installreferrer.api.**

# for gms ads
-keep class com.google.android.gms.ads.identifier.** { *; }

-ignorewarnings

#android logs
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#Annotations
-dontwarn org.androidannotations.**

# Gson
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }

#picasso
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**

#Mathview
-dontwarn com.x5.util.**
-dontwarn com.x5.template.**

#Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

#App model
-keep class com.doubtnutapp.data.remote.models.** { *; }


-keep class android.support.** { *; }

-keep interface android.support.** { *; }

-keepattributes SourceFile,LineNumberTable

#androidx
-keep class androidx.core.app.CoreComponentFactory { *; }

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# android link preview
-keeppackagenames org.jsoup.nodes


-dontwarn com.doubtnutapp.ui.mockTest.MockTestQuestionFragment.onTextData.**

-keep class com.apxor.** { *; }
-dontwarn com.apxor.**


# for DexGuard only
#-keep resourcexmlelements manifest/application/meta-data@value=GlideModule

-keep class com.facebook.FacebookSdk {
   boolean isInitialized();
}
-keep class com.facebook.appevents.AppEventsLogger {
   com.facebook.appevents.AppEventsLogger newLogger(android.content.Context);
   void logSdkEvent(java.lang.String, java.lang.Double, android.os.Bundle);
}

-keep class com.shockwave.**

-keep public class * extends java.lang.Exception

-keep class com.uxcam.** { *; }
-dontwarn com.uxcam.**

-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}


-dontwarn com.google.android.gms.location.**
-keep class com.google.android.gms.location.** { *; }

-keep class com.moe.pushlibrary.activities.** { *; }
-keep class com.moe.pushlibrary.MoEHelper
-keep class com.moengage.locationlibrary.GeofenceIntentService
-keep class com.moe.pushlibrary.InstallReceiver
-keep class com.moe.pushlibrary.providers.MoEProvider
-keep class com.moe.pushlibrary.models.** { *;}
-keep class com.moengage.core.GeoTask
-keep class com.moengage.location.GeoManager
-keep class com.moengage.inapp.InAppManager
-keep class com.moengage.push.PushManager
-keep class com.moengage.inapp.InAppController
-keep class com.moe.pushlibrary.AppUpdateReceiver
-keep class com.moengage.core.MoEAlarmReceiver
-keep class com.moengage.core.MoEngage

# Push
-keep class com.moengage.pushbase.activities.PushTracker
-keep class com.moengage.pushbase.activities.SnoozeTracker
-keep class com.moengage.pushbase.push.MoEPushWorker
-keep class com.moe.pushlibrary.MoEWorker

# Real Time Triggers
-keep class com.moengage.addon.trigger.DTHandlerImpl
-keep class com.moengage.core.MoEDTManager
-keep class com.moengage.core.MoEDTManager.DTHandler

# Push Amplification
-keep class com.moengage.addon.messaging.MessagingHandlerImpl
-keep class com.moengage.push.MoEMessagingManager
-keep class com.moengage.addon.messaging.MoEMessageSyncJob
-keep class com.moengage.addon.messaging.MoEMessageSyncReceiver
-keep class com.moengage.addon.messaging.MoEMessageSyncIntentService

-dontwarn com.moengage.location.GeoManager
-dontwarn com.moengage.core.GeoTask
-dontwarn com.moengage.receiver.*
-dontwarn com.moengage.worker.*
-dontwarn com.moengage.inapp.ViewEngine

-keep class com.delight.**  { *; }

-keep class com.naman14.spider.** { *; }

-keep class com.loopj.android.http.** { *; }

-keep class org.jsoup.**


-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
# Keep all constructors on ListenableWorker, Worker (also marked with @Keep)
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
# We need to keep WorkerParameters for the ListenableWorker constructor
-keep class androidx.work.WorkerParameters

# Truecaller SDK
-keep class com.truecaller.android.sdk.models.**{ *; }

# Conviva Integration
#-keepclassmembers enum * { *; }
#to exclude ExoPlayer class
#-keep class com.google.android.exoplayer2.** { *; }
#-keep interface com.google.android.exoplayer2.** { *; }
#to exclude Conviva class
#-keepclasseswithmembers class com.conviva.sdk.** { *; }
#-keepclasseswithmembers class com.conviva.playerinterface.** { *; }

# https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/d6330d582c34a44ea174884f0b67c431?time=last-thirty-days&sessionEventKey=60C755E200CC0001130A2ADC24C49AD6_1552082592199218586
# https://stackoverflow.com/questions/49228979/badparcelableexceptionclassnotfoundexception-when-unmarshalling-android-suppor
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

