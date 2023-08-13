package springframework.beans.factory.support;

import com.sun.istack.internal.Nullable;
import springframework.BeansException;
import springframework.beans.factory.FactoryBean;
import springframework.beans.factory.config.BeanDefinition;
import springframework.beans.factory.config.BeanPostProcessor;
import springframework.beans.factory.config.ConfigurableBeanFactory;
import springframework.beans.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    @Override
    public Object getBean(String Name,Object... args) throws BeansException {
        Object bean = getSingleton(Name);
        if (bean != null){
            return bean;
        }
        return doGetBean(Name,args);
    }
    @Override
    @Nullable
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        Object bean=getSingleton(name);
        if (bean != null){
            return bean;
        }
        return doGetBean(name,null);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        Object bean=getSingleton(name);
        if (bean != null && (bean.getClass()==requiredType||requiredType.isAssignableFrom(bean.getClass()))){
            return (T) bean;
        }
        BeanDefinition beanDefinition=getBeanDefinition(name);
        if(beanDefinition.getBeanClass() !=requiredType && requiredType.isAssignableFrom(beanDefinition.getClass()))
            return null;
        return (T) doGetBean(name,null);
    }


    //抽象方法交给子类区实现
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
    protected abstract Object createBean(String beanName ,BeanDefinition beanDefinition,Object[] args)throws BeansException;
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }
    protected <T>T doGetBean(final String name,final Object[] args) throws BeansException {
        Object sharedInstance=getSingleton(name);
        if(sharedInstance!=null){
            //如果是FactoryBean类，则需要调用FactoryBean#getObject
            return (T) getObjectForBeanInstance(sharedInstance,name);
        }
        BeanDefinition beanDefinition=getBeanDefinition(name);
        Object bean=createBean(name,beanDefinition,args);
        return (T) getObjectForBeanInstance(bean,name);
    }
    private Object getObjectForBeanInstance(Object beanInstance, String beanName) throws BeansException {
        if(!(beanInstance instanceof FactoryBean)){
             return beanInstance;
        }
        Object object=getCacheObjectForFactoryBean(beanName);
        if(object ==null){
            FactoryBean<?> factoryBean =(FactoryBean<?>) beanInstance;
            object=getObjectFromFactoryBean(factoryBean,beanName);
        }
        return object;
    }
}
