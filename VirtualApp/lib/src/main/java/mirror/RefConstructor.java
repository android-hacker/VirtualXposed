package mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class RefConstructor<T> {
    private Constructor<?> ctor;

    public RefConstructor(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodParams.class)) {
            Class<?>[] types = field.getAnnotation(MethodParams.class).value();
            ctor = cls.getDeclaredConstructor(types);
        } else if (field.isAnnotationPresent(MethodReflectParams.class)) {
            String[] values = field.getAnnotation(MethodReflectParams.class).value();
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
            ctor = cls.getDeclaredConstructor(parameterTypes);
        } else {
            ctor = cls.getDeclaredConstructor();
        }
        if (ctor != null && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }

    public T newInstance() {
        try {
            return (T) ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public T newInstance(Object... params) {
        try {
            return (T) ctor.newInstance(params);
        } catch (Exception e) {
            return null;
        }
    }
}