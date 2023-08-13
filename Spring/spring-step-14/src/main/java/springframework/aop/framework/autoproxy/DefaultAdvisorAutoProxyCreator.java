package springframework.aop.framework.autoproxy;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import springframework.BeansException;
import springframework.aop.*;
import springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import springframework.aop.framework.ProxyFactory;
import springframework.beans.factory.BeanFactory;
import springframework.beans.factory.BeanFactoryAware;
import springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;

public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException, springframework.BeansException {
        if(isInfrastructureClass(beanClass)) return null;
        //获得所有切面集合
        Collection<AspectJExpressionPointcutAdvisor> advisors=beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
        for (AspectJExpressionPointcutAdvisor advisor : advisors) {
            ClassFilter classFilter=advisor.getPointcut().getClassFilter();
            //当不是当前类的类过滤器就过滤掉
            if(!classFilter.matches(beanClass))continue;
            AdvisedSupport advisedSupport=new AdvisedSupport();
            //定义目标资源
            TargetSource targetSource=null;
            try{
                targetSource=new TargetSource(beanClass.getDeclaredConstructor().newInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
            advisedSupport.setTargetSource(targetSource);
            advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
            advisedSupport.setMethodMatcher(advisor.getPointcut().getMathodMatcher());
            advisedSupport.setProxyTargetClass(false);
            return new ProxyFactory(advisedSupport).getProxy();
        }
        return null;
    }

    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advice.class.isAssignableFrom(beanClass) || Pointcut.class.isAssignableFrom(beanClass) || Advisor.class.isAssignableFrom(beanClass);
    }



    @Override
    public void setBeanFactory(BeanFactory beanFactory)throws BeansException{
        this.beanFactory=(DefaultListableBeanFactory) beanFactory;
    }
}
