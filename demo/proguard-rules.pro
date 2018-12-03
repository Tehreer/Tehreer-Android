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
