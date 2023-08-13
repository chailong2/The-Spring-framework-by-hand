package springframework.bean;

import springframework.BeansException;
import springframework.beans.context.ApplicationContext;
import springframework.beans.context.ApplicationContextAware;
import springframework.beans.factory.*;

public class userService implements InitializingBean, DisposeableBean , BeanNameAware, BeanClassLoaderAware, ApplicationContextAware
, BeanFactoryAware {
    private ApplicationContext applicationContext;
    private  BeanFactory beanFactory;
    private String uId;
    private String company;
    private String location;
    private UserDao userDao;
    public void queryUserInfo(){
        System.out.println("用户姓名："+userDao.queryUserName(uId));
        System.out.println("用户公司："+company);
        System.out.println("用户ID："+uId);
        System.out.println("用户地址："+location);
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("执行：UserService.destroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("执行：UserService.afterPropertiesSet");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("classLoader:"+classLoader);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=beanFactory;
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("Bean name is:"+name);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
