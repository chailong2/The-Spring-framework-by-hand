package springframework.beans.factory.support;

import cn.hutool.core.lang.Assert;
import springframework.BeansException;
import springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

public  class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {
    private Map<String, BeanDefinition> beanDefinitionMap =new HashMap<>();
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition=beanDefinitionMap.get(beanName);
        if(beanDefinition==null){
            throw new BeansException("No bean named '"+beanName+"' is defined");
        }
        return beanDefinition;
    }
}
