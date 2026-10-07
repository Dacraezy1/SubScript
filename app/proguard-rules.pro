# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/user/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Subscription entity
-keep class com.dacraezy1.subscript.data.model.** { *; }
