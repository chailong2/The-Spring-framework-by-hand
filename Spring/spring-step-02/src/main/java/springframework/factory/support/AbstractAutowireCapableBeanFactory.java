package springframework.factory.support;

import springframework.BeansException;
import springframework.factory.config.BeanDefinition;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //该方法用于创建单例bean并返回创建的bean
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean=null;
        try{
            bean=beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException  |  IllegalAccessException e) {
            throw new BeansException("Instatiation of bean",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }
}
