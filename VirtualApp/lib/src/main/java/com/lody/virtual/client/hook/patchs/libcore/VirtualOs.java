package com.lody.virtual.client.hook.patchs.libcore;

import android.system.ErrnoException;
import android.system.StructStat;

import com.lody.virtual.IOHook;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;

import libcore.io.ForwardingOs;
import libcore.io.Os;

/**
 * @author Lody
 */
/*package*/ class VirtualOs extends ForwardingOs {


    public VirtualOs(Os os) {
        super(os);
    }

    @Override
    public int getuid() {
        int originUid = os.getuid();
        return IOHook.onGetUid(originUid);
    }

    @Override
    public StructStat stat(String path) throws ErrnoException {
        StructStat stat = super.stat(path);
        if (stat != null) {
            if (stat.st_uid == VirtualCore.getCore().myUid()) {
                Reflect.on(stat).set("st_uid", VClientImpl.getClient().getVUid());
            }
        }
        return stat;
    }
}
