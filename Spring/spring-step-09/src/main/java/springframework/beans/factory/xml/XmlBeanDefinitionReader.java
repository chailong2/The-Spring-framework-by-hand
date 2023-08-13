package springframework.beans.factory.xml;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import springframework.BeansException;
import springframework.PropertyValue;
import springframework.beans.core.io.Resource;
import springframework.beans.core.io.ResourceLoader;
import springframework.beans.factory.config.BeanDefinition;
import springframework.beans.factory.config.BeanReference;
import springframework.beans.factory.support.AbstractBeanDefinitionReader;
import springframework.beans.factory.support.BeanDefinitionRegistry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry){
        super(registry);
    }
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader){
        super(registry,resourceLoader);
    }

    @Override
    public void loadBeanDefinitions(Resource resource) throws BeansException {
        try {
            try (InputStream inputStream=resource.getInputStream()){
                //注册beanDefition
               doLoadBeanDefinitions(inputStream);
            }
        }catch(IOException | ClassNotFoundException | ParserConfigurationException | SAXException e){
            throw  new BeansException("IOException parsing XML document form"+resource,e);
        }
    }

    @Override
    public void loadBeanDefinitions(Resource... resources) throws BeansException {
        for(Resource resource: resources){
            loadBeanDefinitions(resource);
        }
    }

    @Override
    public void loadBeanDefinitions(String location) throws BeansException {
        ResourceLoader resourceLoader=getResourceLoader();
        Resource resource=resourceLoader.getResource(location);
        loadBeanDefinitions(resource);

    }

    @Override
    public void loadBeanDefinitions(String... locations) throws BeansException {
        ResourceLoader resourceLoader=getResourceLoader();
        for(String location: locations){
            Resource resource=resourceLoader.getResource(location);
            loadBeanDefinitions(resource);
        }
    }

    public void doLoadBeanDefinitions(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, BeansException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        NodeList beanNodes = doc.getElementsByTagName("bean");
        for (int i = 0; i < beanNodes.getLength(); i++) {
            Node beanNode = beanNodes.item(i);
            if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
                Element beanElement = (Element) beanNode;
                String id = beanElement.getAttribute("id");
                String name = beanElement.getAttribute("name");
                String className = beanElement.getAttribute("class");
                String init_method=beanElement.getAttribute("init-method");
                String destroy_method=beanElement.getAttribute("destroy-method");
                String beanScope= beanElement.getAttribute("scope");
                Class<?> clazz = Class.forName(className);

                String beanName = id.isEmpty() ? name : id;
                if (beanName.isEmpty()) {
                    beanName = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
                }

                BeanDefinition beanDefinition = new BeanDefinition(clazz);
                beanDefinition.setInitMethodName(init_method);
                beanDefinition.setDestroyMethodName(destroy_method);
                if(!beanScope.isEmpty()){
                    beanDefinition.setScope(beanScope);
                }
                NodeList propertyNodes = beanElement.getElementsByTagName("property");
                for (int j = 0; j < propertyNodes.getLength(); j++) {
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element propertyElement = (Element) propertyNode;
                        String attrName = propertyElement.getAttribute("name");
                        String attrValue = propertyElement.getAttribute("value");
                        String attrRef = propertyElement.getAttribute("ref");
                        Object value = attrRef.isEmpty() ? attrValue : new BeanReference(attrRef);
                        PropertyValue propertyValue = new PropertyValue(attrName, value);
                        beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                    }
                }
                if (getRegistry().containsBeanDefinition(beanName)) {
                    throw new BeansException("Duplicate beanName[" + beanName + "] is not allowed");
                }
                getRegistry().registerBeanDefinition(beanName, beanDefinition);
            }
        }
    }
}
