package springframework.beans.factory.config;

import springframework.BeansException;
import org.springframework.lang.Nullable;
import springframework.PropertyValues;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
    @Nullable
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    @Nullable
    default PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        return null;
    }

}
