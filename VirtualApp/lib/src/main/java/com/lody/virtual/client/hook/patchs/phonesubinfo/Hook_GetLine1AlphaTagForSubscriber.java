package com.lody.virtual.client.hook.patchs.phonesubinfo;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetLine1AlphaTagForSubscriber extends BaseHook_ReplacePkgName {

	@Override
	protected int getIndex() {
		return -2;
	}

	@Override
	public String getName() {
		return "getLine1AlphaTagForSubscriber";
	}
}
