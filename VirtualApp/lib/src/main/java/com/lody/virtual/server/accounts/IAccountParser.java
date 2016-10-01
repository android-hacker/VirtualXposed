package com.lody.virtual.server.accounts;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

public interface IAccountParser {
    XmlResourceParser getParser(Context context, ServiceInfo serviceInfo, String name);
    Resources getResources(Context context, ApplicationInfo appInfo) throws Exception;
}