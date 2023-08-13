package springframework.beans.factory.annotation;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;
import springframework.BeansException;
import springframework.PropertyValues;
import springframework.beans.context.stereotype.Component;
import springframework.beans.factory.BeanFactory;
import springframework.beans.factory.BeanFactoryAware;
import springframework.beans.factory.config.ConfigurableBeanFactory;
import springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import springframework.beans.utils.ClassUtils;
import java.lang.reflect.Field;

@Component
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
    private ConfigurableBeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=(ConfigurableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        //1. 处理注解@Value
        Class<?> clazz= bean.getClass();
        clazz= ClassUtils.isCglibProxy(clazz) ? clazz.getSuperclass() :clazz;
        Field[] declaredFields=clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            Value valueAnnotation= AnnotationUtils.getAnnotation(field,Value.class);;
            if(valueAnnotation!=null){
                String value= valueAnnotation.value();
                value=beanFactory.resolveEmbeddedValue(value);
                BeanUtil.setFieldValue(bean,field.getName(),value);
            }

        }
        //2. 处理注解@Autowired
        for(Field field:declaredFields){
            Autowired autowiredAnnotation=field.getAnnotation(Autowired.class);
            if (autowiredAnnotation!=null) {
                Class<?> fieldType=field.getType();
                String dependentBeanName=null;
                Qualifier qualifierAnnotation=field.getAnnotation(Qualifier.class);
                Object dependBean=null;
                if (qualifierAnnotation!=null) {
                    dependentBeanName=qualifierAnnotation.value();
                    dependBean=beanFactory.getBean(dependentBeanName,fieldType);
                }else {
                    dependBean=beanFactory.getBean(fieldType);
                }
                BeanUtil.setFieldValue(bean,field.getName(),dependBean);
            }
        }
        return pvs;
    }
}
