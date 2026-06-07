# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep kotlinx.serialization (BooksDto, TmdbDto, etc.)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes in data.remote (DTOs)
-keep,includedescriptorclasses class com.mediatracker.data.remote.**$$serializer { *; }
-keepclassmembers class com.mediatracker.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.mediatracker.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable navigation route classes (type-safe args)
-keep,includedescriptorclasses class com.mediatracker.presentation.navigation.**$$serializer { *; }
-keepclassmembers class com.mediatracker.presentation.navigation.** {
    *** Companion;
}
-keepclasseswithmembers class com.mediatracker.presentation.navigation.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable CSV import models
-keep,includedescriptorclasses class com.mediatracker.data.csvimport.**$$serializer { *; }
-keepclassmembers class com.mediatracker.data.csvimport.** {
    *** Companion;
}
-keepclasseswithmembers class com.mediatracker.data.csvimport.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable domain enums used in navigation args
-keep,includedescriptorclasses class com.mediatracker.domain.model.**$$serializer { *; }
-keepclassmembers class com.mediatracker.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.mediatracker.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities and DAOs
-keep class com.mediatracker.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * implements androidx.room.TypeConverter { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Glance widgets
-keep class androidx.glance.** { *; }
-keep class com.mediatracker.presentation.widget.** { *; }

# Keep Vico charts
-keep class com.patrykandpatrick.vico.** { *; }

# Keep DataStore
-keep class * extends androidx.datastore.preferences.** { *; }

# Keep Coil
-dontwarn coil.**

# Keep Google Play Review
-keep class com.google.android.play.** { *; }

# General Android optimizations
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
