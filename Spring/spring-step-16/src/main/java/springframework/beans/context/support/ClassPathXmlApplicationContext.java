package springframework.beans.context.support;

import springframework.BeansException;

public class ClassPathXmlApplicationContext extends  AbstractXmlApplicationContext{
    private String[] configLocations;
    @Override
    protected String[] getConfigLocations() {
        return configLocations;
    }
    public ClassPathXmlApplicationContext(String configLocations) throws BeansException {
        this(new String[]{configLocations});
    }
    public  ClassPathXmlApplicationContext(){
    }
    public ClassPathXmlApplicationContext(String[] configLocations)throws BeansException{
        this.configLocations=configLocations;
        refresh();
    }


}
