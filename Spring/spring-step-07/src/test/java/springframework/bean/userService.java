package springframework.bean;

import springframework.beans.factory.DisposeableBean;
import springframework.beans.factory.InitializingBean;

public class userService implements InitializingBean, DisposeableBean {
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
}
