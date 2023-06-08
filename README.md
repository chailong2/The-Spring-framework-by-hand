

# 一、实现一个简单的SpringBean容器

## 1. 容器是什么

Spring Bean容器包含并管理应用对象的配置和生命周期，从这个意义上来讲，它是一种用于承载对象的容器，开发者可以设置每个Bean对象是如何创建的，以及它们是如何互相关联、构建和使用的。使用这些bean对象可以创建一个单独的实例，或者在需要时生成一个新的实例。如果将一个Bean对象交给Spring Bean容器管理，则这个Bean对象会以类似零件的方式被拆解，然后存储到Spring Bean容器的定义中，便于Spring Bean容器管理，这相当于把对象解耦，当一个Bean对象被定义和存储后，会由Spring Bean容器统一进行分配，这个过程包括Bean对象的初始化和属性填充。最终，我们可以完整地使用一个被实例化后的Bean对象。

## 2. 容器设计

我们将可以存储数据的数据结构称为容器，如ArrayList、LinkedList等。在SpringBean容器中我们需要使用对象名称对Bean进行检索，所以这里我们选择使用HashMap作为Bean的存储容器。实现一个SpringBean容器，需要3个部分

- 定义

BeanDefinition是Spring源码中的一个类，我们实现只需要定义一个Object类型用于存储任意类型的对象

![](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/Spring容器流程.jpg)

![](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/step1项目架构.png) 



> 工厂模式：
>
> > **简单工厂模式**：它有一个工厂类，可以根据传入的参数动态决定创建哪种产品类的实例。它的主要问题是，每次需要添加新的产品类时，都需要修改工厂类的代码。
> >
> > ```java
> > public class SimpleFactory {
> >     public static Product createProduct(String type){
> >         if ("A".equals(type)) {
> >             return new ProductA();
> >         } else if ("B".equals(type)) {
> >             return new ProductB();
> >         }
> >         return null;
> >     }
> > }
> > 
> > ```
>
> > **工厂方法模式**：它把实际创建对象的工作推迟到子类中。这样的话，工厂类就成了抽象类，包含了一些抽象方法，这些抽象方法就是工厂方法。每一个子类都会覆盖这个工厂方法，用于创建具体的产品
> >
> > ```java
> > public abstract class AbstractFactory {
> >     public abstract Product createProduct();
> > }
> > 
> > public class ConcreteFactoryA extends AbstractFactory {
> >     @Override
> >     public Product createProduct() {
> >         return new ProductA();
> >     }
> > }
> > 
> > public class ConcreteFactoryB extends AbstractFactory {
> >     @Override
> >     public Product createProduct() {
> >         return new ProductB();
> >     }
> > }
> > 
> > ```

**实现bean对象的定义**

```java
public class BeanDefinition {
    //定义为Object是因为Object是任何类的父类，可以存储任何类型的对象
    private Object bean;
    public BeanDefinition(Object bean){
        this.bean=bean;
    }
    public Object getBean(){
        return bean;
    }
}
```

**实现BeanFactory的定义**

BeanFactory类是用于生成和使用对象的Bean工厂，BeanFactory类的实现过程包括Bean对象的注册和获取，这里注册的是Bean对象的定义信息。

```java
public class BeanFactory {
    //ConcurrentHashMap是线程安全的
    private Map<String, BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<>();
    
    public Object getBean(String name){
        return  beanDefinitionMap.get(name).getBean();
    }
    public void registerBeanDefinition(String name,BeanDefinition beanDefinition){
        beanDefinitionMap.put(name, beanDefinition);
    }
}
```

**测试**

> 定义userservice

```java
public class Userservice {
    public void queryUerInfo(){
        System.out.println("查询用户信息");
    }
}
```

> 定义测试类

