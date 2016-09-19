package mirror;

import java.lang.reflect.Field;

public class RefFloat {
    private Field field;

    public RefFloat(Class cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public float get(Object object) {
        try {
            return this.field.getFloat(object);
        } catch (Exception e) {
            return 0;
        }
    }

    public void set(Object obj, float value) {
        try {
            this.field.setFloat(obj, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}