package springframework.factory.support;

import springframework.BeansException;
import springframework.factory.BeanFactory;
import springframework.factory.config.BeanDefinition;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {
    @Override
    public Object getBean(String Name,Object... args) throws BeansException {
        Object bean = getSingleton(Name);
        if (bean != null){
            return bean;
        }
        BeanDefinition beanDefinition=getBeanDefinition(Name);
        return createBean(Name,beanDefinition,args);
    }
    //抽象方法交给子类区实现
    protected abstract BeanDefinition getBeanDefinition(String beanName)throws BeansException;
    protected abstract Object createBean(String beanName ,BeanDefinition beanDefinition,Object[] args)throws BeansException;
}
