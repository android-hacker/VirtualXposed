package com.lody.virtual.service.am;

import android.os.Binder;

/**
 * @author Lody
 */

public class VServiceToken extends Binder {

    @Override
    public String getInterfaceDescriptor() {
        return VServiceToken.class.getName();
    }
}
