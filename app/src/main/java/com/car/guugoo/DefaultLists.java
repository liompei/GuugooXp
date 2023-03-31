package com.car.guugoo;

public class DefaultLists {

    public static final String[] DEFAULT_APPS_LIST = new String[] { "com.guugoo.jiapeiteacher" };

    public static final String[] DEFAULT_COMMAND_LIST;

    public static final String[] DEFAULT_KEYWORD_LIST = new String[] {
            "supersu", "superuser", "Superuser", "noshufou", "xposed", "rootcloak", "chainfire", "titanium", "Titanium", "substrate",
            "greenify", "daemonsu", "root", "busybox", "titanium", ".tmpsu", "su", "rootcloak2" };

    public static final String[] DEFAULT_LIBNAME_LIST;

    static {
        DEFAULT_COMMAND_LIST = new String[] { "su", "which", "busybox", "pm", "am", "sh", "ps" };
        DEFAULT_LIBNAME_LIST = new String[0];
    }

}
