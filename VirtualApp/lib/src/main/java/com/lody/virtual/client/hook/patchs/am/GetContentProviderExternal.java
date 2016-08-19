package com.lody.virtual.client.hook.patchs.am;

/**
 * @author Lody
 *
 *
 *         原型: API 16: public ContentProviderHolder
 *         getContentProviderExternal(String name, IBinder token) After: public
 *         ContentProviderHolder getContentProviderExternal(String name, int
 *         userId, IBinder token)
 *
 *
 */
/* package */ class GetContentProviderExternal extends GetContentProvider {

	@Override
	public String getName() {
		return "getContentProviderExternal";
	}

	@Override
	public int getProviderNameIndex() {
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}