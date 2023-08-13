package springframework.bean;

public class Wife {
    private Husband husband;
    private IMother mother;

    public String queryHusband(){
        return "Wife.Husband、Mother.callMother："+mother.callMother();
    }
}
