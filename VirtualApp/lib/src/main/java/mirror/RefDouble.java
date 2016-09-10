package mirror;

import java.lang.reflect.Field;

public class RefDouble {
    private Field field;

    public RefDouble(Class cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public double get(Object object) {
        try {
            return this.field.getDouble(object);
        } catch (Exception e) {
            return 0;
        }
    }

    public void set(Object obj, double value) {
        try {
            this.field.setDouble(obj, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}