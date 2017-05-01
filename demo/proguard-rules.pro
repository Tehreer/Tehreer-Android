# Maintain enums.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Maintain all fields in R class.
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Maintain native methods.
-keepclassmembers, includedescriptorclasses class * {
    native <methods>;
}

# Maintain sustain annotation.
-keep @interface com.mta.tehreer.internal.Sustain

# Maintain explicitly marked classes, methods and variables.
-keepclassmembers class * {
    @com.mta.tehreer.internal.Sustain *;
}
