package com.lody.virtual.client.interfaces;

import com.lody.virtual.client.hook.base.Hook;

import java.util.Map;

/**
 * @author Lody
 *
 */
public interface IHookObject {

	void copyHooks(IHookObject from);

	Map<String, Hook> getAllHooks();

	Hook addHook(Hook hook);

	Hook removeHook(String hookName);

	void removeHook(Hook hook);

	void removeAllHook();

	<H extends Hook> H getHook(String name);

	Object getProxyInterface();

	Object getBaseInterface();

	int getHookCount();

}
