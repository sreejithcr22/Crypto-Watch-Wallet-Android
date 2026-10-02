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
# debugging stack traces (needed to de-obfuscate Play Console crashes).
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Gson: API DTOs are parsed via reflection. Keep the models and
# their SerializedName annotations so minified release builds
# parse network responses correctly.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keepattributes RuntimeVisibleParameterAnnotations, RuntimeVisibleTypeAnnotations
-keep class com.codit.cryptowatchwallet.model.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Gson's own reflective helpers.
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# --- App code: Hilt (ViewModels/Workers), Room (DAOs/Entities),
# Retrofit (service interfaces) and Compose screens use reflection /
# code-gen. Keep all app classes to prevent R8 from stripping or
# renaming anything referenced via Hilt/Room/Retrofit at runtime.
# (Narrower than disabling minify entirely: libraries are still shrunk.)
-keep class com.codit.cryptowatchwallet.** { *; }
-keepnames class com.codit.cryptowatchwallet.** { *; }

# --- Retrofit service methods: keep annotations for dynamic Proxy.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --- ZXing (QR scanner): no bundled consumer rules, keep explicitly.
-keep class com.journeyapps.** { *; }
-dontwarn com.journeyapps.**
