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

-dontnote

-keep public class com.mindlinker.mlsdk.R$*{
public static final int *;
}
-keep public class com.mindlinker.sdk.R$*{
public static final int *;
}
-keep class **.R$* { *; }

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

#kotlin
-dontwarn kotlin.**
-keep public class kotlin.**{*;}

-dontwarn kotlin.**

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

#Android
-dontwarn android.**
-keep public class android.**{*;}
-keep class android.support.** { *; }
-keepattributes *Annotation*
-keep public class * extends android.support.annotation.**

#Androidx
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**


# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# Java
-keep class java.util.** { *; }
-keep class java.lang.** { *; }

-dontwarn dalvik.**
-keep public class dalvik.**{*;}

#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

############################################# sdk相关混淆开始 ########################################

# maxhub-logger
-dontwarn com.maxhub.liblogreporter.**
-keep public class com.maxhub.liblogreporter.**{*;}

# maxme sdk
-keep class com.mindlinker.maxme.** { *; }

-keep class com.mindlinker.sdk.model.** { *; }

# webrtc
-dontwarn org.webrtc.**
-keep class org.webrtc.**{*;}
-keepclassmembers,includedescriptorclasses class * { native <methods>; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

################ 第三方混淆开始 ################

# Retrofit 2.X
## https://square.github.io/retrofit/ ##

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# rxjava
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }
-keep class * extends io.reactivex.** { *; }
-dontwarn sun.misc.**

# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Gson
-keep class com.google.gson.**{*;}

################ 第三方混淆结束 ################

############################################# sdk相关混淆结束 ########################################

############################################# app相关混淆开始 ########################################

-keep class com.mindlinker.mlsdk.model.** { *; }

-dontwarn org.jetbrains.annotations.**

# umeng
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class com.uc.** { *; }
-keep class com.efs.** { *; }
-keep class com.umeng.** {*;}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Parcelable 序列化类不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

