import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BeanFactory {
    //ConcurrentHashMap是线程安全的
    private Map<String, BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<>();

    public Object getBean(String name){
        return  beanDefinitionMap.get(name).getBean();
    }
    public void registerBeanDefinition(String name,BeanDefinition beanDefinition){
        beanDefinitionMap.put(name, beanDefinition);
    }
}
