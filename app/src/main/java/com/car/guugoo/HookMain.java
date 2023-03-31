package com.car.guugoo;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class HookMain implements IXposedHookLoadPackage {

    private static final String FAKE_APPLICATION = "FAKE.JUNK.APPLICATION";
    private static final String FAKE_COMMAND = "FAKEJUNKCOMMAND";
    private static final String FAKE_FILE = "FAKEJUNKFILE";
    private static final String FAKE_PACKAGE = "FAKE.JUNK.PACKAGE";
    private Set<String> appSet;
    private Set<String> commandSet;
    private boolean debugPref;
    private boolean isRootCloakLoadingPref = false;
    private Set<String> keywordSet;
    private Set<String> libnameSet;



    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        this.isRootCloakLoadingPref = true;
        Set<String> tmpAppSet = loadSetFromPrefs(Common.APPS);
        if (!tmpAppSet.contains(lpparam.packageName)) {
            this.isRootCloakLoadingPref = false;
        } else {
            this.appSet = tmpAppSet;
            this.keywordSet = loadSetFromPrefs(Common.KEYWORDS);
            this.commandSet = loadSetFromPrefs(Common.COMMANDS);
            this.libnameSet = loadSetFromPrefs(Common.LIBRARIES);
            initSettings();
            this.isRootCloakLoadingPref = false;
            if (this.debugPref) {
                XposedBridge.log("Loaded app: " + lpparam.packageName);
            }
            initOther(lpparam);
            initFile(lpparam);
            initPackageManager(lpparam);
            initActivityManager(lpparam);
            initRuntime(lpparam);
            initProcessBuilder(lpparam);
            initSettingsGlobal(lpparam);
        }
        if (lpparam.packageName.equals("com.guugoo.jiapeiteacher")) {
            XposedHelpers.findAndHookMethod("com.stub.StubApp", lpparam.classLoader, "attachBaseContext", new Object[]{Context.class, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.1
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) param.args[0];
                    ClassLoader classLoader = context.getClassLoader();
                    XposedHelpers.findAndHookMethod("com.baidu.idl.face.platform.decode.FaceModule", classLoader, "getDetectBestImageList", new Object[]{new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.1.1
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param2) throws Throwable {
                            super.afterHookedMethod(param2);
                            ArrayList<String> ce = (ArrayList) param2.getResult();
                            try {
                                File file1 = new File(Environment.getExternalStorageDirectory(), "sansanmm");
                                BufferedReader br1 = new BufferedReader(new FileReader(file1));
                                StringBuffer sb1 = new StringBuffer();
                                while (true) {
                                    String readline1 = br1.readLine();
                                    if (readline1 == null) {
                                        break;
                                    }
                                    sb1.append(readline1);
                                }
                                br1.close();
                                ce.set(0, sb1.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            param2.setResult(ce);
                        }
                    }});
                }
            }});
        }
    }

    private void initOther(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.os.Debug", lpparam.classLoader, "isDebuggerConnected", new Object[]{XC_MethodReplacement.returnConstant(false)});
        if (!Build.TAGS.equals("release-keys")) {
            if (this.debugPref) {
                XposedBridge.log("Original build tags: " + Build.TAGS);
            }
            XposedHelpers.setStaticObjectField(Build.class, "TAGS", "release-keys");
            if (this.debugPref) {
                XposedBridge.log("New build tags: " + Build.TAGS);
            }
        } else if (this.debugPref) {
            XposedBridge.log("No need to change build tags: " + Build.TAGS);
        }
        XposedHelpers.findAndHookMethod("android.os.SystemProperties", lpparam.classLoader, "get", new Object[]{String.class, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.2
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (((String) param.args[0]).equals("ro.build.selinux")) {
                    param.setResult("1");
                    if (debugPref) {
                        XposedBridge.log("SELinux is enforced.");
                    }
                }
            }
        }});
        XposedHelpers.findAndHookMethod("java.lang.Class", lpparam.classLoader, "forName", new Object[]{String.class, Boolean.TYPE, ClassLoader.class, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.3
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String classname = (String) param.args[0];
                if (classname != null) {
                    if (classname.equals("de.robv.android.xposed.XposedBridge") || classname.equals("de.robv.android.xposed.XC_MethodReplacement")) {
                        param.setThrowable(new ClassNotFoundException());
                        if (debugPref) {
                            XposedBridge.log("Found and hid Xposed class name: " + classname);
                        }
                    }
                }
            }
        }});
    }

    private void initFile(XC_LoadPackage.LoadPackageParam lpparam) {
        Constructor<?> constructLayoutParams = XposedHelpers.findConstructorExact(File.class, new Class[]{String.class});
        XposedBridge.hookMethod(constructLayoutParams, new XC_MethodHook(10000) { // from class: com.example.a.xphook.RootCloak.4
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[0] != null && debugPref) {
                    XposedBridge.log("File: Found a File constructor: " + ((String) param.args[0]));
                }
                if (isRootCloakLoadingPref) {
                    return;
                }
                if (((String) param.args[0]).endsWith("su")) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor ending with su");
                    }
                    param.args[0] = "/system/xbin/FAKEJUNKFILE";
                } else if (((String) param.args[0]).endsWith("busybox")) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor ending with busybox");
                    }
                    param.args[0] = "/system/xbin/FAKEJUNKFILE";
                } else if (stringContainsFromSet((String) param.args[0], keywordSet)) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor with word super, noshufou, or chainfire");
                    }
                    param.args[0] = "/system/app/FAKEJUNKFILE.apk";
                }
            }
        });
        Constructor<?> extendedFileConstructor = XposedHelpers.findConstructorExact(File.class, new Class[]{String.class, String.class});
        XposedBridge.hookMethod(extendedFileConstructor, new XC_MethodHook(10000) { // from class: com.example.a.xphook.RootCloak.5
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[0] != null && param.args[1] != null && debugPref) {
                    XposedBridge.log("File: Found a File constructor: " + ((String) param.args[0]) + ", with: " + ((String) param.args[1]));
                }
                if (isRootCloakLoadingPref) {
                    return;
                }
                if (((String) param.args[1]).equalsIgnoreCase("su")) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor with filename su");
                    }
                    param.args[1] = FAKE_FILE;
                } else if (((String) param.args[1]).contains("busybox")) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor ending with busybox");
                    }
                    param.args[1] = FAKE_FILE;
                } else if (stringContainsFromSet((String) param.args[1], keywordSet)) {
                    if (debugPref) {
                        XposedBridge.log("File: Found a File constructor with word super, noshufou, or chainfire");
                    }
                    param.args[1] = "FAKEJUNKFILE.apk";
                }
            }
        });
        Constructor<?> uriFileConstructor = XposedHelpers.findConstructorExact(File.class, new Class[]{URI.class});
        XposedBridge.hookMethod(uriFileConstructor, new XC_MethodHook(10000) { // from class: com.example.a.xphook.RootCloak.6
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[0] != null && debugPref) {
                    XposedBridge.log("File: Found a URI File constructor: " + ((URI) param.args[0]).toString());
                }
            }
        });
    }

    private void initPackageManager(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledApplications", new Object[]{Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.7
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getInstalledApplications");
                }
                List<ApplicationInfo> packages = (List) param.getResult();
                Iterator<ApplicationInfo> iter = packages.iterator();
                while (iter.hasNext()) {
                    ApplicationInfo tempAppInfo = iter.next();
                    String tempPackageName = tempAppInfo.packageName;
                    if (tempPackageName != null && stringContainsFromSet(tempPackageName, keywordSet)) {
                        iter.remove();
                        if (debugPref) {
                            XposedBridge.log("Found and hid package: " + tempPackageName);
                        }
                    }
                }
                param.setResult(packages);
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledPackages", new Object[]{Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.8
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getInstalledPackages");
                }
                List<PackageInfo> packages = (List) param.getResult();
                Iterator<PackageInfo> iter = packages.iterator();
                while (iter.hasNext()) {
                    PackageInfo tempPackageInfo = iter.next();
                    String tempPackageName = tempPackageInfo.packageName;
                    if (tempPackageName != null && stringContainsFromSet(tempPackageName, keywordSet)) {
                        iter.remove();
                        if (debugPref) {
                            XposedBridge.log("Found and hid package: " + tempPackageName);
                        }
                    }
                }
                param.setResult(packages);
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getPackageInfo", new Object[]{String.class, Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.9
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getPackageInfo");
                }
                String name = (String) param.args[0];
                if (name != null && stringContainsFromSet(name, keywordSet)) {
                    param.args[0] = FAKE_PACKAGE;
                    if (debugPref) {
                        XposedBridge.log("Found and hid package: " + name);
                    }
                }
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getApplicationInfo", new Object[]{String.class, Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.10
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String name = (String) param.args[0];
                if (debugPref) {
                    XposedBridge.log("Hooked getApplicationInfo : " + name);
                }
                if (name != null && stringContainsFromSet(name, keywordSet)) {
                    param.args[0] = FAKE_APPLICATION;
                    if (debugPref) {
                        XposedBridge.log("Found and hid application: " + name);
                    }
                }
            }
        }});
    }

    private void initActivityManager(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningServices", new Object[]{Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.11
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getRunningServices");
                }
                List<ActivityManager.RunningServiceInfo> services = (List) param.getResult();
                Iterator<ActivityManager.RunningServiceInfo> iter = services.iterator();
                while (iter.hasNext()) {
                    ActivityManager.RunningServiceInfo tempService = iter.next();
                    String tempProcessName = tempService.process;
                    if (tempProcessName != null && stringContainsFromSet(tempProcessName, keywordSet)) {
                        iter.remove();
                        if (debugPref) {
                            XposedBridge.log("Found and hid service: " + tempProcessName);
                        }
                    }
                }
                param.setResult(services);
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningTasks", new Object[]{Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.12
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getRunningTasks");
                }
                List<ActivityManager.RunningTaskInfo> services = (List) param.getResult();
                Iterator<ActivityManager.RunningTaskInfo> iter = services.iterator();
                while (iter.hasNext()) {
                    ActivityManager.RunningTaskInfo tempTask = iter.next();
                    String tempBaseActivity = tempTask.baseActivity.flattenToString();
                    if (tempBaseActivity != null && stringContainsFromSet(tempBaseActivity, keywordSet)) {
                        iter.remove();
                        if (debugPref) {
                            XposedBridge.log("Found and hid BaseActivity: " + tempBaseActivity);
                        }
                    }
                }
                param.setResult(services);
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader, "getRunningAppProcesses", new Object[]{new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.13
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked getRunningAppProcesses");
                }
                List<ActivityManager.RunningAppProcessInfo> processes = (List) param.getResult();
                Iterator<ActivityManager.RunningAppProcessInfo> iter = processes.iterator();
                while (iter.hasNext()) {
                    ActivityManager.RunningAppProcessInfo tempProcess = iter.next();
                    String tempProcessName = tempProcess.processName;
                    if (tempProcessName != null && stringContainsFromSet(tempProcessName, keywordSet)) {
                        iter.remove();
                        if (debugPref) {
                            XposedBridge.log("Found and hid process: " + tempProcessName);
                        }
                    }
                }
                param.setResult(processes);
            }
        }});
    }

    private void initRuntime(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("java.lang.Runtime", lpparam.classLoader, "exec", new Object[]{String[].class, String[].class, File.class, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.14
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String[] strArr;
                if (debugPref) {
                    XposedBridge.log("Hooked Runtime.exec");
                }
                String[] execArray = (String[]) param.args[0];
                if (execArray == null || execArray.length < 1) {
                    if (debugPref) {
                        XposedBridge.log("Null or empty array on exec");
                        return;
                    }
                    return;
                }
                String firstParam = execArray[0];
                if (debugPref) {
                    String tempString = "Exec Command:";
                    for (String temp : execArray) {
                        tempString = tempString + " " + temp;
                    }
                    XposedBridge.log(tempString);
                }
                if (stringEndsWithFromSet(firstParam, commandSet)) {
                    if (debugPref) {
                        XposedBridge.log("Found blacklisted command at the end of the string: " + firstParam);
                    }
                    if (!firstParam.equals("su") && !firstParam.endsWith("/su")) {
                        if (!commandSet.contains("pm") || (!firstParam.equals("pm") && !firstParam.endsWith("/pm"))) {
                            if (!commandSet.contains("ps") || (!firstParam.equals("ps") && !firstParam.endsWith("/ps"))) {
                                if (!commandSet.contains("which") || (!firstParam.equals("which") && !firstParam.endsWith("/which"))) {
                                    if (!commandSet.contains("busybox") || !anyWordEndingWithKeyword("busybox", execArray).booleanValue()) {
                                        if (commandSet.contains("sh") && (firstParam.equals("sh") || firstParam.endsWith("/sh"))) {
                                            param.setThrowable(new IOException());
                                        } else {
                                            param.setThrowable(new IOException());
                                        }
                                    } else {
                                        param.setThrowable(new IOException());
                                    }
                                } else {
                                    param.setThrowable(new IOException());
                                }
                            } else {
                                param.args[0] = buildGrepArraySingle(execArray, true);
                            }
                        } else if (execArray.length >= 3 && execArray[1].equalsIgnoreCase("list") && execArray[2].equalsIgnoreCase("packages")) {
                            param.args[0] = buildGrepArraySingle(execArray, true);
                        } else if (execArray.length >= 3 && ((execArray[1].equalsIgnoreCase("dump") || execArray[1].equalsIgnoreCase("path")) && stringContainsFromSet(execArray[2], keywordSet))) {
                            Object[] objArr = param.args;
                            String[] strArr2 = new String[3];
                            strArr2[0] = execArray[0];
                            strArr2[1] = execArray[1];
                            strArr2[2] = FAKE_PACKAGE;
                            objArr[0] = strArr2;
                        }
                    } else {
                        param.setThrowable(new IOException());
                    }
                    if (debugPref && param.getThrowable() == null) {
                        String tempString2 = "New Exec Command:";
                        for (String temp2 : (String[]) param.args[0]) {
                            tempString2 = tempString2 + " " + temp2;
                        }
                        XposedBridge.log(tempString2);
                    }
                }
            }
        }});
        XposedHelpers.findAndHookMethod("java.lang.Runtime", lpparam.classLoader, "loadLibrary", new Object[]{String.class, ClassLoader.class, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.15
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (debugPref) {
                    XposedBridge.log("Hooked loadLibrary");
                }
                String libname = (String) param.args[0];
                if (libname != null && stringContainsFromSet(libname, libnameSet)) {
                    param.setResult((Object) null);
                    if (debugPref) {
                        XposedBridge.log("Loading of library " + libname + " disabled.");
                    }
                }
            }
        }});
    }

    private void initProcessBuilder(XC_LoadPackage.LoadPackageParam lpparam) {
        Constructor<?> processBuilderConstructor2 = XposedHelpers.findConstructorExact(ProcessBuilder.class, new Class[]{String[].class});
        XposedBridge.hookMethod(processBuilderConstructor2, new XC_MethodHook(10000) { // from class: com.example.a.xphook.RootCloak.16
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String[] strArr;
                XposedBridge.log("Hooked ProcessBuilder");
                if (param.args[0] != null) {
                    String[] cmdArray = (String[]) param.args[0];
                    if (debugPref) {
                        String tempString = "ProcessBuilder Command:";
                        for (String temp : cmdArray) {
                            tempString = tempString + " " + temp;
                        }
                        XposedBridge.log(tempString);
                    }
                    if (stringEndsWithFromSet(cmdArray[0], commandSet)) {
                        cmdArray[0] = FAKE_COMMAND;
                        param.args[0] = cmdArray;
                    }
                    if (debugPref) {
                        String tempString2 = "New ProcessBuilder Command:";
                        for (String temp2 : (String[]) param.args[0]) {
                            tempString2 = tempString2 + " " + temp2;
                        }
                        XposedBridge.log(tempString2);
                    }
                }
            }
        });
    }

    private void initSettingsGlobal(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(Settings.Global.class, "getInt", new Object[]{ContentResolver.class, String.class, Integer.TYPE, new XC_MethodHook() { // from class: com.example.a.xphook.RootCloak.17
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String setting = (String) param.args[1];
                if (setting == null) {
                    return;
                }
                if ("adb_enabled".equals(setting)) {
                    param.setResult(0);
                    if (debugPref) {
                        XposedBridge.log("Hooked ADB debugging info, adb status is off");
                    }
                }
                if ("development_settings_enabled".equals(setting)) {
                    param.setResult(0);
                    if (debugPref) {
                        XposedBridge.log("Hooked development options info, development options status is off");
                    }
                }
            }
        }});
    }

    private void initSettings() {
        XSharedPreferences prefSettings = new XSharedPreferences(BuildConfig.APPLICATION_ID, Common.PREFS_SETTINGS);
        prefSettings.makeWorldReadable();
        this.debugPref = prefSettings.getBoolean(Common.DEBUG_KEY, false);
    }

    private static Set<String> loadSetFromPrefs(Common.PrefSet type) {
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old).permitDiskReads().permitDiskWrites().build());
        Set<String> newSet = new HashSet<>();
        try {
            XSharedPreferences loadedPrefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, type.getPrefKey());
            loadedPrefs.makeWorldReadable();
            boolean isFirstRun = loadedPrefs.getBoolean(Common.FIRST_RUN_KEY, true);
            Set<String> loadedSet = loadedPrefs.getStringSet(type.getSetKey(), (Set) null);
            if (loadedSet != null) {
                newSet.addAll(loadedSet);
            } else if (isFirstRun) {
                newSet.addAll(type.getDefaultSet());
            }
            return newSet;
        } finally {
            StrictMode.setThreadPolicy(old);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Boolean anyWordEndingWithKeyword(String keyword, String[] wordArray) {
        for (String tempString : wordArray) {
            if (tempString.endsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    public boolean stringContainsFromSet(String base, Set<String> values) {
        if (base != null && values != null) {
            for (String tempString : values) {
                if (base.matches(".*(\\W|^)" + tempString + "(\\W|$).*")) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean stringEndsWithFromSet(String base, Set<String> values) {
        if (base != null && values != null) {
            for (String tempString : values) {
                if (base.endsWith(tempString)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String[] buildGrepArraySingle(String[] original, boolean addSH) {
        StringBuilder builder = new StringBuilder();
        ArrayList<String> originalList = new ArrayList<>();
        if (addSH) {
            originalList.add("sh");
            originalList.add("-c");
        }
        for (String temp : original) {
            builder.append(" ");
            builder.append(temp);
        }
        for (String temp2 : this.keywordSet) {
            builder.append(" | grep -v ");
            builder.append(temp2);
        }
        originalList.add(builder.toString());
        return (String[]) originalList.toArray(new String[0]);
    }


}
