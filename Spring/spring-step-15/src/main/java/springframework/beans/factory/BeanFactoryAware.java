package springframework.beans.factory;

import springframework.BeansException;

public interface BeanFactoryAware extends Aware{
    void setBeanFactory(BeanFactory beanFactory)throws BeansException;
}
