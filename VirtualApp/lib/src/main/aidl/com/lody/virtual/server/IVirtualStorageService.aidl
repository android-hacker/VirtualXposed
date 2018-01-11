// IVirtualStorageService.aidl
package com.lody.virtual.server;


interface IVirtualStorageService {

   void setVirtualStorage(in String packageName, in int userId, in String vsPath);

   String getVirtualStorage(in String packageName, in int userId);

   void setVirtualStorageState(in String packageName, in int userId, in boolean enable);

   boolean isVirtualStorageEnable(in String packageName, in int userId);

}
