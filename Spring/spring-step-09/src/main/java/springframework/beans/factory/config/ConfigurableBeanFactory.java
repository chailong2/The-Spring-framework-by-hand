package springframework.beans.factory.config;

import com.sun.istack.internal.Nullable;
import springframework.beans.factory.HierarchicalBeanFactory;

public interface ConfigurableBeanFactory  extends HierarchicalBeanFactory {
    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    void destroySingletons() throws Exception;
    @Nullable
    ClassLoader getBeanClassLoader();
}
