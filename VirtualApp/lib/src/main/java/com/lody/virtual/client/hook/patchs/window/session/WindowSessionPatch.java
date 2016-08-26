package com.lody.virtual.client.hook.patchs.window.session;

import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;

/**
 * @author Lody
 *
 *
 */
@Patch({Add.class, AddToDisplay.class, AddToDisplayWithoutInputChannel.class,
		AddWithoutInputChannel.class, Relayout.class})
public class WindowSessionPatch extends PatchDelegate<HookDelegate<IInterface>> {

	public WindowSessionPatch(IInterface session) {
		super(session);
	}

	@Override
	public HookDelegate<IInterface> createHookDelegate() {

		return new HookDelegate<IInterface>() {
			@Override
			protected IInterface createInterface() {
				return (IInterface) baseObject;
			}
		};
	}

	@Override
	public void onBindHooks() {
		super.onBindHooks();
	}

	@Override
	public void inject() throws Throwable {
		// <EMPTY>
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate().getProxyInterface() != null;
	}
}
