package mirror;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class FieldDef {
    private Field field;

    public FieldDef(Class<?> cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public <T> T get(Object object) {
        try {
            return (T) this.field.get(object);
        } catch (Exception e) {
            return null;
        }
    }

    public void set(Object obj, Object value) {
        try {
            this.field.set(obj, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}