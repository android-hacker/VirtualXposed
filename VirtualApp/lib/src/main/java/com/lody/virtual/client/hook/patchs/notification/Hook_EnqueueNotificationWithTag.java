package com.lody.virtual.client.hook.patchs.notification;

/**
 * @author Lody
 *
 */
/* package */ class Hook_EnqueueNotificationWithTag extends Hook_EnqueueNotification {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_EnqueueNotificationWithTag(NotificationManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "enqueueNotificationWithTag";
	}
}
