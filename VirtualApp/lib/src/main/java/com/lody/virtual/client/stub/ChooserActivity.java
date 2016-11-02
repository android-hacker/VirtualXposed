package com.lody.virtual.client.stub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import com.lody.virtual.R;
import com.lody.virtual.helper.utils.VLog;

public class ChooserActivity extends ResolverActivity {

    public static boolean check(Intent intent) {
        try {
            return TextUtils.equals(Intent.ACTION_CHOOSER, intent.getAction());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Parcelable targetParcelable = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (!(targetParcelable instanceof Intent)) {
            VLog.w("ChooseActivity", "Target is not an intent: " + targetParcelable);
            finish();
            return;
        }
        Intent target = (Intent) targetParcelable;
        CharSequence title = intent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        if (title == null) {
            title = getString(R.string.choose);
        }
        Parcelable[] pa = intent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
        Intent[] initialIntents = null;
        if (pa != null) {
            initialIntents = new Intent[pa.length];
            for (int i = 0; i < pa.length; i++) {
                if (!(pa[i] instanceof Intent)) {
                    VLog.w("ChooseActivity", "Initial intent #" + i
                            + " not an Intent: " + pa[i]);
                    finish();
                    return;
                }
                initialIntents[i] = (Intent) pa[i];
            }
        }
        super.onCreate(savedInstanceState, target, title, initialIntents, null, false);
    }
}
