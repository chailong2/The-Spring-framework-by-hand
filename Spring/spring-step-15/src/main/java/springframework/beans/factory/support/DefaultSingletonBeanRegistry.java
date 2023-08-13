package springframework.beans.factory.support;

import springframework.beans.factory.DisposeableBean;
import springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract  class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //用来存储单例bean的集合
    private Map<String,Object> singletonObjects=new HashMap<>();
    //注册了销毁方法的Bean集合
    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    @Override
    public Object getSingleton(String Name) {
        return singletonObjects.get(Name);
    }


    @Override
    public void registerSinleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName,singletonObject);
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

