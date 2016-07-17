package com.lody.virtual.client.hook.patchs.notification;

/**
 * @author 247321453
 *
 */
/* package */ class Hook_EnqueueNotificationWithTagPriority extends Hook_EnqueueNotificationWithTag {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject
     *            注入对象
     */
    public Hook_EnqueueNotificationWithTagPriority(NotificationManagerPatch patchObject) {
        super(patchObject);
    }
}
