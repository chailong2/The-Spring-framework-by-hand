package springframework.bean;

public class userService {
    private String uId;
    private UserDao userDao;
    public void queryUserInfo(){
        System.out.println("查询用户信息："+userDao.queryUserName(uId));
    }
}
