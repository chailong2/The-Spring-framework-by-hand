package springframework.factory.support;

import springframework.factory.config.BeanDefinition;

public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName , BeanDefinition beanDefinition);
}
