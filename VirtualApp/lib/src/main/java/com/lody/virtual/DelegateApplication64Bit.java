package com.lody.virtual;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Lody
 *         <p>
 *         <p>
 *         Copy the file to your Project.
 */
@TargetApi(Build.VERSION_CODES.M)
public abstract class DelegateApplication64Bit extends Application {

    private Application mTarget;

    protected abstract String get32BitPackageName();


    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);


                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }


    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }


    private static void expandFieldList(Object instance, String fieldName, Object[] extraElements) throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(instance, fieldName);
        Object[] original = ((List) field.get(instance)).toArray();
        Object[] combined = (Object[]) Array.newInstance(original.getClass().getComponentType(), original.length + 1);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, 1);
        field.set(instance, Arrays.asList(combined));
    }

    private static Object[] makeDexElements(
            Object dexPathList, ArrayList<File> files,
            ArrayList<IOException> suppressedExceptions)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Method makeDexElements;
        if (Build.VERSION.SDK_INT >= 23) {
            makeDexElements = findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
        } else {
            makeDexElements = findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class, ArrayList.class);
        }
        return (Object[]) makeDexElements.invoke(dexPathList, files, null,
                suppressedExceptions);

    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(get32BitPackageName(), 0);
            ClassLoader classLoader = getClassLoader();
            Object dexPathList = findField(classLoader, "pathList").get(classLoader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            ArrayList<File> dexFiles = new ArrayList<>();
            dexFiles.add(new File(ai.publicSourceDir));
            ArrayList<File> nativeLibs = new ArrayList<>();
            nativeLibs.add(new File(ai.nativeLibraryDir));
            if (Build.VERSION.SDK_INT > 25) {
                expandFieldList(dexPathList, "nativeLibraryDirectories", new File[]{new File(ai.nativeLibraryDir)});
                expandFieldArray(dexPathList, "nativeLibraryPathElements",
                        (Object[]) findMethod(dexPathList, "makePathElements", List.class).invoke(dexPathList, nativeLibs));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                expandFieldList(dexPathList, "nativeLibraryDirectories", new File[]{new File(ai.nativeLibraryDir)});
                expandFieldArray(dexPathList, "nativeLibraryPathElements", makeDexElements(dexPathList, nativeLibs, suppressedExceptions));
            } else {
                expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{new File(ai.nativeLibraryDir)});
            }
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, dexFiles, suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(getClass().getSimpleName(), "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField =
                        findField(classLoader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                        (IOException[]) suppressedExceptionsField.get(classLoader);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                            suppressedExceptions.toArray(
                                    new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                            new IOException[suppressedExceptions.size() +
                                    dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }
                suppressedExceptionsField.set(classLoader, dexElementsSuppressedExceptions);
            }
            mTarget = (Application) classLoader.loadClass(ai.className).newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (mTarget != null) {
            mTarget.onConfigurationChanged(configuration);
        }
    }

    public void onCreate() {
        super.onCreate();
        if (mTarget != null) {
            mTarget.onCreate();
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (mTarget != null) {
            mTarget.onLowMemory();
        }
    }

    public void onTerminate() {
        super.onTerminate();
        if (mTarget != null) {
            mTarget.onTerminate();
        }
    }

    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        if (mTarget != null) {
            mTarget.onTrimMemory(i);
        }
    }

}
