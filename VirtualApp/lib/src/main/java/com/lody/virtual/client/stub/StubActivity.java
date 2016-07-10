package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.helper.ExtraConstants;

/**
 * @author Lody
 *
 */
public abstract class StubActivity extends Activity {

	private static final boolean DEBUG = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent().getParcelableExtra(ExtraConstants.EXTRA_TARGET_INTENT);
		if (intent == null) {
			if (DEBUG) {
				Toast.makeText(this, "Ops...", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			// Fix : ActivityThread$mH
			try {
				PatchManager.getInstance().checkEnv(HCallbackHook.class);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			overridePendingTransition(0, 0);
			startActivity(intent);
		}
		finish();
	}

	public static class C0 extends StubActivity {
	}

	public static class C1 extends StubActivity {
	}

	public static class C2 extends StubActivity {
	}

	public static class C3 extends StubActivity {
	}

	public static class C4 extends StubActivity {
	}

	public static class C5 extends StubActivity {
	}

	public static class C6 extends StubActivity {
	}

	public static class C7 extends StubActivity {
	}

	public static class C8 extends StubActivity {
	}

	public static class C9 extends StubActivity {
	}

	public static class C10 extends StubActivity {
	}

	public static class C11 extends StubActivity {
	}

	public static class C12 extends StubActivity {
	}

	public static class C13 extends StubActivity {
	}

	public static class C14 extends StubActivity {
	}

	public static class C15 extends StubActivity {
	}

}
