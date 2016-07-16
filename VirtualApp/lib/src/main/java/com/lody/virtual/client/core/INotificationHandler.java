package com.lody.virtual.client.core;

import android.content.Context;

/**
 * @author 247321453
 */
public interface INotificationHandler {
    /***
     *
     * @param hostContext 宿主
     * @param packageName vapp的包名
     * @param args enqueueNotification 方法参数
     * @return 是否处理成功
     * @throws Exception 异常
     */
    boolean dealNotification(Context hostContext, String packageName, Object... args) throws Exception;
}
