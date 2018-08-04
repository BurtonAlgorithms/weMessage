# weMessage App ProGuard Rules
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


##----------------------  Commons  ----------------------

-keepnames class scott.wemessage.commons.connection.**
-keep,allowobfuscation class scott.wemessage.commons.** { *; }

-keep class scott.wemessage.commons.connection.Heartbeat { *; }
-keep class scott.wemessage.commons.connection.security.EncryptedFile { *; }
-keep class scott.wemessage.commons.connection.security.EncryptedText { *; }

##--------------------------------------------------------


##----------------------  ChatKit  -----------------------

-keepclassmembers class * extends com.stfalcon.chatkit.commons.ViewHolder {
   public <init>(android.view.View);
}

##--------------------------------------------------------


##-----------------------  SMSKit  -----------------------

-dontwarn com.android.mms.**
-dontwarn android.net.**

##--------------------------------------------------------


##------------------------------  GSON  ------------------------------

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##--------------------------------------------------------------------


##----------------------------  JodaTime  ----------------------------

-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**

##--------------------------------------------------------------------


##------------------------------  Glide  -----------------------------

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

##--------------------------------------------------------------------


##------------------------------  OkHttp  -----------------------------

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

##--------------------------------------------------------------------


##------------------------  Shape Image View  ------------------------

-keep class com.github.siyamed.shapeimageview.**{ *; }
-dontwarn com.github.siyamed.**

##--------------------------------------------------------------------


##------------------------  Shortcut Badger  ------------------------

-keep public class me.leolin.shortcutbadger.**
-keepclassmembers public class me.leolin.shortcutbadger.** {
    *;
}

#---------------------------------------------------------------------