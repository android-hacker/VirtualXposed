package com.lody.virtual.client.hook.patchs.window.session;

/**
 * @author Lody
 *
 */
/* package */ class Hook_AddToDisplay extends BaseHook_ReplacePkgName {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_AddToDisplay(WindowSessionPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "addToDisplay";
	}
}
