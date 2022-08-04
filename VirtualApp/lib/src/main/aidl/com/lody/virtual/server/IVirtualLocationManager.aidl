// IVirtualLocationManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.remote.vloc.VCell;
import com.lody.virtual.remote.vloc.VWifi;
import com.lody.virtual.remote.vloc.VLocation;

interface IVirtualLocationManager {

    int getMode(int userId, in String pkg);
    void setMode(int userId, in String pkg, int mode);

    void setCell(in int userId, in String pkg, in VCell cell);
    void setAllCell(in int userId, in String pkg, in List<VCell> cell);
    void setNeighboringCell(in int userId, in String pkg, in List<VCell> cell);

    void setGlobalCell(in VCell cell);
    void setGlobalAllCell(in List<VCell> cell);
    void setGlobalNeighboringCell(in List<VCell> cell);

    VCell getCell(in int userId, in String pkg);
    List<VCell> getAllCell(in int userId, in String pkg);
    List<VCell> getNeighboringCell(in int userId, in String pkg);

    void setLocation(in int userId, in String pkg, in VLocation loc);
    VLocation getLocation(in int userId, in String pkg);

    void setGlobalLocation(in VLocation loc);
    VLocation getGlobalLocation();
}