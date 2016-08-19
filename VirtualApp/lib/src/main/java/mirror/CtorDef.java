package mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class CtorDef {
    private Constructor<?> ctor;

    public CtorDef(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodInfo.class)) {
            Class<?>[] types = field.getAnnotation(MethodInfo.class).value();
            this.ctor = cls.getDeclaredConstructor(types);
        } else {
            if (field.isAnnotationPresent(MethodReflectionInfo.class)) {
                String[] values = field.getAnnotation(MethodReflectionInfo.class).value();
                Class[] parameterTypes = new Class[values.length];
                int N = 0;
                while (N < values.length) {
                    try {
                        parameterTypes[N] = Class.forName(values[N]);
                        N++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.ctor = cls.getDeclaredConstructor(parameterTypes);
            }
            this.ctor = cls.getDeclaredConstructor();
        }
        this.ctor.setAccessible(true);
    }

    public Object newInstance() {
        try {
            return this.ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public Object newInstance(Object... params) {
        try {
            return this.ctor.newInstance(params);
        } catch (Exception e) {
            return null;
        }
    }
}