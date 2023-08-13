package springframework.aop;

public class TargetSource {

    private Object target;
    public TargetSource(){

    }
    public TargetSource(Object target){
       this.target=target;
    }
    public Class<?>[] getTargetClass() {
        return new Class[]{target.getClass()};
    }

    public Object getTarget(){
        return target;
    }
}
