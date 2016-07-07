package com.lody.virtual.helper.loaders;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipFile;

import android.app.Application;
import android.os.Build;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * 运行时为一个已创建的DexClassLoader注入新的类加载器以及lib库。
 */
public class ClassLoaderInjectHelper {

	/**
	 * 注入jar
	 *
	 * @param aApp
	 *            {@link Application}
	 * @param aLibPath
	 *            jar文件路径
	 * @return {@link InjectResult}
	 * 
	 * 
	 */
	public static InjectResult inject(Application aApp, String aLibPath) {
		try {
			Class.forName("dalvik.system.LexClassLoader");
			return injectInAliyunOs(aApp, aLibPath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		boolean hasBaseDexClassLoader = true;
		try {
			Class.forName("dalvik.system.BaseDexClassLoader");
		} catch (ClassNotFoundException e) {
			hasBaseDexClassLoader = false;
		}
		if (!hasBaseDexClassLoader) {
			return injectBelowApiLevel14(aApp, aLibPath);
		} else {
			return injectAboveEqualApiLevel14(aApp, aLibPath);
		}
	}

	/**
	 * 注入
	 * 
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 * 
	 * 
	 */
	public static InjectResult inject(ClassLoader parentClassLoader, ClassLoader childClassLoader) {
		//
		boolean hasBaseDexClassLoader = true;
		try {
			Class.forName("dalvik.system.BaseDexClassLoader");
		} catch (ClassNotFoundException e) {
			hasBaseDexClassLoader = false;
		}
		if (!hasBaseDexClassLoader) {
			return injectBelowApiLevel14(parentClassLoader, childClassLoader);
		} else {
			return injectAboveEqualApiLevel14(parentClassLoader, childClassLoader);
		}
	}

	/**
	 * 注入
	 * 
	 * @param loader
	 *            {@link ClassLoader}
	 * @param sourceApk
	 *            插件apk文件
	 * @param optimizedDirectory
	 *            优化后的dex文件目录
	 * @param nativeLibraryDirectory
	 *            插件lib文件目录
	 * @return {@link InjectResult}
	 */
	public static InjectResult inject(ClassLoader loader, File sourceApk, File optimizedDirectory,
			File nativeLibraryDirectory) {
		InjectResult result = null;
		if (sourceApk == null) {
			result = makeInjectResult(false, new RuntimeException("Apk source file is null!"));
			return result;
		}
		List<File> files = new ArrayList<File>();
		files.add(sourceApk);
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				V23.install(loader, files, optimizedDirectory, nativeLibraryDirectory);
			} else if (Build.VERSION.SDK_INT >= 19) {
				V19.install(loader, files, optimizedDirectory, nativeLibraryDirectory);
			} else if (Build.VERSION.SDK_INT >= 14) {
				V14.install(loader, files, optimizedDirectory, nativeLibraryDirectory);
			} else {
				//
				ClassLoader classLoader = new DexClassLoader(sourceApk.getAbsolutePath(),
						optimizedDirectory.getAbsolutePath(), nativeLibraryDirectory.getAbsolutePath(), loader);
				return inject(loader, classLoader);
			}
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}

		if (result == null) {
			result = makeInjectResult(true, null);
		}

		return result;
	}

