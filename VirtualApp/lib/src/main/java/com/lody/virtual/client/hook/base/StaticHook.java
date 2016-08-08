package com.lody.virtual.client.hook.base;

/**
 * @author Lody
 */

public class StaticHook extends Hook {

	private String mName;

	public StaticHook(String name) {
		this.mName = name;
	}

	@Override
	public String getName() {
		return mName;
	}
}
