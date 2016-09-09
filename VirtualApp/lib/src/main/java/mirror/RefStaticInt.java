package mirror;

import java.lang.reflect.Field;

public class RefStaticInt {
    private Field field;

    public RefStaticInt(Class<?> cls, Field field) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public int get() {
        try {
            return this.field.getInt(null);
        } catch (Exception e) {
            return 0;
        }
    }

    public void set(int value) {
        try {
            this.field.setInt(null, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}