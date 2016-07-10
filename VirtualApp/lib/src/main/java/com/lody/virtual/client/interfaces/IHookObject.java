package com.lody.virtual.client.interfaces;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
public interface IHookObject<T> {

	void addHook(Hook hook);

	Hook removeHook(String hookName);

	void removeHook(Hook hook);

	void removeAllHook();

	<H extends Hook> H getHook(String name);

	T getProxyObject();

	T getBaseObject();

	int getHookCount();

}
