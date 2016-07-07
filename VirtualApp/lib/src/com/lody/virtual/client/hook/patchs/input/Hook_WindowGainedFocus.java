package com.lody.virtual.client.hook.patchs.input;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayIndex;

import android.view.inputmethod.EditorInfo;

/**
 * @author Lody
 *
 *
 *         原型: public InputBindResult startInput(IInputMethodClient client,
 *         IInputContext inputContext, EditorInfo attribute, int controlFlags)
 */
/* package */ class Hook_WindowGainedFocus extends Hook<InputMethodManagerPatch> {

	private Boolean noEditorInfo = null;
	private int editorInfoIndex = -1;
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_WindowGainedFocus(InputMethodManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "windowGainedFocus";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (noEditorInfo == null) {
			editorInfoIndex = ArrayIndex.indexOfFirst(args, EditorInfo.class);
			noEditorInfo = editorInfoIndex == -1;
		}
		if (!noEditorInfo) {
			EditorInfo attribute = (EditorInfo) args[editorInfoIndex];
			if (attribute != null) {
				String pkgName = attribute.packageName;
				if (isAppPkg(pkgName)) {
					attribute.packageName = getHostPkg();
				}
			}
		}
		return method.invoke(who, args);
	}

}
