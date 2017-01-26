package mirror.java.lang;


import java.util.List;

import mirror.RefClass;
import mirror.RefObject;

/**
 * @author Lody
 */
public class ThreadGroup {
    public static Class<?> TYPE = RefClass.load(ThreadGroup.class, java.lang.ThreadGroup.class);
    public static RefObject<List<java.lang.ThreadGroup>> groups;
    public static RefObject<java.lang.ThreadGroup> parent;
}