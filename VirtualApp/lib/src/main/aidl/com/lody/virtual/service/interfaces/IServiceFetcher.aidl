// IServiceFetcher.aidl
package com.lody.virtual.service.interfaces;

interface IServiceFetcher {
    IBinder getService(String name);
    void addService(String name,in IBinder service);
    void removeService(String name);
}