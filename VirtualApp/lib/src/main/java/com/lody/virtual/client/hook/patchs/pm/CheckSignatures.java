package com.lody.virtual.client.hook.patchs.pm;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Lody
 *
 */
@SuppressLint("PackageManagerGetSignatures")
/* package */ class CheckSignatures extends Hook {

	@Override
	public String getName() {
		return "checkSignatures";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {

		if (args.length == 2 && args[0] instanceof String && args[1] instanceof String) {

			PackageManager pm = VirtualCore.getPM();

			String pkgNameOne = (String) args[0], pkgNameTwo = (String) args[1];
			try {
				PackageInfo pkgOne = pm.getPackageInfo(pkgNameOne, PackageManager.GET_SIGNATURES);
				PackageInfo pkgTwo = pm.getPackageInfo(pkgNameTwo, PackageManager.GET_SIGNATURES);

				Signature[] one = pkgOne.signatures;
				Signature[] two = pkgTwo.signatures;

				if (ArrayUtils.isEmpty(one)) {
					if (!ArrayUtils.isEmpty(two)) {
						return PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
					} else {
						return PackageManager.SIGNATURE_NEITHER_SIGNED;
					}
				} else {
					if (ArrayUtils.isEmpty(two)) {
						return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
					} else {
						// 走到了这里说明两个包的签名都在
						if (Arrays.equals(one, two)) {
							return PackageManager.SIGNATURE_MATCH;
						} else {
							return PackageManager.SIGNATURE_NO_MATCH;
						}
					}
				}
			} catch (Throwable e) {
				// Ignore
			}
		}

		return method.invoke(who, args);
	}
}
