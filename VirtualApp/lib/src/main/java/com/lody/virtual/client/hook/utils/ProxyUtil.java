package com.lody.virtual.client.hook.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yuekuapp on 16-7-14.
 */
public class ProxyUtil {

  /**
   * 循环获取当前类的所有接口，包括多重接口。代理当前类的时候更加严谨
   * @param clazz
   * @return
   */
  public static Class<?>[] getAllInterface(Class clazz){
    ArrayList<Class<?>> classes = new ArrayList<>();
    ProxyUtil.getAllInterfaces(clazz,classes);
    Class<?>[] result=new Class[classes.size()];
    classes.toArray(result);
    return result;
  }


  public static void getAllInterfaces(Class clazz, ArrayList<Class<?>> interfaceCollection) {
    Class<?>[] classes = clazz.getInterfaces();
    if (classes.length != 0) {
      interfaceCollection.addAll(Arrays.asList(classes));
    }
    if (clazz.getSuperclass() != Object.class) {
      getAllInterfaces(clazz.getSuperclass(), interfaceCollection);
    }
  }

}
