package springframework.beans.factory.support;

import cn.hutool.core.lang.Assert;
import springframework.BeansException;
import springframework.beans.factory.ConfigurableListableBeanFactory;
import springframework.beans.factory.config.BeanDefinition;
import springframework.beans.factory.config.BeanPostProcessor;
import java.util.HashMap;
import java.util.Map;

public  class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry, ConfigurableListableBeanFactory {
    private Map<String, BeanDefinition> beanDefinitionMap =new HashMap<>();

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

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
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition=beanDefinitionMap.get(beanName);
        if(beanDefinition==null){
            throw new BeansException("No bean named '"+beanName+"' is defined");
        }
        return beanDefinition;
    }

    @Override
    public void preInstantiateSingletons() {

    }


    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> var1) throws BeansException {
        Map<String, T> result =new HashMap<>();
        for(String  beanName: beanDefinitionMap.keySet()){
            if(beanDefinitionMap.get(beanName).getBeanClass() == var1||var1.isAssignableFrom(beanDefinitionMap.get(beanName).getBeanClass())){
                result.put(beanName, (T) getBean(beanName));
            }
        }
        return result;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.keySet().size()]);
    }

    @Override
    public void destroySingletons() throws Exception {
        super.destroySingletons();
    }
}
