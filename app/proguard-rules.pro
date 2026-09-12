# CircleKeep ProGuard Rules

# 1. Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.migration.Migration
-keep @androidx.room.Entity class *
-keep class com.circlekeep.data.AppDatabaseConstructor_Impl { *; }
-keep class com.circlekeep.data.AppDatabaseConstructor { *; }
-keep class com.circlekeep.data.**_Impl { *; }

# 2. Kotlin Serialization (Crucial for Navigation and Backup)
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.circlekeep.navigation.Destination** {
    *** Companion;
    *** $serializer;
}
-keepclassmembers class com.circlekeep.data.BackupData** {
    *** Companion;
    *** $serializer;
}
-keep class kotlinx.serialization.json.Json { *; }

# 3. Moshi (Used for Barcode/JSON)
-keep class com.circlekeep.data.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *

# 4. ViewModel and Lifecycle
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# 5. AdMob and Google Play Services
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# 6. MLKit Barcode Scanning
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_barcode_scanning.** { *; }

# 7. WorkManager (Reminders)
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.circlekeep.EventReminderWorker { *; }

# 8. General Data Integrity
# Keep all data models to prevent Room/Moshi/Serialization issues in Full Mode
-keep class com.circlekeep.data.** { *; }
-keep class com.circlekeep.navigation.** { *; }
