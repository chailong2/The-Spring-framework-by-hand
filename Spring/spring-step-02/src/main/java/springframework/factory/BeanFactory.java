package springframework.factory;

import springframework.BeansException;

public interface BeanFactory {
    //根据名称获得单例bean
    Object getBean(String Name) throws BeansException;
}
