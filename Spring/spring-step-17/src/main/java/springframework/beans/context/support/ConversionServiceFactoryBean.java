package springframework.beans.context.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;
import springframework.beans.core.convert.converter.ConvertersFactory;
import springframework.beans.factory.InitializingBean;


import java.util.Set;

public class ConversionServiceFactoryBean implements FactoryBean<ConversionService>, InitializingBean {
    private Set<?> converters;

    @Nullable
    private DefaultConversionService conversionService;

    @Override
    public ConversionService getObject() throws Exception {
        return conversionService;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.conversionService= new DefaultConversionService();
        registerConverters(converters,conversionService);

    }
    private void registerConverters(Set<?> converters, ConverterRegistry registry){
        if(converters!=null){
            for (Object converter : converters) {
                if(converter instanceof GenericConverter){
                    registry.addConverter((GenericConverter) converter);
                }else if(converter instanceof Converter<?,?>){
                    registry.addConverter((Converter<?, ?>) converter);
                }else if(converter instanceof ConvertersFactory<?,?>){
                    registry.addConverterFactory((ConverterFactory<?, ?>) converter);
                }else {
                    throw new IllegalArgumentException("Each converter object must implement one of the Converter.ConverterFactory,or GenericConverter interafaces")
                }
            }
        }
    }
    public void setConverters(Set<?> converters){
        this.converters=converters;
    }
}
