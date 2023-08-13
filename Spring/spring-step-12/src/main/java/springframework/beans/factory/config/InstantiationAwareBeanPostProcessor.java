package springframework.beans.factory.config;

import springframework.BeansException;
import springframework.PropertyValues;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor{
    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException, springframework.BeansException;
    default PropertyValues postProcessPropertyValues(
            PropertyValues pvs, Object bean, String beanName) throws BeansException {

        return pvs;
    }
}
