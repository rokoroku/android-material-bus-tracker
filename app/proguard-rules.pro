# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/rok/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep class com.joanzapata.** { *; }
-keep public class com.fsn.cauly.** { public protected *; }
-keep public class com.trid.tridad.** { public protected *; }
-dontwarn android.webkit.**
-dontwarn java.lang.invoke.*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes *Annotation*

# API 23 fix
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

-keep class android.net.http.** { *; }
-dontwarn android.net.http.**

-keep class android.support.v7.** { *; }
-keep class android.support.v4.** { *; }

# MapDB
-keep class org.mapdb.** { *; }
-dontwarn org.mapdb.**

# Retrofit
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclassmembers class * {
    @retrofit.http.* <methods>;
}

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class kr.rokoroku.mbus.api.gbis.model.** { *; }
-keep class kr.rokoroku.mbus.api.gbisweb.model.** { *; }
-keep class kr.rokoroku.mbus.api.incheon.model.** { *; }
-keep class kr.rokoroku.mbus.api.seoul.model.** { *; }
-keep class kr.rokoroku.mbus.api.seoulweb.model.** { *; }
-keep class kr.rokoroku.mbus.api.tago.model.** { *; }
-keep class kr.rokoroku.mbus.data.model.** { *; }


# SimpleXml
-keep public class javax.xml.** { *; }
-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }
-dontwarn org.simpleframework.**
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}