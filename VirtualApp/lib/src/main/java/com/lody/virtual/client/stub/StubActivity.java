package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.lody.virtual.BuildConfig;
import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.helper.proto.StubActivityRecord;

/**
 * @author Lody
 *
 */
public abstract class StubActivity extends Activity {

	private static final boolean DEBUG = BuildConfig.DEBUG;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent stubIntent = getIntent();
		try {
			PatchManager.getInstance().checkEnv(HCallbackHook.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// Note:
		// ClassLoader of savedInstanceState is not exist now.
		super.onCreate(null);
		StubActivityRecord r = stubIntent.getParcelableExtra("_VA_|_stub_");
		if (r == null) {
			if (DEBUG) {
				Toast.makeText(this, "Ops...", Toast.LENGTH_SHORT).show();
			}
		} else {
			Intent intent = r.intent;
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

	public static class C0_ extends StubActivity {
	}

	public static class C1_ extends StubActivity {
	}

	public static class C2_ extends StubActivity {
	}

	public static class C3_ extends StubActivity {
	}

	public static class C4_ extends StubActivity {
	}

	public static class C5_ extends StubActivity {
	}

	public static class C6_ extends StubActivity {
	}

	public static class C7_ extends StubActivity {
	}

	public static class C8_ extends StubActivity {
	}

	public static class C9_ extends StubActivity {
	}

	public static class C10_ extends StubActivity {
	}

	public static class C11_ extends StubActivity {
	}

	public static class C12_ extends StubActivity {
	}

	public static class C13_ extends StubActivity {
	}

	public static class C14_ extends StubActivity {
	}

	public static class C15_ extends StubActivity {
	}

}
