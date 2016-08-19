package mirror;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class StaticMethodDef {
    private Method method;

    public StaticMethodDef(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodInfo.class)) {
            Class<?>[] types = field.getAnnotation(MethodInfo.class).value();
            this.method = cls.getDeclaredMethod(field.getName(), types);
            this.method.setAccessible(true);
            return;
        }
        for (Method method : cls.getDeclaredMethods()) {
            if (method.getName().equals(field.getName())) {
                this.method = method;
                this.method.setAccessible(true);
                break;
            }
        }
        if (this.method == null) {
            throw new NoSuchMethodException(field.getName());
        }
    }

    public <T> T call(Object... params) {
        Object obj = null;
        try {
            obj = this.method.invoke(null, params);
        } catch (Exception e) {
            //Ignore
        }
        return (T) obj;
    }

    public <T> T callWithException(Object... params) throws InvocationTargetException, IllegalAccessException {
        return (T) this.method.invoke(null, params);
    }
}