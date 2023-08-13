package springframework.beans.factory.support;

import springframework.beans.factory.DisposeableBean;
import springframework.beans.factory.ObjectFactory;
import springframework.beans.factory.config.SingletonBeanRegistry;

import javax.naming.Name;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract  class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //一级缓存用来缓存普通对象
    private Map<String,Object> singletonObjects=new ConcurrentHashMap<>();
    //二级缓存，提前暴露对象，没有完全实例化的对象
    protected final Map<String,Object> earlySingletonObjects=new HashMap<>();
    //三级缓存，用来存储代理对象
    private final Map <String, ObjectFactory<?>> singletonFacories=new HashMap<>();
    //注册了销毁方法的Bean集合
    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    @Override
    public Object getSingleton(String Name) {
        Object sinletonObject=singletonObjects.get(Name);
        if(null==sinletonObject){
            sinletonObject=earlySingletonObjects.get(Name);
            //判断三级缓存中是否有对象，如果有，则这个对象就是代理对象，因为只有代理对象才会存入三级缓存中
            //三级缓存中
            if(null==sinletonObject){
                ObjectFactory<?> singletonFactory=singletonFacories.get(Name);
                if(singletonFactory==null){
                    sinletonObject=singletonFactory.getObject();
                    //获得三级缓存中代理对象的真实对象，将其存储在二级缓存中
                    earlySingletonObjects.put(Name,sinletonObject);
                    singletonFacories.remove(Name);
                }
            }
        }
        return sinletonObject;
    }


    @Override
    public void registerSinleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName,singletonObject);
        earlySingletonObjects.remove(beanName);
        singletonFacories.remove(beanName);
    }
    public void addSingletonFactory(String beanName,ObjectFactory<?> sinletonFactory){
        if(!this.singletonObjects.containsKey(beanName)){
            this.singletonFacories.put(beanName,sinletonFactory);
            this.earlySingletonObjects.remove(beanName);
        }
    }
    public void registerDisposableBean(String beanName, DisposeableBean bean) {
            this.disposableBeans.put(beanName, bean);
    }
    public void destroySingleton(String beanName) throws Exception {
        removeSingleton(beanName);
        DisposeableBean disposableBean=(DisposeableBean) this.disposableBeans.remove(beanName);
        disposableBean.destroy();
    }
    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
        }
    }

    public void destroySingletons() throws Exception {
        String[] disposableBeanNames;
        disposableBeanNames = (this.disposableBeans.keySet()).toArray(new String[0]);
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }
    }
}

