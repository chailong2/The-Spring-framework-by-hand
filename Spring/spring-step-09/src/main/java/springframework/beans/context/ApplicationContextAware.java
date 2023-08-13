package springframework.beans.context;

import springframework.BeansException;
import springframework.beans.factory.Aware;

public interface ApplicationContextAware extends Aware {
    void setApplicationContext(ApplicationContext applicationContext)throws BeansException;
}
