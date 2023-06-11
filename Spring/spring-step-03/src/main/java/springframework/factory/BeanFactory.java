package springframework.factory;

import springframework.BeansException;

public interface BeanFactory {
    //带构造函数获取bena
    Object getBean(String name,Object... args)throws BeansException;
}
