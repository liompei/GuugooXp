package com.car.guugoo;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceActivity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Common {

    public static final PrefSet APPS = new AppsSet();

    public static final PrefSet COMMANDS;

    public static final String DEBUG_KEY = "debug_logs";

    public static final String FIRST_RUN_KEY = "com.devadvance.rootcloak2IS_FIRST_RUN";

    public static final PrefSet KEYWORDS = new KeywordSet();

    public static final PrefSet LIBRARIES;

    public static final String PACKAGE_NAME = "com.devadvance.rootcloak2";

    public static final String PREFS_SETTINGS = "CustomizeSettings";

    public static final String REFRESH_APPS_INTENT = "com.devadvance.rootcloak2.REFRESH_APPS";

    public static final String SHOW_WARNING = "SHOW_WARNING";

    static {
        COMMANDS = new CommandSet();
        LIBRARIES = new LibrarySet();
    }

    public static boolean isUserApp(ApplicationInfo paramApplicationInfo) {
        return "com.example.a.xphook".equals(paramApplicationInfo.packageName) ? false : (!((paramApplicationInfo.flags & 0x1) != 0));
    }

    public static class AppsSet extends PrefSet {
        public static final String APP_SET_KEY = "com.devadvance.rootcloak2APPS_LIST";

        public static final Set<String> DEFAULT_APPS_SET = new HashSet<String>(Arrays.asList(DefaultLists.DEFAULT_APPS_LIST));

        public static final String PREFS_APPS = "CustomizeApps";

        public Set<String> getDefaultSet() {
            return DEFAULT_APPS_SET;
        }

        public String getPrefKey() {
            return "CustomizeApps";
        }

        public String getSetKey() {
            return "com.devadvance.rootcloak2APPS_LIST";
        }
    }

    public static class CommandSet extends PrefSet {
        public static final String COMMAND_SET_KEY = "com.devadvance.rootcloak2APPS_SET";

        public static final Set<String> DEFAULT_COMMAND_SET = new HashSet<String>(Arrays.asList(DefaultLists.DEFAULT_COMMAND_LIST));

        public static final String PREFS_COMMANDS = "CustomizeCommands";

        public Set<String> getDefaultSet() {
            return DEFAULT_COMMAND_SET;
        }

        public String getPrefKey() {
            return "CustomizeCommands";
        }

        public String getSetKey() {
            return "com.devadvance.rootcloak2APPS_SET";
        }
    }

    public static class KeywordSet extends PrefSet {
        public static final Set<String> DEFAULT_KEYWORD_SET = new HashSet<String>(Arrays.asList(DefaultLists.DEFAULT_KEYWORD_LIST));

        public static final String KEYWORD_SET_KEY = "com.devadvance.rootcloak2KEYWORD_SET";

        public static final String PREFS_KEYWORDS = "CustomizeKeywords";

        public Set<String> getDefaultSet() {
            return DEFAULT_KEYWORD_SET;
        }

        public String getPrefKey() {
            return "CustomizeKeywords";
        }

        public String getSetKey() {
            return "com.devadvance.rootcloak2KEYWORD_SET";
        }
    }

    public static class LibrarySet extends PrefSet {
        public static final Set<String> DEFAULT_LIBNAME_SET = new HashSet<String>(Arrays.asList(DefaultLists.DEFAULT_LIBNAME_LIST));

        public static final String LIBRARY_SET_KEY = "LIBNAMES_SET";

        public static final String PREFS_LIBNAMES = "CustomizeLibnames";

        public Set<String> getDefaultSet() {
            return DEFAULT_LIBNAME_SET;
        }

        public String getPrefKey() {
            return "CustomizeLibnames";
        }

        public String getSetKey() {
            return "LIBNAMES_SET";
        }
    }

    public static abstract class PrefSet {
        abstract Set<String> getDefaultSet();

        abstract String getPrefKey();

        abstract String getSetKey();

        @SuppressLint({"WorldReadableFiles"})
        public SharedPreferences getSharedPreferences(PreferenceActivity param1PreferenceActivity) {
            param1PreferenceActivity.getPreferenceManager().setSharedPreferencesMode(1);
            return param1PreferenceActivity.getSharedPreferences(getPrefKey(), 1);
        }
    }

}
