package springframework.beans.core.io;

public interface ResourceLoader {
    //文件资源位置标识的前缀
    String CLASSPATH_URL_PREFIX="classpath:";
    //获取资源
    Resource getResource(String location);
}
