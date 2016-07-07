package com.lody.virtual.client.hook.patchs.imms;

/**
 * @author Lody
 *
 */
/* package */ class Hook_UpdateStoredMessageStatus extends BaseHook_ReplacePkgName {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_UpdateStoredMessageStatus(MmsPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "updateStoredMessageStatus";
	}
}
