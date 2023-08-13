package springframework.beans.factory.support;

import springframework.BeansException;
import springframework.beans.factory.FactoryBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry{
    //这个是用来缓冲单例bean的集合
    private final Map<String,Object> factoryBeanObjectCache=new ConcurrentHashMap<String,Object>();
    protected Object getCacheObjectForFactoryBean(String beanName){
        Object object=this.factoryBeanObjectCache.get(beanName);
        return (object !=null ? object :null);
    }

    protected Object getObjectFromFactoryBean(FactoryBean factory,String beanName) throws BeansException {
        if(factory.isSingleton()){
            Object object=this.factoryBeanObjectCache.get(beanName);
            if(object==null){
                object=doGetObjectFromFactoryBean(factory,beanName);
                this.factoryBeanObjectCache.put(beanName,(object!=null?object:null));
            }
            return object;
        }else {
            return doGetObjectFromFactoryBean(factory,beanName);
        }
    }
    private Object doGetObjectFromFactoryBean(final  FactoryBean factory,final String beanName) throws BeansException {
        try {
            return factory.getObject();
        }catch(Exception e){
            throw new BeansException("FactoryBean threw exception on object["+beanName+"] creation",e);
        }
    }
}
