package com.lody.virtual.client.env;

/**
 * @author Lody
 *
 */
public class SandBoxEnv {
	private static final SandBoxEnv sEnv = new SandBoxEnv();

	/**
	 * 是否与沙盒外部的APP隔离
	 */
	private boolean isolationOutSideApp = true;

	public static SandBoxEnv getEnv() {
		return sEnv;
	}

	public boolean isIsolationOutSideApp() {
		return isolationOutSideApp;
	}

	public void setIsolationOutSideApp(boolean isolationOutSideApp) {
		this.isolationOutSideApp = isolationOutSideApp;
	}
}
