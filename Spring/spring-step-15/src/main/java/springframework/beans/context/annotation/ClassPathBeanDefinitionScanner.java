package springframework.beans.context.annotation;

import cn.hutool.core.util.StrUtil;
import springframework.beans.context.stereotype.Component;
import springframework.beans.factory.config.BeanDefinition;
import springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Set;

public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider{
    private BeanDefinitionRegistry registry;

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry){
        this.registry=registry;
    }

    public void doScan(String... basePackages){
        for(String basePackage : basePackages){
            //扫描指定路径下的注解，并获取所有的Bean定义
            Set<BeanDefinition> candidates=findCandidateComponents(basePackage);
            for (BeanDefinition beanDefinition : candidates) {
                //解析Bean对象作用域Singleton、prototype
                String beanScope=resolveBeanScope(beanDefinition);
                if (StrUtil.isNotEmpty(beanScope)) {
                    beanDefinition.setScope(beanScope);
                }
                registry.registerBeanDefinition(determineBeanName(beanDefinition),beanDefinition);
            }
        }
    }

    private String resolveBeanScope(BeanDefinition beanDefinition){
        Class<?> beanClass=beanDefinition.getBeanClass();
        Scope scope=beanClass.getAnnotation(Scope.class);
        if(null!=scope) return scope.value();
        return StrUtil.EMPTY;
    }

    private String determineBeanName(BeanDefinition beanDefinition){
        Class<?> beanClass=beanDefinition.getBeanClass();
        Component component=beanClass.getAnnotation(Component.class);
        String value= component.value();
        if (StrUtil.isEmpty(value)) {
            value=StrUtil.lowerFirst(beanClass.getSimpleName());
        }
        return value;
    }
}
