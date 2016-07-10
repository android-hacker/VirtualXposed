package com.lody.virtual.client.hook.base;

import java.lang.reflect.Constructor;

import com.lody.virtual.client.interfaces.IHookObject;
import com.lody.virtual.client.interfaces.Injectable;

import android.os.Build;

/**
 * @author Lody
 *
 *         <p>
 *         所有注入器的基类,使用Patch注解来添加Hook.
 * @see Patch
 */
public abstract class PatchObject<T, H extends IHookObject<T>> implements Injectable {

	protected H hookObject;

	protected T baseObject;

	public PatchObject() {
		this.hookObject = initHookObject();
		applyHooks();
		afterHookApply(hookObject);
	}

	public PatchObject(T baseObject) {
		this.baseObject = baseObject;
		this.hookObject = initHookObject();
		applyHooks();
		afterHookApply(hookObject);
	}

	/**
	 * 初始化Hook对象
	 */
	protected abstract H initHookObject();

	/**
	 * 打入Hooks的Impl
	 */
	protected void applyHooks() {

		if (hookObject != null) {
			Class<? extends PatchObject> clazz = getClass();
			Patch patch = clazz.getAnnotation(Patch.class);
			int version = Build.VERSION.SDK_INT;
			if (patch != null) {
				Class<? extends Hook>[] hookTypes = patch.value();
				for (Class<? extends Hook> hookType : hookTypes) {
					ApiLimit apiLimit = hookType.getAnnotation(ApiLimit.class);
					boolean needToAddHook = true;
					if (apiLimit != null) {
						int apiStart = apiLimit.start();
						int apiEnd = apiLimit.end();
						boolean highThanStart = apiStart == -1 || version > apiStart;
						boolean lowThanEnd = apiEnd == -1 || version < apiEnd;
						if (!highThanStart || !lowThanEnd) {
							needToAddHook = false;
						}
					}
					if (needToAddHook) {
						addHook(hookType);
					}
				}

			}
		}
	}

	protected void addHook(Class<? extends Hook> hookType) {
		try {
			Constructor<?> constructor = hookType.getDeclaredConstructors()[0];
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			Hook hook = (Hook) constructor.newInstance(this);
			hookObject.addHook(hook);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to instance Hook : " + hookType + " :" + e.getMessage());
		}
	}

	/**
	 * Hook添加完成后回调
	 *
	 * @param hookObject
	 *            HookObject
	 */
	protected void afterHookApply(H hookObject) {
	}

	/**
	 * 注入的Impl
	 *
	 * @throws Throwable
	 */
	@Override
	public abstract void inject() throws Throwable;

	public H getHookObject() {
		return hookObject;
	}
}
