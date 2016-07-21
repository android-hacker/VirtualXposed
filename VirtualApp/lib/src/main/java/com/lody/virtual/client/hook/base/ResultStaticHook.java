package com.lody.virtual.client.hook.base;

/**
 * @author Lody
 */

public class ResultStaticHook extends StaticHook {

    Object mResult;

    public ResultStaticHook(String name, Object result) {
        super(name);
        mResult = result;
    }


}
