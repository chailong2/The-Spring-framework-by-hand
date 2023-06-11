package springframework;

public class BeansException extends Throwable {
    private String  s;
    private Exception e;
    public BeansException(String s) {
        this.s=s;
    }
    public BeansException(String s,Exception e) {
        this.s=s;
        this.e=e;
    }
}
