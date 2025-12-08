# ============================================
# PROGUARD RULES - Food Finder App
# ============================================

# ===== FIREBASE =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Firebase Realtime Database
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
}
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Analytics & Crashlytics
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }

# ===== DATA CLASSES (MODELE) =====
-keep class com.example.sharoma_finder.domain.** { *; }
-keepclassmembers class com.example.sharoma_finder.domain.** {
    <init>(...);
    <fields>;
}

# ===== COIL (IMAGE LOADING) =====
-dontwarn coil.**
-keep class coil.** { *; }
-keep interface coil.** { *; }

# OkHttp (used by Coil)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ===== GOOGLE MAPS =====
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===== KOTLIN =====
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# ===== JETPACK COMPOSE =====
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.compose.**

# Compose Runtime
-keepclassmembers class androidx.compose.runtime.** {
    <methods>;
}

# ===== ANDROIDX =====
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ===== GSON (COMENTAT - NU ESTE INSTALAT) =====
# Decomenteaza liniile de mai jos doar daca instalezi com.google.code.gson:gson
# -keepattributes Signature
# -keepattributes *Annotation*
# -dontwarn sun.misc.**
# -keep class com.google.gson.** { *; }
# -keep class * extends com.google.gson.TypeAdapter
# -keep class * implements com.google.gson.TypeAdapterFactory
# -keep class * implements com.google.gson.JsonSerializer
# -keep class * implements com.google.gson.JsonDeserializer

# ===== RETROFIT (COMENTAT - NU ESTE INSTALAT) =====
# Decomenteaza daca instalezi Retrofit pe viitor
# -keepattributes Signature, InnerClasses, EnclosingMethod
# -keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
# -keepclassmembers,allowshrinking,allowobfuscation interface * {
#    @retrofit2.http.* <methods>;
# }
# -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreRequirement
# -dontwarn javax.annotation.**
# -dontwarn kotlin.Unit
# -dontwarn retrofit2.-KotlinExtensions

# ===== GENERAL RULES =====
# Keep line numbers for crash reports (important for Crashlytics!)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# Prevent crashes from missing classes
-dontwarn java.lang.invoke.StringConcatFactory

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== DEBUGGING =====
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}