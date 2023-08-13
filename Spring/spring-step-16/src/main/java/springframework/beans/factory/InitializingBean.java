package springframework.beans.factory;

public interface InitializingBean {
    /**
     * 在bean对象属性填充后调用
     */
    void afterPropertiesSet() throws  Exception;
}
