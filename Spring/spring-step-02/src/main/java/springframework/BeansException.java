package springframework;

public class BeansException extends Throwable {
    public BeansException(String instatiationOfBean, ReflectiveOperationException e) {
    }

    public BeansException(String instatiationOfBean) {
    }
}
