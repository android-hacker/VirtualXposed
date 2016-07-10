package com.lody.virtual.client.hook.patchs.window;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetAppStartingWindow extends BaseHook_PatchSession {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetAppStartingWindow(WindowManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "setAppStartingWindow";
	}
}
