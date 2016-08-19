package mirror;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class StaticFieldDef {
    private Field field;

    public StaticFieldDef(Class<?> cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public Class<?> type() {
        return field.getType();
    }

    public <T> T get() {
        Object obj = null;
        try {
            obj = this.field.get(null);
        } catch (Exception e) {
            //Ignore
        }
        return (T) obj;
    }

    public void set(Object obj) {
        try {
            this.field.set(null, obj);
        } catch (Exception e) {
            //Ignore
        }
    }
}