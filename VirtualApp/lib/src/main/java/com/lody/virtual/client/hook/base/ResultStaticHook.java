package com.lody.virtual.client.hook.base;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class ResultStaticHook extends StaticHook {

	Object mResult;

	public ResultStaticHook(String name, Object result) {
		super(name);
		mResult = result;
	}

	public Object getResult() {
		return mResult;
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return mResult;
	}
}
