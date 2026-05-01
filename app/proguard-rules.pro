# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep IR Manager and related classes
-keep class com.example.universalirremote.ir.** { *; }
-keep class com.example.universalirremote.model.** { *; }
-keep class com.example.universalirremote.data.** { *; }

# Keep ConsumerIrManager
-keep class android.hardware.ConsumerIrManager { *; }
-keep class android.hardware.ConsumerIrManager$CarrierFrequencyRange { *; }
