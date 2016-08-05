package com.lody.virtual.helper.loaders;

import android.os.Build.VERSION;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

class ClassLoaderInjectHelper {

    private static final String DEX_SUFFIX = ".dex";
    private static Class ElementClass = null;
    private static final String TAG = ClassLoaderInjectHelper.class.getSimpleName();
    private static final String ZIP_SEPARATOR = "!/";


    private static final class V14 {
        private V14() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Object dexPathList = ClassLoaderInjectHelper.findField(loader, "pathList").get(loader);
            ClassLoaderInjectHelper.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList<>(additionalClassPathEntries), optimizedDirectory));
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            try {
                return ClassLoaderInjectHelper.makePathElements(files, optimizedDirectory, null);
            } catch (Exception e) {
                e.printStackTrace();
                return (Object[]) ClassLoaderInjectHelper.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class).invoke(dexPathList, files, optimizedDirectory);
            }
        }
    }

    private static final class V19 {
        private V19() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Object dexPathList = ClassLoaderInjectHelper.findField(loader, "pathList").get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            ClassLoaderInjectHelper.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList<>(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException suppressedException : suppressedExceptions) {
                    suppressedException.printStackTrace();
                }
                Field suppressedExceptionsField = ClassLoaderInjectHelper.findField(loader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions = (IOException[]) suppressedExceptionsField.get(loader);
                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions = suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[(suppressedExceptions.size() + dexElementsSuppressedExceptions.length)];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined, suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }
                suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
            }
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            try {
                return ClassLoaderInjectHelper.makePathElements(files, optimizedDirectory, suppressedExceptions);
            } catch (Exception e) {
                e.printStackTrace();
                return (Object[]) ClassLoaderInjectHelper.findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class, ArrayList.class).invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
            }
        }
    }

    private static final class V23 {
        private V23() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Object dexPathList = ClassLoaderInjectHelper.findField(loader, "pathList").get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            ClassLoaderInjectHelper.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList<>(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException suppressedException : suppressedExceptions) {
                   suppressedException.printStackTrace();
                }
                Field suppressedExceptionsField = ClassLoaderInjectHelper.findField(loader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions = (IOException[]) suppressedExceptionsField.get(loader);
                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions = suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[(suppressedExceptions.size() + dexElementsSuppressedExceptions.length)];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined, suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }
                suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
            }
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            try {
                return ClassLoaderInjectHelper.makePathElements(files, optimizedDirectory, suppressedExceptions);
            } catch (Exception e) {
                e.printStackTrace();
                return (Object[]) ClassLoaderInjectHelper.findMethod(dexPathList, "makePathElements", List.class, File.class, List.class).invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
            }
        }
    }

    private static final class V4 {
        private V4() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, IOException {
            int extraSize = additionalClassPathEntries.size();
            Field pathField = ClassLoaderInjectHelper.findField(loader, "path");
            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            ListIterator<File> iterator = additionalClassPathEntries.listIterator();
            while (iterator.hasNext()) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                extraDexs[index] = DexFile.loadDex(entryPath, entryPath + "@classes.dex", 0);
            }
            pathField.set(loader, path.toString());
            ClassLoaderInjectHelper.expandFieldArray(loader, "mPaths", extraPaths);
            ClassLoaderInjectHelper.expandFieldArray(loader, "mFiles", extraFiles);
            ClassLoaderInjectHelper.expandFieldArray(loader, "mZips", extraZips);
            ClassLoaderInjectHelper.expandFieldArray(loader, "mDexs", extraDexs);
        }
    }

    ClassLoaderInjectHelper() {
    }

    static void installDexes(ClassLoader loader, File dexDir, List<File> files) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
        if (!files.isEmpty()) {
            if (VERSION.SDK_INT >= 23) {
                V23.install(loader, files, dexDir);
            } else if (VERSION.SDK_INT >= 19) {
                V19.install(loader, files, dexDir);
            } else if (VERSION.SDK_INT >= 14) {
                V14.install(loader, files, dexDir);
            } else {
                V4.install(loader, files);
            }
        }
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }

    private static void expandFieldArray(Object instance, String fieldName, Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static Class clazz() throws ClassNotFoundException {
        if (ElementClass == null) {
            ElementClass = Class.forName("dalvik.system.DexPathList$Element");
        }
        return ElementClass;
    }

    private static Object newElement(File dir, boolean isDirectory, File zip, DexFile dexFile) throws Exception {
        Class<?> clazz = clazz();
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(File.class, Boolean.TYPE, File.class, DexFile.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(dir, isDirectory, zip, dexFile);
        } catch (Exception e) {
            try {
                constructor = clazz.getConstructor(File.class, ZipFile.class, DexFile.class);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(dir, new ZipFile(zip), dexFile);
            } catch (Exception err) {
                constructor = clazz.getConstructor(File.class, File.class, DexFile.class);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(dir, zip, dexFile);
            }
        }
    }

    private static Object[] makePathElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions) throws Exception {
        List<Object> elements = new ArrayList<>();
        for (File file : files) {
            File zip = null;
            File dir = new File("");
            DexFile dex = null;
            String path = file.getPath();
            String name = file.getName();
            if (path.contains(ZIP_SEPARATOR)) {
                String[] split = path.split(ZIP_SEPARATOR, 2);
                zip = new File(split[0]);
                dir = new File(split[1]);
            } else if (file.isDirectory()) {
                elements.add(newElement(file, true, null, null));
            } else if (!file.isFile()) {
                Log.e(TAG, "ClassLoader referenced unknown path: " + file);
            } else if (name.endsWith(DEX_SUFFIX)) {
                try {
                    dex = loadDexFile(file, optimizedDirectory);
                } catch (IOException ex) {
                    Log.e(TAG, "Unable to load dex file: " + file, ex);
                }
            } else {
                zip = file;
                try {
                    dex = loadDexFile(file, optimizedDirectory);
                } catch (IOException suppressed) {
                    suppressedExceptions.add(suppressed);
                }
            }
            if (zip != null || dex != null) {
                elements.add(newElement(dir, false, zip, dex));
            }
        }
        Object[] array = new Object[elements.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = elements.get(i);
        }
        return array;
    }

    private static DexFile loadDexFile(File file, File optimizedDirectory) throws IOException {
        if (optimizedDirectory == null) {
            return new DexFile(file);
        }
        return DexFile.loadDex(file.getPath(), optimizedPathFor(file, optimizedDirectory), 0);
    }

    private static String optimizedPathFor(File path, File optimizedDirectory) {
        String fileName = path.getPath();
        if (!fileName.endsWith(DEX_SUFFIX)) {
            fileName = getDalvikCacheFileName(fileName);
        }
        return new File(optimizedDirectory, fileName).getPath();
    }


    private static String getDalvikCacheFileName(String fileName) {
        String dexName = fileName.replaceAll(File.separator, "@");
        if (dexName.startsWith("@")) {
            dexName = dexName.substring(1);
        }
        return dexName + "@classes.dex";
    }
}