package springframework.bean;

public class userService {
    private String name;
    public userService(String name){
        this.name=name;
    }
    public void queryUserInfo(){
        System.out.println("查询用户信息："+name);
    }
    @Override
    public String toString(){
        final StringBuilder sb=new StringBuilder("");
        sb.append("").append(name);
        return sb.toString();
    }
}
