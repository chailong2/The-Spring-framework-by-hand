package springframework.aop.framework;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import springframework.aop.AdvisedSupport;

import java.lang.reflect.Method;

public class Cglib2AopProxy implements AopProxy{
    private  final AdvisedSupport advised;

    public Cglib2AopProxy(AdvisedSupport advised){
        this.advised=advised;
    }
    @Override
    public Object getProxy() {
        Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(advised.getTargetSource().getTarget().getClass());
        System.out.println(advised.getTargetSource().getTarget().getClass());
        enhancer.setInterfaces(advised.getTargetSource().getTarget().getClass().getInterfaces());
        enhancer.setCallback(new DynamicAdvisedInterceptor(advised));
        return enhancer.create();
    }
    private static class DynamicAdvisedInterceptor implements MethodInterceptor{
        private final AdvisedSupport advisedSupport;
        public DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advisedSupport=advised;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            CglibMethodInvocation methodInvocation=new CglibMethodInvocation(advisedSupport.getTargetSource().getTarget(),method,objects,methodProxy);
            if(advisedSupport.getMethodMatcher().matches(method,advisedSupport.getTargetSource().getTarget().getClass())){
                return advisedSupport.getMethodInterceptor().invoke(methodInvocation);
            }
            return methodInvocation.proceed();
        }
    }
    private  static class CglibMethodInvocation extends ReflectiveMethodInvocation{

        public CglibMethodInvocation(Object target, Method method, Object[] args, MethodProxy methodProxy) {
            super(target, method, args);
        }

        @Override
        public Object proceed() throws Throwable {
            return this.getMethod().invoke(this.getTarget(),this.getArguments());
        }
    }
}
