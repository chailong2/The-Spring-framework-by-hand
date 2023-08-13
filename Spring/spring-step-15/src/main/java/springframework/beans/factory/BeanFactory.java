package springframework.beans.factory;

import springframework.BeansException;

public interface BeanFactory {
    //带构造函数获取bena
    Object getBean(String name,Object... args)throws BeansException;
    Object getBean(String name) throws BeansException;
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;

}
