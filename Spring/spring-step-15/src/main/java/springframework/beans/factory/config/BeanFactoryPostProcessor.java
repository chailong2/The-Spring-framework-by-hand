package springframework.beans.factory.config;

import springframework.BeansException;
import springframework.beans.factory.ConfigurableListableBeanFactory;

public interface BeanFactoryPostProcessor {
    //在所有BeanDefinition加载后，且将Bean对象实例化之前，提供修改BeanDefinition属性的机制
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)throws BeansException;
}
