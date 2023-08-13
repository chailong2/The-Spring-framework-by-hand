package springframework.beans.factory;

import com.sun.istack.internal.Nullable;
import springframework.BeansException;
import springframework.beans.factory.config.BeanFactoryPostProcessor;
import springframework.beans.factory.config.BeanPostProcessor;

import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {
    <T> Map<String, T> getBeansOfType(@Nullable Class<T> var1) throws BeansException;
    String[] getBeanDefinitionNames();
}