```java
public class ApiTest {
    @Test
    public void test_BeanFactory(){
        //初始化BeanFactory
        BeanFactory beanFactory=new BeanFactory();

        //注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(new Userservice());
        beanFactory.registerBeanDefinition("userservice",beanDefinition);

        //获取bean对象
        Userservice userservice= (Userservice) beanFactory.getBean("userservice");
        userservice.queryUerInfo();
    }
}
```

![image-20230606104923973](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/image-20230606104923973.png)

# 二、实现Bean对象的定义、注册和获取



## 1. 本节重点

我们使用AbstractBeanFactory抽象类，运用模版模式拆分功能，解耦Spring Bean容器，处理界限上下文关系，完成BeanFactory接口的实现。

## 2. 容器的思考

首先、应该通过SpringBean容器创建Bean对象，而不是在调用时传递一个完成了实例化Bean对象，然后，还需要考虑单例对象，在二次获取对象时，可以从内存中获取。此外，不仅要实现功能，还需要完善基础容器框架的类结构体，否则很难讲其他的功能添加进去。下面使用设计模式来开发Spring Bean容器的功能结构，安装类的功能拆分出不同的接口和实现类

## 3. 完善容器的设计

需要先完善Spring Bean容器。在注册Bean对象时，只注册一个类信息，而不直接将实例化信息注册到SpringBean容器中。这里首先需要将BeanDefinition类中的Object属性修改为Class类，然后在获取Bean对象时进行Bean对象的实例化，以及判断当前实例对象在容器中是否已经被缓存。

![image-20230606104923973](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/2-1.jpg)

下面时项目结构

![](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/step2.png)

- BeanFacactory：是通过AbstractBeanFactory抽象类实现的getBean方法来定义的
- AbstractBeanFactory：该抽象类继承了SinletonBeanRegistry接口的DefaultSingletonBeanRegistry类，所以AbstractFactory抽象类就具备了单例bean对象的注册功能
- AbstractBeanFactory定义了两个抽象方法-getBeanDefinition(Stirng beanName)和createBean(String beanName,BeanDefinition)，它们分别由DefaultListableBeanFactory类和AbstractAutowireCapableBeanFactory类实现
- 最终，DefaultListableBeanFactory类还会继承抽象类AbstractAutowireCapabelBeanFactory，因此可以调用该抽象类中的creatBean方法



**BeanDefinition类的定义**

```java
public class BeanDefinition {
    private Class beanClass;

    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}
```

> 在对Bean对象定义类中，将原来的ObjectBean替换为Class，这样就可以将Bean对象的实例化放在容器中处理



**单例对象注册接口的定义和实现**

```java
public interface SingletonBeanRegistry {
    //根据名称获得单例bean
    Object getSingleton(String Name);
    //注册单例bean的方法
    void registerSinleton(String beanName , Object singletonObject)
    
}
```

> 主要用于定义一个注册和获取单例bean的接口

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //用来存储单例bean的集合
    private Map<String,Object> singletonObjects=new HashMap<>();

    @Override
    public Object getSingleton(String Name) {
        return singletonObjects.get(Name);
    }

    @Override
    public void registerSinleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName,singletonObject);
    }
}
```

> 该类主要实现获取单例对象的方法和注册单例对象的方法，这两个方法都可以被继承此类的其他类调用



**抽象类定义模版**

```java
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {


    @Override
    public Object getBean(String Name) throws BeansException {
        Object bean = getSingleton(Name);
        if (bean != null){
            return bean;
        }
        BeanDefinition beanDefinition=getBeanDefinition(Name);
        return createBean(Name,beanDefinition);
    }
    //抽象方法交给子类区实现
    protected abstract BeanDefinition getBeanDefinition(String beanName)throws BeansException;
    protected abstract Object createBean(String beanName ,BeanDefinition beanDefinition)throws BeansException;
}

