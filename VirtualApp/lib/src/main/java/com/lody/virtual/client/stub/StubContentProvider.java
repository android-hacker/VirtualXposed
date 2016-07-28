package com.lody.virtual.client.stub;

import android.os.Bundle;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.component.BaseContentProvider;

/**
 * @author Lody
 *
 */
public abstract class StubContentProvider extends BaseContentProvider {

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		if (MethodConstants.INIT_PROCESS.equals(method)) {
			initProcess(extras);
			return null;
		}

		return null;
	}

	private void initProcess(Bundle extras) {
		final String processName = extras.getString(ExtraConstants.EXTRA_PROCESS_NAME);
		final String packageName = extras.getString(ExtraConstants.EXTRA_PKG);
		AppSandBox.install(processName, packageName);
	}

	public static class C0 extends StubContentProvider {
	}

	public static class C1 extends StubContentProvider {
	}

	public static class C2 extends StubContentProvider {
	}

	public static class C3 extends StubContentProvider {
	}

	public static class C4 extends StubContentProvider {
	}

	public static class C5 extends StubContentProvider {
	}

	public static class C6 extends StubContentProvider {
	}

	public static class C7 extends StubContentProvider {
	}

	public static class C8 extends StubContentProvider {
	}

	public static class C9 extends StubContentProvider {
	}

	public static class C10 extends StubContentProvider {
	}

	public static class C11 extends StubContentProvider {
	}

	public static class C12 extends StubContentProvider {
	}

	public static class C13 extends StubContentProvider {
	}

	public static class C14 extends StubContentProvider {
	}

	public static class C15 extends StubContentProvider {
	}

	public static class C16 extends StubContentProvider {
	}

	public static class C17 extends StubContentProvider {
	}

	public static class C18 extends StubContentProvider {
	}

	public static class C19 extends StubContentProvider {
	}

	public static class C20 extends StubContentProvider {
	}

}
