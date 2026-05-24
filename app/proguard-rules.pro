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
