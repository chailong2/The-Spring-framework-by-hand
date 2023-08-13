package springframework.beans.core.io;

import cn.hutool.core.lang.Assert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DefaultResourceLoader implements ResourceLoader{
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location,"Location must bot be null");
        //如果地址是以我们指定的前缀开头的
        if(location.startsWith(CLASSPATH_URL_PREFIX)){
            //使用ClassPathResource对象来解析Classpath类型的资源
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
        }else{
            try {
                //如果locatino不是ClassPath，则为网络资源
                URL url=new URL(location);
                return new UrlResource(url);
            } catch (MalformedURLException e) {
                //如果两者都不是就是系统文件资源
                return new FileSystemResource(location);
            }
        }
    }
}