```

> 上述代码运用模版式定义了一个流程标准的用户获取对象的的AbstractBeanFactory抽象类，并采用职责分类的结构设计，继承DefaultSingletonBeanRegistry类，使用其提供的单例对象注册和获取功能，通过BeanFactory接口提供一个功能单一的方法，屏蔽了内部实现的细节。这里的BeanFactory接口提供了一个获取对象的方法getBean，然后由抽象类实现细节。在getBean方法中可以看到，它主要用于获取单例Bean对象，以及在无法获取Bean对象时做出相应的Bean对象实例化。getBean自身并没有实现这些方法，只是定义了调用过程并提供了抽象方法，由此实现抽象类中其它方法的相应功能。

**实例化bean对象（AbstractAutowireCapaleBeanFactory）**

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //该方法用于创建单例bean并返回创建的bean
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean=null;
        try{
            bean=beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException  |  IllegalAccessException e) {
            throw new BeansException("Instatiation of bean",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }
}
```

> 继续细分功能职责，AbstractAutowireCapaleBeanFactory抽象类继承AbstractBeanFactory类，用于实现创建对象的具体功能。因为它是一个抽象类，所以可以只实现其中的部分抽象类的接口。另外，用于实现Bean对象实例化的newInstance方法中存在着一个问题，如果对象中含有带入参信息的构造函数，该如何处理。在完成Bean对象的实例化后，可以直接调用registerSinleton方法，将单例对象存储在缓冲中。

**核心类实现（DefaultListableBeanFactory）**

```java

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {
    private Map<String, BeanDefinition> beanDefinitionMap =new HashMap<>();
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition=beanDefinitionMap.get(beanName);
        if(beanDefinition==null){
            throw new BeansException("No bean named '"+beanName+"' is defined");
        }
        return beanDefinition;
    }
}
```

**Bean生命周期测试**



> 定义userservice

```java
public class Userservice {
    public void queryUerInfo(){
        System.out.println("查询用户信息");
    }
}
```

> 定义测试类

```java
public class ApiTest {
    @Test
    public void test_BeanFactory() throws BeansException {
        //1. 初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //2. 注册bean对象
        BeanDefinition beanDefinition=new BeanDefinition(Userservice.class);
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //3. 获取bean对象
        Userservice userservice= (Userservice) beanFactory.getBean("userService");
        userservice.queryUserInfo();
        //4.再次获取和调用对象
        Userservice userservice_singleton=(Userservice) beanFactory.getBean("userService");
        userservice_singleton.queryUserInfo();
        System.out.println("两个bean是否为同一个bean："+(userservice_singleton.equals(userservice)));
        System.out.println("userservice："+userservice.hashCode());
        System.out.println("userservice_singleton："+userservice_singleton.hashCode());
    }
}
```



![](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/2-1-ex.png)

 上面结果中第一次获取bean是创建的，第二次是从缓存中获取的



> 对于SpringBean容器的实现类，重点关注类之间的职责和关系，几乎所有的程序的功能设计都离不开接口、抽象类的实现和继承。使用这些特性不同的e

# 三、基于Cglib实现含构造函数的类实例化策略

## 1. 本节重点

前面介绍了Spring Bean容器的功能，可以把实例化对象交给容器进行统一处理。但是在实例化对象的代码中，并没有考虑类中是否含有带入参信息的构造函数。

## 2. 实例化策略设计

解决上述问题主要考虑两个方面，一方面是如何将构造函数的入参信息合理地传递到实例化中，另一方面是如何将含有入参信息的构造函数的类实例化。

![](/Users/jackchai/Desktop/自学笔记/java项目/手写Spring框架/img/3-1.jpg)

参考Spring Bean容器的源码实现方式，在BeanFactory类中添加Object getBean(String name,Object...args)接口，这样就可以在获取Bean对象时将构造函数的入参信息传递进去。那么使用什么方式可以创建包含构造函数的Bean对象呢？这里有两种方式可以选择，一种是基于Java本身自带的方法DeclaredConstructor，另一种是使用Cglib动态创建Bean对象，因为Cglib本身是基于ASM字节码框架实现的，所以可以直接通过ASM指令码来创建对象
