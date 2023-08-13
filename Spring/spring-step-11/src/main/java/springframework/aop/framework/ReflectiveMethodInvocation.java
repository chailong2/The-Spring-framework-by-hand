package springframework.aop.framework;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
//ReflectiveMethodInvocation是Spring框架中的一个类，用于执行方法调用的封装和处理。它是MethodInvocation接口的默认实现类之一。
// ReflectiveMethodInvocation主要用于在AOP中实现方法的拦截和增强。当使用Spring的AOP功能时，当目标方法被调用时，Spring会创建
// 一个ReflectiveMethodInvocation对象来封装方法调用的上下文信息，包括目标对象、目标方法、参数等。
// ReflectiveMethodInvocation通过反射机制来实际执行目标方法，并在执行前后提供了一些扩展点，如前置通知、后置通知、异常通知等。
// 它还支持方法的动态调用和参数的传递。
// ReflectiveMethodInvocation的主要作用是提供一个统一的方式来处理方法调用，使得AOP的拦截和增强逻辑能够方便地与目标方法的执行进行集成。
// 通过ReflectiveMethodInvocation，可以在不修改原始类的情况下，对方法进行拦截、修改或增强，实现诸如日志记录、性能监控、事务管理等横切
// 关注点的功能。
// 总之，ReflectiveMethodInvocation类在Spring的AOP框架中起到了关键作用，它封装了方法调用的上下文信息，并提供了方法调用的执行和拦截的
// 扩展点，使得AOP的功能能够得以实现。
public class ReflectiveMethodInvocation implements ProxyMethodInvocation {
    private Object target;
    private Method method;
    private Object[] arguments;
    public ReflectiveMethodInvocation(Object target, Method method, Object[] args) {
        this.target=target;
        this.method=method;
        this.arguments=args;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public MethodInvocation invocableClone() {
        return null;
    }

    @Override
    public MethodInvocation invocableClone(Object... objects) {
        return null;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public void setUserAttribute(String s, Object o) {

    }

    @Override
    public Object getUserAttribute(String s) {
        return null;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target,arguments);
    }

    @Override
    public Object getThis() {
        return null;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return null;
    }
}
