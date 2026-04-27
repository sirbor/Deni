# Keep core annotations and runtime metadata used by reflection/serialization.
-keepattributes Signature,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault,EnclosingMethod,InnerClasses

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-dontwarn dagger.hilt.internal.**

# Retrofit / OkHttp / Gson
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Kotlin coroutines internals often generate warnings under shrink.
-dontwarn kotlinx.coroutines.internal.**

# WorkManager
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class androidx.work.impl.WorkDatabase { *; }

# Keep app models under persistence/network layers.
-keep class com.loki.deni.data.local.entity.** { *; }
-keep class com.loki.deni.data.remote.** { *; }