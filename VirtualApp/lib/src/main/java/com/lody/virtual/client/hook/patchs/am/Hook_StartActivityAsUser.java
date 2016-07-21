package com.lody.virtual.client.hook.patchs.am;

/**
 * @author Lody
 *
 */
/* package */ class Hook_StartActivityAsUser extends Hook_StartActivity {

	@Override
	public String getName() {
		return "startActivityAsUser";
	}
}
