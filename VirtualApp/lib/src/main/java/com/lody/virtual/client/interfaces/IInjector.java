package com.lody.virtual.client.interfaces;

/**
 * @author Lody
 *
 * The Objects who implemention this interface will be able to inject other object.
 *
 */
public interface IInjector {

	/**
	 *
     * Do injection.
	 * 
	 * @throws Throwable if inject failed
	 */
	void inject() throws Throwable;

	/**
     * Check if the injection has bad.
     *
	 * @return If the injection has bad
	 */
	boolean isEnvBad();

}
