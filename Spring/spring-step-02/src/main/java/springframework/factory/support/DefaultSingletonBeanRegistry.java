package springframework.factory.support;

import springframework.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //用来存储单例bean的集合
    private Map<String,Object> singletonObjects=new HashMap<>();

    @Override
    public Object getSingleton(String Name) {
        return singletonObjects.get(Name);
    }

    @Override
    public void registerSinleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName,singletonObject);
    }
}
