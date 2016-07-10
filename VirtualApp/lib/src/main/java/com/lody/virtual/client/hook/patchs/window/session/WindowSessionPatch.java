package com.lody.virtual.client.hook.patchs.window.session;

import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;

import android.view.IWindowSession;

/**
 * @author Lody
 *
 *
 * @see IWindowSession
 */
@Patch({Hook_Add.class, Hook_AddToDisplay.class, Hook_AddToDisplayWithoutInputChannel.class,
		Hook_AddWithoutInputChannel.class, Hook_Relayout.class})
public class WindowSessionPatch extends PatchObject<IWindowSession, HookObject<IWindowSession>> {

	private IWindowSession session;

	public WindowSessionPatch(IWindowSession session) {
		this.session = session;
		this.hookObject = initHookObject();
		applyHooks();
	}

	@Override
	public HookObject<IWindowSession> initHookObject() {
		return new HookObject<IWindowSession>(session);
	}

	@Override
	public void applyHooks() {
		super.applyHooks();
	}

	@Override
	public void inject() throws Throwable {
		// Not use it
	}

	@Override
	public boolean isEnvBad() {
		return session != null;
	}
}