	/**
	 * 阿里云系统注入jar
	 *
	 * @param aApp
	 *            {@link Application}
	 * @param aLibPath
	 *            jar文件路径
	 * @return {@link InjectResult}
	 * 
	 */
	private static InjectResult injectInAliyunOs(Application aApp, String aLibPath) {
		InjectResult result = null;
		PathClassLoader localClassLoader = (PathClassLoader) aApp.getClassLoader();
		new DexClassLoader(aLibPath, aApp.getDir("dex", 0).getAbsolutePath(), aLibPath, localClassLoader);
		String lexFileName = new File(aLibPath).getName();
		lexFileName = lexFileName.replaceAll("\\.[a-zA-Z0-9]+", ".lex");
		try {
			Class<?> classLexClassLoader = Class.forName("dalvik.system.LexClassLoader");
			Constructor<?> constructorLexClassLoader = classLexClassLoader.getConstructor(String.class, String.class,
					String.class, ClassLoader.class);
			Object localLexClassLoader = constructorLexClassLoader.newInstance(
					aApp.getDir("dex", 0).getAbsolutePath() + File.separator + lexFileName,
					aApp.getDir("dex", 0).getAbsolutePath(), aLibPath, localClassLoader);
			Method methodLoadClass = classLexClassLoader.getMethod("loadClass", String.class);
			methodLoadClass.invoke(localLexClassLoader, "com.baidu.browser.favorite.BdInjectInvoker");
			setField(localClassLoader, PathClassLoader.class, "mPaths",
					appendArray(getField(localClassLoader, PathClassLoader.class, "mPaths"),
							getField(localLexClassLoader, classLexClassLoader, "mRawDexPath")));
			setField(localClassLoader, PathClassLoader.class, "mFiles",
					combineArray(getField(localClassLoader, PathClassLoader.class, "mFiles"),
							getField(localLexClassLoader, classLexClassLoader, "mFiles")));
			setField(localClassLoader, PathClassLoader.class, "mZips",
					combineArray(getField(localClassLoader, PathClassLoader.class, "mZips"),
							getField(localLexClassLoader, classLexClassLoader, "mZips")));
			setField(localClassLoader, PathClassLoader.class, "mLexs",
					combineArray(getField(localClassLoader, PathClassLoader.class, "mLexs"),
							getField(localLexClassLoader, classLexClassLoader, "mDexs")));
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}

		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	/**
	 * api小于14时，注入jar
	 *
	 * @param aApp
	 *            {@link Application}
	 * @param aLibPath
	 *            jar文件路径
	 * @return {@link InjectResult}
	 * 
	 * 
	 */
	private static InjectResult injectBelowApiLevel14(Application aApp, String aLibPath) {
		InjectResult result = null;
		PathClassLoader pathClassLoader = (PathClassLoader) aApp.getClassLoader();
		DexClassLoader dexClassLoader = new DexClassLoader(aLibPath, aApp.getDir("dex", 0).getAbsolutePath(), aLibPath,
				aApp.getClassLoader());

		result = injectBelowApiLevel14(pathClassLoader, dexClassLoader);

		return result;
	}

	/**
	 * api小于14时，注入
	 * 
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 */
	private static InjectResult injectBelowApiLevel14(ClassLoader parentClassLoader, ClassLoader childClassLoader) {
		InjectResult result = null;
		PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
		DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
		try {
			setField(pathClassLoader, PathClassLoader.class, "mPaths",
					appendArray(getField(pathClassLoader, PathClassLoader.class, "mPaths"),
							getField(dexClassLoader, DexClassLoader.class, "mRawDexPath")));
			setField(pathClassLoader, PathClassLoader.class, "mFiles",
					combineArray(getField(pathClassLoader, PathClassLoader.class, "mFiles"),
							getField(dexClassLoader, DexClassLoader.class, "mFiles")));
			setField(pathClassLoader, PathClassLoader.class, "mZips",
					combineArray(getField(pathClassLoader, PathClassLoader.class, "mZips"),
							getField(dexClassLoader, DexClassLoader.class, "mZips")));
			setField(pathClassLoader, PathClassLoader.class, "mDexs",
					combineArray(getField(pathClassLoader, PathClassLoader.class, "mDexs"),
							getField(dexClassLoader, DexClassLoader.class, "mDexs")));

			try {
				@SuppressWarnings("unchecked")
				ArrayList<String> libPaths = (ArrayList<String>) getField(pathClassLoader, PathClassLoader.class,
						"libraryPathElements");
				String[] libArray = (String[]) getField(dexClassLoader, DexClassLoader.class, "mLibPaths");
				Collections.addAll(libPaths, libArray);
			} catch (Exception e) {
				setField(pathClassLoader, PathClassLoader.class, "mLibPaths",
						combineArray(getField(pathClassLoader, PathClassLoader.class, "mLibPaths"),
								getField(dexClassLoader, DexClassLoader.class, "mLibPaths")));
			}
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}

		if (result == null) {
			result = makeInjectResult(true, null);
		}

		return result;
	}

	/**
	 * api大于等于14时，注入jar
	 *
	 * @param aApp
	 *            {@link Application}
	 * @param aLibPath
	 *            jar文件路径
	 * @return {@link InjectResult}
	 * 
	 * 
	 */
	private static InjectResult injectAboveEqualApiLevel14(Application aApp, String aLibPath) {
		PathClassLoader pathClassLoader = (PathClassLoader) aApp.getClassLoader();
		DexClassLoader dexClassLoader = new DexClassLoader(aLibPath, aApp.getDir("dex", 0).getAbsolutePath(), aLibPath,
				aApp.getClassLoader());

		return injectAboveEqualApiLevel14(pathClassLoader, dexClassLoader);
	}

	/**
	 * 针对api大于等于14进行代码注入
	 * 
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 * 
	 * 
	 */
	private static InjectResult injectAboveEqualApiLevel14(ClassLoader parentClassLoader,
			ClassLoader childClassLoader) {
		PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
		DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
		InjectResult result = null;
		try {
			// 注入 dex
			Object dexElements = combineArray(getDexElements(getPathList(pathClassLoader)),
					getDexElements(getPathList(dexClassLoader)));

			Object pathList = getPathList(pathClassLoader);

			setField(pathList, pathList.getClass(), "dexElements", dexElements);

			// 注入native lib so目录，需要parent class
			// loader遍历此目录能够找到。因为注入了以后，不处理这个目录找不到。
			Object dexNativeLibraryDirs = combineArray(getNativeLibraryDirectories(getPathList(pathClassLoader)),
					getNativeLibraryDirectories(getPathList(dexClassLoader)));

			setField(pathList, pathList.getClass(), "nativeLibraryDirectories", dexNativeLibraryDirs);
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}
		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	/**
	 * 反射设置类中{@link Field}
	 *
	 * @param oObj
	 *            类实例对象
	 * @param aCl
	 *            类
	 * @param aField
	 *            待设置变量
	 * @param value
	 *            待设置值
	 */
	private static void setField(Object oObj, Class<?> aCl, String aField, Object value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field localField = aCl.getDeclaredField(aField);
		localField.setAccessible(true);
		localField.set(oObj, value);
	}

	/**
	 * 反射获取类中变量
	 * 
	 * @param oObj
	 *            类实例
	 * @param aCl
	 *            类
	 * @param aField
	 *            变量
	 * 
	 * @return 变量对象
	 */
	private static Object getField(Object oObj, Class<?> aCl, String aField)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field localField = aCl.getDeclaredField(aField);
		localField.setAccessible(true);
		return localField.get(oObj);
	}

	/**
	 * 合并数组
	 *
	 * @param aArrayLhs
	 *            左数组
	 * @param aArrayRhs
	 *            右数组
	 * @return 合并后的数组
	 * 
	 * 
	 */
	private static Object combineArray(Object aArrayLhs, Object aArrayRhs) {
		Class<?> localClass = aArrayLhs.getClass().getComponentType();
		int i = Array.getLength(aArrayLhs);
		int j = i + Array.getLength(aArrayRhs);
		Object result = Array.newInstance(localClass, j);
		for (int k = 0; k < j; ++k) {
			if (k < i) {
				Array.set(result, k, Array.get(aArrayLhs, k));
			} else {
				Array.set(result, k, Array.get(aArrayRhs, k - i));
			}
		}
		return result;
	}

	/**
	 * Replace the value of a field containing a non null array, by a new array
	 * containing the elements of the original array plus the elements of
	 * extraElements.
	 * 
	 * @param instance
	 *            the instance whose field is to be modified.
	 * @param fieldName
	 *            the field to modify.
	 * @param extraElements
	 *            elements to append at the end of the array.
	 */
	private static void expandFieldArray(Object instance, String fieldName, Object[] extraElements)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field jlrField = findField(instance, fieldName);
		if (jlrField.getType() == List.class || jlrField.getType() == ArrayList.class) {
			List list = (List) jlrField.get(instance);
			synchronized (ClassLoaderInjectHelper.class) {
				for (Object o : extraElements) {
					if (o != null) {
						list.add(o);
					}
				}
			}
			return;
		}
		Object[] original = (Object[]) jlrField.get(instance);
		Object[] combined = (Object[]) Array.newInstance(original.getClass().getComponentType(),
				original.length + extraElements.length);
		System.arraycopy(original, 0, combined, 0, original.length);
		System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
		jlrField.set(instance, combined);
	}

