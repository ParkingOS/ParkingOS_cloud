# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-libraryjars libs\android_api_1.1_forsdk.jar
#-libraryjars libs\android-query-full.0.26.8.jar
#-libraryjars libs\android-support-v4.jar
#-libraryjars libs\BaiduLBS_Android.jar
#-libraryjars libs\galaxy_mini.jar
#-libraryjars libs\gson-2.3.jar
#-libraryjars libs\libammsdk.jar
#-libraryjars libs\umeng-analytics-v5.2.3.jar
#-libraryjars libs\universal-image-loader-1.9.2.jar
#-libraryjars libs\GetuiExt-2.0.3.jar

##----------------------------ZBar二维码配置开始-----------------------------------
-keep class com.zbar.lib.** {*;}
##----------------------------ZBar二维码配置结束-----------------------------------

##----------------------------友盟反馈配置开始-----------------------------------
#-libraryjars libs\com.umeng.fb.v5.1.0.jar
-keep class com.umeng.** {*;}
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

#把[您的应用包名] 替换成您自己的包名，如"com.example.R$*"。
-keep public class com.tq.zld.R$*{
    public static final int *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class com.umeng.fb.ui.ThreadView { }
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
##----------------------------友盟反馈配置结束-----------------------------------

##----------------------------支付宝配置开始-----------------------------------
#-libraryjars libs/alipaysdk.jar
#-libraryjars libs/alipaysecsdk.jar
#-libraryjars libs/alipayutdid.jar
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.auth.AlipaySDK{ public *;}
-keep class com.alipay.sdk.auth.APAuthInfo{ public *;}
-keep class com.alipay.mobilesecuritysdk.*
-keep class com.ut.*
##----------------------------支付宝配置结束-----------------------------------

#-----环信-------
-keep class com.easemob.** {*;}
-keep class org.jivesoftware.** {*;}
-keep class org.apache.** {*;}
-dontwarn  com.easemob.**
#-----环信结束-----

-keep class com.tq.zld.pay.Keys {*;}
-keep class com.actionbarsherlock.** {*;}
-keep class android.support.v4.** {*;}
-keep class com.androidquery.** {*;}
-keep class com.google.gson.** {*;}
-keep class oauth.** {*;}
-keep class com.androidquery.auth.TwitterHandle.** {*;}
-keep class com.baidu.** {*;}
-keep class com.sinovoice.** {*;}
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage { *;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class com.alipay.android.app.IAliPay{*;}
-keep class com.alipay.android.app.IAlixPay{*;}
-dontwarn com.igexin.**
-keep class com.igexin.**{*;}
-dontwarn com.baidu.navisdk.comapi.tts.ttsplayer.**
-dontwarn oauth.signpost.**
-dontwarn com.androidquery.auth.**

-keep public class com.tq.zld.ActivityMonitor$*

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

##---------------Begin: proguard configuration common for all Android apps ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class * {
    public protected *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
##---------------End: proguard configuration common for all Android apps ----------

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.** { *; }
-keep class com.tq.zld.bean.** { *; }

##---------------End: proguard configuration for Gson  ----------