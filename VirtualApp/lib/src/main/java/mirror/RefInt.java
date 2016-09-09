package mirror;

import java.lang.reflect.Field;

public class RefInt {
    private Field field;

    public RefInt(Class cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public int get(Object object) {
        try {
            return this.field.getInt(object);
        } catch (Exception e) {
            return 0;
        }
    }

    public void set(Object obj, int intValue) {
        try {
            this.field.setInt(obj, intValue);
        } catch (Exception e) {
            //Ignore
        }
    }
}