	/**
	 * Locates a given field anywhere in the class inheritance hierarchy.
	 *
	 * @param instance
	 *            an object to search the field into.
	 * @param name
	 *            field name
	 * @return a field object
	 * @throws NoSuchFieldException
	 *             if the field cannot be located
	 */
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

	/**
	 * Locates a given method anywhere in the class inheritance hierarchy.
	 *
	 * @param instance
	 *            an object to search the method into.
	 * @param name
	 *            method name
	 * @param parameterTypes
	 *            method parameter types
	 * @return a method object
	 * @throws NoSuchMethodException
	 *             if the method cannot be located
	 */
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

		throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes)
				+ " not found in " + instance.getClass());
	}

	/**
	 * delete elements from specific array object.
	 * 只删除一次，因为add的时候会添加重复的，比如/system/lib
	 * <p>
	 * 这个删除的时候不能全部删除
	 * </p>
	 *
	 * @param srcArray
	 *            原数组
	 * @param targetArray
	 *            待删除数据数组
	 * @return 删除数据后的数组
	 *
	 *
	 */
	private static Object removeArrayElements(Object srcArray, Object targetArray) {
		Class<?> localClass = srcArray.getClass().getComponentType();
		int srcLen = Array.getLength(srcArray);
		int targetLen = Array.getLength(targetArray);

		// 备份一下target，用于删除处理
		ArrayList<Object> targetCopy = new ArrayList<Object>();
		for (int j = 0; j < targetLen; j++) {
			Object target = Array.get(targetArray, j);
			targetCopy.add(target);
		}

		ArrayList<Object> resultArray = new ArrayList<Object>();

		for (int i = 0; i < srcLen; i++) {
			Object src = Array.get(srcArray, i);
			boolean finded = false;
			for (int j = 0; j < targetLen; j++) {
				Object target = targetCopy.get(j);
				if (target != null && src.equals(target)) {
					finded = true;
					targetCopy.set(j, null); // 找到后设置为空，表示已经从src删除过一次了，
					break;
				}
			}

			if (!finded) {
				resultArray.add(src);
			}

		}

		int length = resultArray.size();
		Object result = Array.newInstance(localClass, length);

		for (int i = 0; i < length; i++) {
			Array.set(result, i, resultArray.get(i));
		}

		return result;
	}

	/**
	 * delete elements from specific array object.
	 *
	 * @param srcArray
	 *            原数组
	 * @param element
	 *            待删除的数据
	 * @return 已删除后的数据数组
	 *
	 *
	 */
	private static Object removeArrayElement(Object srcArray, Object element) {
		Class<?> localClass = srcArray.getClass().getComponentType();
		int srcLen = Array.getLength(srcArray);

		ArrayList<Object> resultArray = new ArrayList<Object>();

		for (int i = 0; i < srcLen; i++) {
			Object src = Array.get(srcArray, i);
			boolean found = false;
			if (src.equals(element)) {
				found = true;
			}

			if (!found) {
				resultArray.add(src);
			}

		}

		int length = resultArray.size();
		Object result = Array.newInstance(localClass, length);

		for (int i = 0; i < length; i++) {
			Array.set(result, i, resultArray.get(i));
		}

		return result;
	}

	/**
	 * append for array
	 *
	 * @param aArray
	 *            原数组
	 * @param aValue
	 *            待添加的数据
	 * @return 添加数据后的数组
	 *
	 *
	 */
	private static Object appendArray(Object aArray, Object aValue) {
		Class<?> localClass = aArray.getClass().getComponentType();
		int i = Array.getLength(aArray);
		int j = i + 1;
		Object localObject = Array.newInstance(localClass, j);
		for (int k = 0; k < j; ++k) {
			if (k < i) {
				Array.set(localObject, k, Array.get(aArray, k));
			} else {
				Array.set(localObject, k, aValue);
			}
		}
		return localObject;
	}

	/**
	 * make a inject result
	 *
	 * @param aResult
	 *            注入结果状态
	 * @param aT
	 *            {@link Throwable}
	 * @return {@link InjectResult}
	 */
	public static InjectResult makeInjectResult(boolean aResult, Throwable aT) {
		InjectResult ir = new InjectResult();
		ir.mIsSuccessful = aResult;
		ir.mErrMsg = (aT != null ? aT.getLocalizedMessage() : null);
		return ir;
	}

	/**
	 * 获取需注入的实例对象pathList
	 *
	 *
	 */
	private static Object getPathList(Object aBaseDexClassLoader)
			throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		return getField(aBaseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
	}

	/**
	 * 获取需注入的实例对象dexElements
	 *
	 *
	 */
	private static Object getDexElements(Object aParamObject)
			throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return getField(aParamObject, aParamObject.getClass(), "dexElements");
	}

	private static Object getNativeLibraryDirectories(Object aParamObject)
			throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return getField(aParamObject, aParamObject.getClass(), "nativeLibraryDirectories");
	}

	/**
	 * 反注入
	 *
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 *
	 *
	 */
	public static InjectResult eject(ClassLoader parentClassLoader, ClassLoader childClassLoader) {
		//
		boolean hasBaseDexClassLoader = true;
		try {
			Class.forName("dalvik.system.BaseDexClassLoader");
		} catch (ClassNotFoundException e) {
			hasBaseDexClassLoader = false;
		}
		if (!hasBaseDexClassLoader) {
			return ejectBelowApiLevel14(parentClassLoader, childClassLoader);
		} else {
			return ejectAboveEqualApiLevel14(parentClassLoader, childClassLoader);
		}
	}

	/**
	 * api小于14时，反注入
	 *
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 *
	 *
	 */
	private static InjectResult ejectBelowApiLevel14(ClassLoader parentClassLoader, ClassLoader childClassLoader) {
		InjectResult result = null;
		PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
		DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
		try {
			setField(pathClassLoader, PathClassLoader.class, "mPaths",
					removeArrayElement(getField(pathClassLoader, PathClassLoader.class, "mPaths"),
							getField(dexClassLoader, DexClassLoader.class, "mRawDexPath")));
			setField(pathClassLoader, PathClassLoader.class, "mFiles",
					removeArrayElements(getField(pathClassLoader, PathClassLoader.class, "mFiles"),
							getField(dexClassLoader, DexClassLoader.class, "mFiles")));
			setField(pathClassLoader, PathClassLoader.class, "mZips",
					removeArrayElements(getField(pathClassLoader, PathClassLoader.class, "mZips"),
							getField(dexClassLoader, DexClassLoader.class, "mZips")));
			setField(pathClassLoader, PathClassLoader.class, "mDexs",
					removeArrayElements(getField(pathClassLoader, PathClassLoader.class, "mDexs"),
							getField(dexClassLoader, DexClassLoader.class, "mDexs")));

			try {
				@SuppressWarnings("unchecked")
				ArrayList<String> libPaths = (ArrayList<String>) getField(pathClassLoader, PathClassLoader.class,
						"libraryPathElements");
				String[] libArray = (String[]) getField(dexClassLoader, DexClassLoader.class, "mLibPaths");
				for (String path : libArray) {
					libPaths.remove(path);
				}
			} catch (Exception e) {
				setField(pathClassLoader, PathClassLoader.class, "mLibPaths",
						removeArrayElements(getField(pathClassLoader, PathClassLoader.class, "mLibPaths"),
								getField(dexClassLoader, DexClassLoader.class, "mLibPaths")));
			}
		} catch (NoSuchFieldException e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		} catch (Exception e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}

		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	/**
	 * api大于等于14，反注入
	 *
	 * @param parentClassLoader
	 *            父{@link ClassLoader}
	 * @param childClassLoader
	 *            子{@link ClassLoader}
	 * @return {@link InjectResult}
	 *
	 *
	 */
	private static InjectResult ejectAboveEqualApiLevel14(ClassLoader parentClassLoader, ClassLoader childClassLoader) {
		PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
		DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
		InjectResult result = null;
		try {
			// 注入 dex
			Object dexElements = removeArrayElements(getDexElements(getPathList(pathClassLoader)),
					getDexElements(getPathList(dexClassLoader)));

			Object pathList = getPathList(pathClassLoader);

			setField(pathList, pathList.getClass(), "dexElements", dexElements);

			// 注入native lib so目录，需要parent class
			// loader遍历此目录能够找到。因为注入了以后，不处理这个目录找不到。
			Object dexNativeLibraryDirs = removeArrayElements(getNativeLibraryDirectories(getPathList(pathClassLoader)),
					getNativeLibraryDirectories(getPathList(dexClassLoader)));

			setField(pathList, pathList.getClass(), "nativeLibraryDirectories", dexNativeLibraryDirs);
		} catch (Throwable e) {
			result = makeInjectResult(false, e);
			e.printStackTrace();
		}
		if (result == null) {
			result = makeInjectResult(true, null);
		}
		return result;
	}

	/**
	 * Installer for platform versions 23.
	 */
	private static final class V23 {

		/**
		 * 注入代码
		 *
		 * @param loader
		 *            {@link ClassLoader}
		 * @param additionalClassPathEntries
		 *            插件代码文件
		 * @param optimizedDirectory
		 *            已优化的文件目录
		 * @param nativeLibraryDirectory
		 *            插件lib文件目录
		 */
		private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory,
				File nativeLibraryDirectory) throws IllegalArgumentException, IllegalAccessException,
				NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
			/**
			 * The patched class loader is expected to be a descendant of
			 * dalvik.system.BaseDexClassLoader. We modify its
			 * dalvik.system.DexPathList pathList field to append additional DEX
			 * file entries.
			 */
			Field pathListField = findField(loader, "pathList");
			Object dexPathList = pathListField.get(loader);
			ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
			expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
					new ArrayList<File>(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
			if (suppressedExceptions.size() > 0) {
				Field suppressedExceptionsField = findField(loader, "dexElementsSuppressedExceptions");
				IOException[] dexElementsSuppressedExceptions = (IOException[]) suppressedExceptionsField.get(loader);

				if (dexElementsSuppressedExceptions == null) {
					dexElementsSuppressedExceptions = suppressedExceptions
							.toArray(new IOException[suppressedExceptions.size()]);
				} else {
					IOException[] combined = new IOException[suppressedExceptions.size()
							+ dexElementsSuppressedExceptions.length];
					suppressedExceptions.toArray(combined);
					System.arraycopy(dexElementsSuppressedExceptions, 0, combined, suppressedExceptions.size(),
							dexElementsSuppressedExceptions.length);
					dexElementsSuppressedExceptions = combined;
				}

				suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
			}

			expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{nativeLibraryDirectory});
		}

		/**
		 * A wrapper around
		 * {@code private static final dalvik.system.DexPathList#makeDexElements}
		 * .
		 */
		private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory,
				ArrayList<IOException> suppressedExceptions)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			// 采用非常丑陋的方式进行兼容
			Method makeDexElements = null;
			try {
				makeDexElements = findMethod(dexPathList, "makePathElements", ArrayList.class, File.class,
						ArrayList.class);
			} catch (NoSuchMethodException e) {
				makeDexElements = null;
			}
			if (makeDexElements == null) {
				try {
					makeDexElements = findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
				} catch (NoSuchMethodException e) {
					makeDexElements = null;
				}
			}
			if (makeDexElements == null) {
				try {
					makeDexElements = findMethod(dexPathList, "makePathElements", List.class, File.class,
							ArrayList.class);
				} catch (NoSuchMethodException e) {
					makeDexElements = null;
				}
			}
			if (makeDexElements == null) {
				makeDexElements = findMethod(dexPathList, "makePathElements", ArrayList.class, File.class, List.class);
			}

			return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
		}
	}

	/**
	 * Installer for platform versions 19.
	 */
	private static final class V19 {

		/**
		 * 注入代码
		 *
		 * @param loader
		 *            {@link ClassLoader}
		 * @param additionalClassPathEntries
		 *            插件代码文件
		 * @param optimizedDirectory
		 *            已优化的文件目录
		 * @param nativeLibraryDirectory
		 *            插件lib文件目录
		 */
		private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory,
				File nativeLibraryDirectory) throws IllegalArgumentException, IllegalAccessException,
				NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
			/**
			 * The patched class loader is expected to be a descendant of
			 * dalvik.system.BaseDexClassLoader. We modify its
			 * dalvik.system.DexPathList pathList field to append additional DEX
			 * file entries.
			 */
			Field pathListField = findField(loader, "pathList");
			Object dexPathList = pathListField.get(loader);
			ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
			expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
					new ArrayList<File>(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
			if (suppressedExceptions.size() > 0) {
				Field suppressedExceptionsField = findField(loader, "dexElementsSuppressedExceptions");
				IOException[] dexElementsSuppressedExceptions = (IOException[]) suppressedExceptionsField.get(loader);

				if (dexElementsSuppressedExceptions == null) {
					dexElementsSuppressedExceptions = suppressedExceptions
							.toArray(new IOException[suppressedExceptions.size()]);
				} else {
					IOException[] combined = new IOException[suppressedExceptions.size()
							+ dexElementsSuppressedExceptions.length];
					suppressedExceptions.toArray(combined);
					System.arraycopy(dexElementsSuppressedExceptions, 0, combined, suppressedExceptions.size(),
							dexElementsSuppressedExceptions.length);
					dexElementsSuppressedExceptions = combined;
				}

				suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
			}

			expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{nativeLibraryDirectory});
		}

		/**
		 * A wrapper around
		 * {@code private static final dalvik.system.DexPathList#makeDexElements}
		 * .
		 */
		private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory,
				ArrayList<IOException> suppressedExceptions)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			// 采用非常丑陋的方式进行兼容
			Method makeDexElements = null;
			try {
				makeDexElements = findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
						ArrayList.class);
			} catch (NoSuchMethodException e) {
				makeDexElements = null;
			}
			if (makeDexElements == null) {
				try {
					makeDexElements = findMethod(dexPathList, "makeDexElements", List.class, File.class, List.class);
				} catch (NoSuchMethodException e) {
					makeDexElements = null;
				}
			}
			if (makeDexElements == null) {
				try {
					makeDexElements = findMethod(dexPathList, "makeDexElements", List.class, File.class,
							ArrayList.class);
				} catch (NoSuchMethodException e) {
					makeDexElements = null;
				}
			}
			if (makeDexElements == null) {
				makeDexElements = findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class, List.class);
			}

			return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
		}
	}

	/**
	 * Installer for platform versions 14, 15, 16, 17 and 18.
	 */
	private static final class V14 {

		/**
		 * 注入代码
		 *
		 * @param loader
		 *            {@link ClassLoader}
		 * @param additionalClassPathEntries
		 *            插件代码文件
		 * @param optimizedDirectory
		 *            已优化的文件目录
		 * @param nativeLibraryDirectory
		 *            插件lib文件目录
		 */
		private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory,
				File nativeLibraryDirectory) throws IllegalArgumentException, IllegalAccessException,
				NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
			/**
			 * The patched class loader is expected to be a descendant of
			 * dalvik.system.BaseDexClassLoader. We modify its
			 * dalvik.system.DexPathList pathList field to append additional DEX
			 * file entries.
			 */
			Field pathListField = findField(loader, "pathList");
			Object dexPathList = pathListField.get(loader);
			expandFieldArray(dexPathList, "dexElements",
					makeDexElements(dexPathList, new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
			expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{nativeLibraryDirectory});
		}

		/**
		 * A wrapper around
		 * {@code private static final dalvik.system.DexPathList#makeDexElements}
		 * .
		 */
		private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Method makeDexElements = null;
			try {
				makeDexElements = findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);
			} catch (NoSuchMethodException e) {
				makeDexElements = findMethod(dexPathList, "makeDexElements", List.class, File.class);
			}

			return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
		}
	}

	/**
	 * Installer for platform versions 4 to 13.
	 *
	 *
	 */
	private static final class V4 {

		private static void install(ClassLoader loader, List<File> additionalClassPathEntries)
				throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, IOException {
			/**
			 * The patched class loader is expected to be a descendant of
			 * dalvik.system.DexClassLoader. We modify its fields mPaths,
			 * mFiles, mZips and mDexs to append additional DEX file entries.
			 */
			int extraSize = additionalClassPathEntries.size();

			Field pathField = findField(loader, "path");

			StringBuilder path = new StringBuilder((String) pathField.get(loader));
			String[] extraPaths = new String[extraSize];
			File[] extraFiles = new File[extraSize];
			ZipFile[] extraZips = new ZipFile[extraSize];
			DexFile[] extraDexs = new DexFile[extraSize];
			for (ListIterator<File> iterator = additionalClassPathEntries.listIterator(); iterator.hasNext();) {
				File additionalEntry = iterator.next();
				String entryPath = additionalEntry.getAbsolutePath();
				path.append(':').append(entryPath);
				int index = iterator.previousIndex();
				extraPaths[index] = entryPath;
				extraFiles[index] = additionalEntry;
				extraZips[index] = new ZipFile(additionalEntry);
				extraDexs[index] = DexFile.loadDex(entryPath, entryPath + ".dex", 0);
			}

			pathField.set(loader, path.toString());
			expandFieldArray(loader, "mPaths", extraPaths);
			expandFieldArray(loader, "mFiles", extraFiles);
			expandFieldArray(loader, "mZips", extraZips);
			expandFieldArray(loader, "mDexs", extraDexs);
		}
	}

	/**
	 * inject result
	 */
	public static class InjectResult {
		/** is successful */
		public boolean mIsSuccessful;
		/** error msg */
		public String mErrMsg;
	}
}
