package com.lody.virtual.client.hook.patchs.isms;

import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceSpecPkgHook;

import mirror.com.android.internal.telephony.ISms;

/**
 * @author Lody
 */

public class ISmsPatch extends PatchBinderDelegate {

    public ISmsPatch() {
        super(ISms.Stub.TYPE, "isms");
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addHook(new ReplaceSpecPkgHook("getAllMessagesFromIccEfForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("updateMessageOnIccEfForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("copyMessageToIccEfForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("sendDataForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("sendDataForSubscriberWithSelfPermissions", 1));
            addHook(new ReplaceSpecPkgHook("sendTextForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("sendTextForSubscriberWithSelfPermissions", 1));
            addHook(new ReplaceSpecPkgHook("sendMultipartTextForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("sendStoredText", 1));
            addHook(new ReplaceSpecPkgHook("sendStoredMultipartText", 1));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addHook(new ReplaceCallingPkgHook("getAllMessagesFromIccEf"));
            addHook(new ReplaceSpecPkgHook("getAllMessagesFromIccEfForSubscriber", 1));
            addHook(new ReplaceCallingPkgHook("updateMessageOnIccEf"));
            addHook(new ReplaceSpecPkgHook("updateMessageOnIccEfForSubscriber", 1));
            addHook(new ReplaceCallingPkgHook("copyMessageToIccEf"));
            addHook(new ReplaceSpecPkgHook("copyMessageToIccEfForSubscriber", 1));
            addHook(new ReplaceCallingPkgHook("sendData"));
            addHook(new ReplaceSpecPkgHook("sendDataForSubscriber", 1));
            addHook(new ReplaceCallingPkgHook("sendText"));
            addHook(new ReplaceSpecPkgHook("sendTextForSubscriber", 1));
            addHook(new ReplaceCallingPkgHook("sendMultipartText"));
            addHook(new ReplaceSpecPkgHook("sendMultipartTextForSubscriber", 1));
            addHook(new ReplaceSpecPkgHook("sendStoredText", 1));
            addHook(new ReplaceSpecPkgHook("sendStoredMultipartText", 1));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            addHook(new ReplaceCallingPkgHook("getAllMessagesFromIccEf"));
            addHook(new ReplaceCallingPkgHook("updateMessageOnIccEf"));
            addHook(new ReplaceCallingPkgHook("copyMessageToIccEf"));
            addHook(new ReplaceCallingPkgHook("sendData"));
            addHook(new ReplaceCallingPkgHook("sendText"));
            addHook(new ReplaceCallingPkgHook("sendMultipartText"));
        }
    }
}
