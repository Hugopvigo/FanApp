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

-keep,includedescriptorclasses class com.mediatracker.data.remote.**$$serializer { *; }
-keepclassmembers class com.mediatracker.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.mediatracker.data.remote.** {
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
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

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
