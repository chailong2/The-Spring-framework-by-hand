package springframework.beans.utils;

import com.sun.istack.internal.Nullable;

public abstract class ClassUtils {
    public static final String CGLIB_CLASS_SEPARATOR = "$$";
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            //获取当前线程的上下文类加载器
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // 这行代码的作用是获取ClassUtils类的类加载器
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    //获得系统的默认的类加载器
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
    public static boolean isCglibProxy(Object object) {
        return isCglibProxyClass(object.getClass());
    }
    public static boolean isCglibProxyClass(@Nullable Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }
    public static boolean isCglibProxyClassName(@Nullable String className) {
        return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
    }
}
