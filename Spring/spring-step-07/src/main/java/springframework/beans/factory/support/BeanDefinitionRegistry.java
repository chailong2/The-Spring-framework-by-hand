package springframework.beans.factory.support;

import springframework.beans.factory.config.BeanDefinition;

public interface BeanDefinitionRegistry {
    //用来注册BeanDefinition
    void registerBeanDefinition(String beanName , BeanDefinition beanDefinition);
    boolean containsBeanDefinition(String beanName);
}
