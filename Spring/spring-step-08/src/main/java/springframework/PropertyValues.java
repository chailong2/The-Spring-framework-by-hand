package springframework;

import java.util.ArrayList;
import java.util.List;

public class PropertyValues {
    //这个是存放Bean的属性的List集合
    private final List<PropertyValue> propertyValueList=new ArrayList<>();

    public void addPropertyValue(PropertyValue pv){
        propertyValueList.add(pv);
    }
    //toArray()是Java中的一个方法，它可以将一个列表转换为一个数组。
    //在这里，我们使用了一个空数组作为参数，以告诉Java我们想要将列表转换为一个新的数组
    public PropertyValue[] getPropertyValues(){
        return this.propertyValueList.toArray(new PropertyValue[0]);
    }
    public PropertyValue getPropertyValue(String propertyName){
        for(PropertyValue pv:this.propertyValueList){
            if(pv.getName().equals(propertyName)){
                return pv;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "PropertyValues{" +
                "propertyValueList=" + propertyValueList +
                '}';
    }
}
