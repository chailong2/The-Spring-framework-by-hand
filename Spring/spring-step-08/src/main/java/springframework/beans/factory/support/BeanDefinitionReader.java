package springframework.beans.factory.support;

import springframework.BeansException;
import springframework.beans.core.io.Resource;
import springframework.beans.core.io.ResourceLoader;

public interface BeanDefinitionReader {
    //获得BeanDefinitionRegistry用来注册BeanDefinition
    BeanDefinitionRegistry getRegistry();
    //获取ResourceLoader用来加载资源
    ResourceLoader getResourceLoader();
    //这三个方法都是用来加载Bean定义的。其中，第一个方法通过Resource对象来加载Bean定义，
    // 第二个方法通过Resource数组来加载Bean定义，第三个方法通过字符串类型的资源位置来加载Bean定义。
    // 如果加载Bean定义时出现问题，会抛出BeansException异常。
    void loadBeanDefinitions(Resource resource)throws BeansException;
    void loadBeanDefinitions(Resource... resources)throws BeansException;
    void loadBeanDefinitions(String location)throws BeansException;
    void loadBeanDefinitions(String... locations)throws BeansException ;
}
