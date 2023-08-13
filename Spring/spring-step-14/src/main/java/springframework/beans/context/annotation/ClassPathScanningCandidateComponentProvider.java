package springframework.beans.context.annotation;

import cn.hutool.core.util.ClassUtil;
import springframework.beans.context.stereotype.Component;
import springframework.beans.factory.config.BeanDefinition;



import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathScanningCandidateComponentProvider {
    public Set<BeanDefinition> findCandidateComponents(String basePackage){
        //用来存放BeanDefinition
        Set<BeanDefinition> candidates=new LinkedHashSet<>();
        Set<Class<?>> classes= ClassUtil.scanPackageByAnnotation(basePackage, Component.class);
        for (Class<?> aClass : classes) {
            candidates.add(new BeanDefinition(aClass));
        }
        return candidates;
    }
}
