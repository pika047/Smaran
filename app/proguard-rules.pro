# Strip verbose, debug and info logging from release builds so note text can never
# reach the system log by accident. Warnings and errors are kept for crash reports.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
