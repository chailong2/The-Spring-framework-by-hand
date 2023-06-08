public class BeanDefinition {
    //定义为Object是因为Object是任何类的父类，可以存储任何类型的对象
    private Object bean;
    public BeanDefinition(Object bean){
        this.bean=bean;
    }
    public Object getBean(){
        return bean;
    }
}
