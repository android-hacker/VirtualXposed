package com.lody.virtual.client.fixer;

import android.view.LayoutInflater;
import android.view.View;

import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author Lody
 *
 * Clear the cache of LayoutInflater.
 *
 */
public class LayoutInflaterFixer {

	private static HashMap<String, Constructor<? extends View>> sConstructorMap = null;

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
