package mirror;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class RefStaticObject<T> {
    private Field field;

    public RefStaticObject(Class<?> cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public Class<?> type() {
        return field.getType();
    }

    public T get() {
        T obj = null;
        try {
            obj = (T) this.field.get(null);
        } catch (Exception e) {
            //Ignore
        }
        return obj;
    }

    public void set(T obj) {
        try {
            this.field.set(null, obj);
        } catch (Exception e) {
            //Ignore
        }
    }
}