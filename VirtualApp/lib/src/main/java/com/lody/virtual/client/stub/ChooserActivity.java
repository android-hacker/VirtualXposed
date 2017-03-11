package com.lody.virtual.client.stub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import com.lody.virtual.R;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

public class ChooserActivity extends ResolverActivity {
    public static final String EXTRA_DATA = "android.intent.extra.virtual.data";
    public static final String EXTRA_WHO = "android.intent.extra.virtual.who";
    public static final String EXTRA_REQUEST_CODE = "android.intent.extra.virtual.request_code";
    public static final String ACTION;

    static {
        Intent target = new Intent();
        Intent intent = Intent.createChooser(target, "");
        ACTION = intent.getAction();
    }
    public static boolean check(Intent intent) {
        try {
            return TextUtils.equals(ACTION, intent.getAction())
                    ||TextUtils.equals(Intent.ACTION_CHOOSER, intent.getAction());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int userId = intent.getIntExtra(Constants.EXTRA_USER_HANDLE, VUserHandle.getCallingUserId());
        mOptions = intent.getParcelableExtra(EXTRA_DATA);
        mResultWho = intent.getStringExtra(EXTRA_WHO);
        mRequestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
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
        super.onCreate(savedInstanceState, target, title, initialIntents, null, false, userId);
    }
}
