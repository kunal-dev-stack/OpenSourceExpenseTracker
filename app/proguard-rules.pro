# Add project specific ProGuard rules here.
-dontwarn java.lang.invoke.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
}
