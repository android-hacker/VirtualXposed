package com.lody.virtual.client.hook.modifiers;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.lody.virtual.helper.utils.Reflect;

import android.view.LayoutInflater;
import android.view.View;

/**
 * @author Lody
 *
 */
public class LayoutInflaterModifier {

	static HashMap<String, Constructor<? extends View>> sConstructorMap = null;

	static {
		try {
			sConstructorMap = Reflect.on(LayoutInflater.class).get("sConstructorMap");
		} catch (Throwable e) {
			// Ignore
		}
	}

	public static void clearLayoutCache() {
		if (sConstructorMap != null) {
			sConstructorMap.clear();
		}
	}
}
