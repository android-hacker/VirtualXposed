package com.lody.virtual.client.hook.patchs.vibrator;

/**
 * @author Lody
 *
 */
/* package */ class Hook_VibratePattern extends Hook_Vibrate {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_VibratePattern(VibratorPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "vibratePattern";
	}
}
