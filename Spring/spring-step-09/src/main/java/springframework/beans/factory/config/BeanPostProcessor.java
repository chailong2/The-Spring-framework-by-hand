package springframework.beans.factory.config;


import springframework.BeansException;

public interface BeanPostProcessor {
    //在Bean对象执行初始化方法之前，执行此方法
    Object postProcessBeforeInitialization(Object bean, String beanName)throws BeansException;
    //在Bean对象执行初始化方法之后，执行此方法
    Object postProcessAfterInitialization(Object bean, String name)throws  BeansException;
}
