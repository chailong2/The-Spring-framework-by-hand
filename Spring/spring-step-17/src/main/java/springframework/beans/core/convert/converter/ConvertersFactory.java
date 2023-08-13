package springframework.beans.core.convert.converter;

public interface ConvertersFactory<S,R> {
    <T extends R> Converter<S,T> getConverter(Class<T> targetType);
}
