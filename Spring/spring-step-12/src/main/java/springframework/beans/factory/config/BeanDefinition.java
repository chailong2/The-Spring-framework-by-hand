package springframework.beans.factory.config;
import springframework.PropertyValues;

public class BeanDefinition {
    String SCOPE_SINGLETON=ConfigurableBeanFactory.SCOPE_SINGLETON;
    String SCOPE_PROTOTYPE=ConfigurableBeanFactory.SCOPE_PROTOTYPE;
    private Class beanClass;
    private PropertyValues propertyValues;
    private String initMethodName;
    private String destroyMethodName;
    private String scope=SCOPE_SINGLETON;
    //默认是单例模式
    private boolean singleton=true;
    private boolean prototype=false;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
        if(scope.equals("prototype")){
            this.singleton=false;
            this.prototype=true;
        }
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public boolean isPrototype() {
        return prototype;
    }

    public void setPrototype(boolean prototype) {
        this.prototype = prototype;
    }

    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
        this.propertyValues=new PropertyValues();
    }
    public BeanDefinition(Class beanClass ,PropertyValues propertyValues){
        this.beanClass=beanClass;
        this.propertyValues=propertyValues !=null ? propertyValues: new PropertyValues();
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
}
