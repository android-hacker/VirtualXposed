package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/**
 * Class:
 * Created by andy on 16-8-3.
 * TODO:
 */
public class Hook_StartVoiceActivity extends Hook_BaseStartActivity {
  @Override
  public String getName() {
    return "startVoiceActivity";
  }

  @Override
  public Object onHook(Object who, Method method, Object... args) throws Throwable {
    return super.onHook(who, method, args);
  }
}
