package mirror.java.lang;


import mirror.RefClass;
import mirror.RefObject;

/**
 * @author Lody
 */
public class ThreadGroupN {
    public static Class<?> Class = RefClass.load(ThreadGroupN.class, java.lang.ThreadGroup.class);
    public static RefObject<Integer> ngroups;
    public static RefObject<java.lang.ThreadGroup[]> groups;
    public static RefObject<java.lang.ThreadGroup> parent;
}