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
/* package */ class Hook_GetContentProviderExternal extends Hook_GetContentProvider {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetContentProviderExternal(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

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