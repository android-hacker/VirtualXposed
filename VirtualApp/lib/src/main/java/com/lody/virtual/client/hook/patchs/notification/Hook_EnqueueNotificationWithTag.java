package com.lody.virtual.client.hook.patchs.notification;

/**
 * @author Lody
 *
 */
/* package */ class Hook_EnqueueNotificationWithTag extends Hook_EnqueueNotification {
	{
		replaceLastUserId();
	}

	@Override
	public String getName() {
		return "enqueueNotificationWithTag";
	}
}
