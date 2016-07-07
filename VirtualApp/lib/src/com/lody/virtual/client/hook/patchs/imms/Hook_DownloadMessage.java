package com.lody.virtual.client.hook.patchs.imms;

/**
 * @author Lody
 *
 */
/* package */ class Hook_DownloadMessage extends BaseHook_ReplacePkgName {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_DownloadMessage(MmsPatch patchObject) {
		super(patchObject);
	}

	@Override
	public int getIndex() {
		return 1;
	}

	@Override
	public String getName() {
		return "downloadMessage";
	}
}
