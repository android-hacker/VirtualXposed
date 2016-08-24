package com.lody.virtual.client.hook.patchs.pm;

/**
 * @author Lody
 */
public class GetPackageUidEtc extends GetPackageUid {
    @Override
    public String getName() {
        return super.getName() + "Etc";
    }
}
