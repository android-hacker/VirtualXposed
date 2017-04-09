package com.lody.virtual.client.hook.proxies.graphics;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.android.view.IGraphicsStats;


/**
 * @author Lody
 */
public class GraphicsStatsStub extends BinderInvocationProxy {

	public GraphicsStatsStub() {
		super(IGraphicsStats.Stub.asInterface, "graphicsstats");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("requestBufferForProcess"));
	}
}
