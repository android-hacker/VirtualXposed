package mirror;

import java.lang.reflect.Field;

public class BooleanFieldDef {
    private Field field;

    public BooleanFieldDef(Class<?> cls, Field field) throws NoSuchFieldException {
            this.field = cls.getDeclaredField(field.getName());
            this.field.setAccessible(true);
    }

    public boolean get(Object object) {
        try {
            return this.field.getBoolean(object);
        } catch (Exception e) {
            return false;
        }
    }

    public void set(Object obj, boolean value) {
        try {
            this.field.setBoolean(obj, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}