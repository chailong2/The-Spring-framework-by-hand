package springframework.bean;

import springframework.beans.context.stereotype.Component;

import java.util.Random;
@Component("userService")
public class UserService implements IUserService{
    private String token;
    @Override
    public String queryUserInfo(){
        try{
            Thread.sleep(new Random(1).nextInt(100));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "张三，100001，深圳";
    }
    @Override
    public String register(String userName){
        try{
            Thread.sleep(new Random(1).nextInt(100));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "注册用户："+userName+" success!";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "token='" + token + '\'' +
                '}';
    }
}
