package mirror;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class RefStaticMethod<T> {
    private Method method;

    public RefStaticMethod(Class<?> cls, Field field) throws NoSuchMethodException {
        if (field.isAnnotationPresent(MethodParams.class)) {
            Class<?>[] types = field.getAnnotation(MethodParams.class).value();
            for (int i = 0; i < types.length; i++) {
                Class<?> clazz = types[i];
                if (clazz.getClassLoader() == getClass().getClassLoader()) {
                    try {
                        Class.forName(clazz.getName());
                        Class<?> realClass = (Class<?>) clazz.getField("TYPE").get(null);
                        types[i] = realClass;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            this.method = cls.getDeclaredMethod(field.getName(), types);
            this.method.setAccessible(true);
        } else if (field.isAnnotationPresent(MethodReflectParams.class)) {
            boolean arrayset=false;
            String[] typeNames = field.getAnnotation(MethodReflectParams.class).value();
            Class<?>[] types = new Class<?>[typeNames.length];
            Class<?>[] types2 = new Class<?>[typeNames.length];
            for (int i = 0; i < typeNames.length; i++) {
                Class<?> type = getProtoType(typeNames[i]);
                if (type == null) {
                    try {
                        type = Class.forName(typeNames[i]);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                types[i] = type;
                if("java.util.HashSet".equals(typeNames[i])){
                    arrayset=true;
                    Class<?> type2 =type;
                    try {
                        type2 = Class.forName("android.util.ArraySet");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(type2 != null) {
                        types2[i] = type2;
                    }else{
                        types2[i] = type;
                    }
                }else{
                    types2[i] = type;
                }
            }
            try {
                this.method = cls.getDeclaredMethod(field.getName(), types);
            }catch (Exception e){
                e.printStackTrace();
                if(arrayset){
                    this.method = cls.getDeclaredMethod(field.getName(), types2);
                }
            }
            this.method.setAccessible(true);
        } else {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.getName().equals(field.getName())) {
                    this.method = method;
                    this.method.setAccessible(true);
                    break;
                }
            }
        }

        if (this.method == null) {
            throw new NoSuchMethodException(field.getName());
        }
    }

    static Class<?> getProtoType(String typeName) {
        if (typeName.equals("int")) {
            return Integer.TYPE;
        }
        if (typeName.equals("long")) {
            return Long.TYPE;
        }
        if (typeName.equals("boolean")) {
            return Boolean.TYPE;
        }
        if (typeName.equals("byte")) {
            return Byte.TYPE;
        }
        if (typeName.equals("short")) {
            return Short.TYPE;
        }
        if (typeName.equals("char")) {
            return Character.TYPE;
        }
        if (typeName.equals("float")) {
            return Float.TYPE;
        }
        if (typeName.equals("double")) {
            return Double.TYPE;
        }
        if (typeName.equals("void")) {
            return Void.TYPE;
        }
        return null;
    }


    public T call(Object... params) {
        T obj = null;
        try {
            obj = (T) method.invoke(null, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public T callWithException(Object... params) throws Throwable {
        try {
            return (T) this.method.invoke(null, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }
}
