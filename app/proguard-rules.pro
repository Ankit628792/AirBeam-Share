# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room database, entities, and converters to prevent database schemas or reflection from breaking
-keep class com.example.data.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep model states or serialization targets
-keep class com.example.viewmodel.SelectedFileState { *; }
-keep class com.example.viewmodel.TransferSpeedMode { *; }

# Keep ZXing core classes if needed
-keep class com.google.zxing.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
