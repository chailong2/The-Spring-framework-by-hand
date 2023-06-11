package springframework.factory.support;

import springframework.BeansException;
import springframework.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

public interface InstantiationStrategy {
    Object instatiate(BeanDefinition beanDefinition, String beanName , Constructor ctor, Object[] args) throws BeansException;
}
