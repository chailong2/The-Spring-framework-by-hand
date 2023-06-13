

# 一、实现一个简单的SpringBean容器

## 1. 容器是什么

Spring Bean容器包含并管理应用对象的配置和生命周期，从这个意义上来讲，它是一种用于承载对象的容器，开发者可以设置每个Bean对象是如何创建的，以及它们是如何互相关联、构建和使用的。使用这些bean对象可以创建一个单独的实例，或者在需要时生成一个新的实例。如果将一个Bean对象交给Spring Bean容器管理，则这个Bean对象会以类似零件的方式被拆解，然后存储到Spring Bean容器的定义中，便于Spring Bean容器管理，这相当于把对象解耦，当一个Bean对象被定义和存储后，会由Spring Bean容器统一进行分配，这个过程包括Bean对象的初始化和属性填充。最终，我们可以完整地使用一个被实例化后的Bean对象。

## 2. 容器设计

我们将可以存储数据的数据结构称为容器，如ArrayList、LinkedList等。在SpringBean容器中我们需要使用对象名称对Bean进行检索，所以这里我们选择使用HashMap作为Bean的存储容器。实现一个SpringBean容器，需要3个部分

- 定义

BeanDefinition是Spring源码中的一个类，我们实现只需要定义一个Object类型用于存储任意类型的对象

![](./img/Spring容器流程.jpg)

![](./img/step1项目架构.png) 



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

![image-20230606104923973](./img/image-20230606104923973.png)

# 二、实现Bean对象的定义、注册和获取



## 1. 本节重点

我们使用AbstractBeanFactory抽象类，运用模版模式拆分功能，解耦Spring Bean容器，处理界限上下文关系，完成BeanFactory接口的实现。

## 2. 容器的思考

首先、应该通过SpringBean容器创建Bean对象，而不是在调用时传递一个完成了实例化Bean对象，然后，还需要考虑单例对象，在二次获取对象时，可以从内存中获取。此外，不仅要实现功能，还需要完善基础容器框架的类结构体，否则很难讲其他的功能添加进去。下面使用设计模式来开发Spring Bean容器的功能结构，安装类的功能拆分出不同的接口和实现类

## 3. 完善容器的设计

需要先完善Spring Bean容器。在注册Bean对象时，只注册一个类信息，而不直接将实例化信息注册到SpringBean容器中。这里首先需要将BeanDefinition类中的Object属性修改为Class类，然后在获取Bean对象时进行Bean对象的实例化，以及判断当前实例对象在容器中是否已经被缓存。

![image-20230606104923973](./img/2-1.jpg)

下面时项目结构

![](./img/step2.png)

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



![](./img/2-1-ex.png)

 上面结果中第一次获取bean是创建的，第二次是从缓存中获取的



> 对于SpringBean容器的实现类，重点关注类之间的职责和关系，几乎所有的程序的功能设计都离不开接口、抽象类的实现和继承。使用这些特性不同的e

# 三、基于Cglib实现含构造函数的类实例化策略

## 1. 本节重点

前面介绍了Spring Bean容器的功能，可以把实例化对象交给容器进行统一处理。但是在实例化对象的代码中，并没有考虑类中是否含有带入参信息的构造函数。

## 2. 实例化策略设计

解决上述问题主要考虑两个方面，一方面是如何将构造函数的入参信息合理地传递到实例化中，另一方面是如何将含有入参信息的构造函数的类实例化。

![](./img/3-1.jpg)

参考Spring Bean容器的源码实现方式，在BeanFactory类中添加Object getBean(String name,Object...args)接口，这样就可以在获取Bean对象时将构造函数的入参信息传递进去。那么使用什么方式可以创建包含构造函数的Bean对象呢？这里有两种方式可以选择，一种是基于Java本身自带的方法DeclaredConstructor，另一种是使用Cglib动态创建Bean对象，因为Cglib本身是基于ASM字节码框架实现的，所以可以直接通过ASM指令码来创建对象

## 3. 实例化策略代码实现

代码的目录结构如下：

![](./img/3-1test.png)

本节的核心是在现有工程中添加InstantiationStrategy实例化策略接口、开发对应的JDK和cglib实例化方式，以及补充相应的含有带入参信息的getBean构造函数。当进行外部调用时，可以传递构造函数的入参信息并进行实例化

**新增getBean接口**

```java
public interface BeanFactory {
    //不带构造函数获取bean
    Object getBean(String name) throws BeansException ;
    //带构造函数获取bena
    Object getBean(String name,Object... args)throws BeansException;
}
```

**定义实例化策略接口**

```java
public interface InstantiationStrategy {
    Object instatiate(BeanDefinition beanDefinition, String beanName , Constructor ctor, Object[] args);
}
```

**JDK实例化**

```java
public class SimpleInstantiatioinStrategy implements InstantiationStrategy{

    @Override
    public Object instatiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Class clazz=beanDefinition.getBeanClass();
        try{
            //如果构造函数不为空
            if(ctor !=null){
                //直接使用反射创建代理对象
                return clazz.getDeclaredConstructor(ctor.getParameterTypes()).newInstance(args);
            }else {
                //构造函数为空则使用默认无参构造函数创建bean实例
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new BeansException("Failed to instantiate  ["+ clazz.getName()+"]");
        }
    }
}
```

**Cglib实例化**

```java
public class CglibSubclassingInstantiationStrategy implements InstantiationStrategy {

    @Override
    public Object instatiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        //定义增强器
        Enhancer enhancer=new Enhancer();
        //定义代理类所继承的父类
        enhancer.setSuperclass(beanDefinition.getBeanClass());
        //这段代码的作用是创建一个名为"enhancer"的对象，并为其设置一个回调函数（callback），
        // 该回调函数被命名为"NoOp"。在这个回调函数中，重写了hashCode()方法，但是没有实际的实现。
        // 这个回调函数的作用是在enhancer对象执行某些操作时被调用，但是它并不会对操作产生任何影响。
        enhancer.setCallback(new NoOp() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });
        if(null==ctor){
            //创建目标类的增加实例
            return enhancer.create();
        }
        //有参构造
        return enhancer.create(ctor.getParameterTypes(),args);
    }
}
```

**创建策略调用**

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();
    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition,Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition,String beanName,Object[] args) throws BeansException {
        //创建构造函数对象
        Constructor constructorTOuser=null;
        //获得BeanDefinition的类型
        Class<?> beanClass=beanDefinition.getBeanClass();
        //获得Bean的所有构造函数
        Constructor<?>[] declaredConstructors=beanClass.getDeclaredConstructors();
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            //按照提供的参数类型获得指定的构造函数
            if(args!=null && declaredConstructor.getParameterTypes().length==args.length){
                constructorTOuser=declaredConstructor;
                break;
            }
        }
        return instantiationStrategy.instatiate(beanDefinition,beanName,constructorTOuser,args);
    }
}
```

**测试**

> 准备Userservice

```java
public class Userservice {
    private String name;
    public Userservice(String name){
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
```

> 准备测试类

```java
public class Userservice {
    private String name;
    public Userservice(String name){
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
```

> 测试结果

![](./img/3-2test.png)



****

## 4. Spring相应源码解析

- BeanFactory

> BeanFactory是一个核心接口，它是Spring框架中的一个重要组件。它的作用是创建和管理Bean对象，这些Bean对象是应用程序中的组件，可以在运行时动态地创建和销毁。BeanFactory提供了一种简单的方式来管理应用程序中的对象，它可以自动装配对象之间的依赖关系，从而使应用程序更加灵活和可扩展。

```java
public interface BeanFactory {   
  Object getBean(String name) throws BeansException;

	//返回指定类型的bean，如果不是参数中所指定的类型的bean，Spring 将尝试进行类型转换，如果无法转换，将会抛出一个异常
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	//该方法用于从 Spring IoC 容器中获取一个特定名称的 Bean，并且可以提供构造参数或工厂方法参数以在获取 Bean 的同时进行实例化
	Object getBean(String name, Object... args) throws BeansException;

	//不提供bean的名称，获得指定的类型的bean
	<T> T getBean(Class<T> requiredType) throws BeansException;
    //带构造参数创建bean，之所以带构造参数，是因为如果我们获取bean时，可以根据参数创建（主要是用于bean懒加载的时候）
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
}
```

- AbstractBeanFactory

```java
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {
  
  //根据bean的名称获取bean
	@Override
	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}
    //获得指定类型的bean
	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return doGetBean(name, requiredType, null, false);
	}
    //获得指定名称的bean，并传入构造函数参数
	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		return doGetBean(name, null, args, false);
	}
   //超着指定类型的bean，并传入指定的构造函数
	public <T> T getBean(String name, @Nullable Class<T> requiredType, @Nullable Object... args)
			throws BeansException {

		return doGetBean(name, requiredType, args, false);
	}
  //这个方法是BeanFactory接口中的一个方法，用于获取Bean实例。它接收四个参数：Bean的名称或ID、Bean的类型、构造函数参数数组和一个标志，用于指示是否只检查Bean是否存在。如果指定的Bean存在，则返回Bean对象；如果不存在，则返回null。如果要获取指定类型的Bean，则可以使用 requiredType 参数；如果要传递构造函数参数，则可以使用 args 参数
  protected <T> T doGetBean(
			String name, @Nullable Class<T> requiredType, @Nullable Object[] args, boolean typeCheckOnly)
			throws BeansException {
    //在 Spring 中，Bean 名称可以包含特殊字符，比如点号（.）和斜杠（/），这些特殊字符在 Bean 的定义中具有特殊的含义。
		// 为了避免这些特殊字符带来的问题，Spring 使用了一个默认的分隔符（默认为点号），将 Bean 名称中的特殊字符替换成默认分隔符。
		String beanName = transformedBeanName(name);
		Object bean;

		//调用DefaultSingletonBeanRegistry的getSingleton方法，如果是单例就直接获取（单例bean从缓存中获取）
		Object sharedInstance = getSingleton(beanName);
		//如果获取到了单例bean，且传入的构造函数为空
		if (sharedInstance != null && args == null) {
			//判断是否开启类Trace级别的日志记录
			if (logger.isTraceEnabled()) {
				//判断该单例对象是否正在创建
				if (isSingletonCurrentlyInCreation(beanName)) {
					//记录日志（提示该bean是一个早期引用还没被初始化）
					logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					//提示该bean是一个缓冲实例（已经被初始化类）
					logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			//getObjectForBeanInstance  方法是 AbstractAutowireCapableBeanFactory 类的一个私有方法，
			// 用于从 Bean 实例中获取对应的 Bean 对象。
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}
		//如果没有找到指定的单例bean对象或存在构造参数
		else {
			//它首先检查指定名称的Bean单例实例是否正在创建中，如果是，则抛出BeanCurrentlyInCreationException异常。
			// 这是为了避免在Bean实例创建期间发生循环依赖或重复创建的情况
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}
      // getParentBeanFactory() 是BeanFactory接口中的一个方法，用于获取当前BeanFactory的父级BeanFactory。在Spring应用程序中，可能会有多个 BeanFactory ，每个BeanFactory都有自己的一组Bean定义。如果当前BeanFactory 无法找到所需的Bean定义，则可以委托给其父级BeanFactory进行查找。通过使用 getParentBeanFactory()方法，可以获取当前BeanFactory的父级BeanFactory ，从而实现Bean定义的继承和重用。
			BeanFactory parentBeanFactory = getParentBeanFactory();
			//如果父工厂不为空，且工厂中有该Bean
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				//转换Bean的名称
				String nameToLookup = originalBeanName(name);
				//如果父工厂的类型是AbstractBeanFactory或是其子类
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					//调用父工厂的doGetBean方法
					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
							nameToLookup, requiredType, args, typeCheckOnly);
				}
				else if (args != null) {
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else if (requiredType != null) {
					// 没有参数就会调用默认的getBean的方法
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					//如果父类工厂不是AbstractBeanFactory或其子类，就会调用这个工厂的getBean方法
					return (T) parentBeanFactory.getBean(nameToLookup);
				}
			}
      //如果不是只检查当前bean是否存在
      if (!typeCheckOnly) {
        //markBeanAsCreated(beanName) 方法用于在当前 BeanFactory 中标记一个Bean已经创建。该方法在Bean创建时由 BeanFactory 内部调用。它以Bean的名称作为参数，并更新内部数据结构以指示该Bean已经被创建。这个方法的作用是防止Bean之间出现循环依赖。当一个Bean被创建时，它可能依赖于其他尚未创建的Bean。 BeanFactory 使用这个方法来跟踪已经创建的Bean，以便正确地解决循环依赖问题。
				markBeanAsCreated(beanName);
			}
      //创建一个 StartupStep 实例，这是Spring的新特性，用于监控应用的启动过程，可以帮助我们更好地理解和优化应用的启动过程
			StartupStep beanCreation = this.applicationStartup.start("spring.beans.instantiate")
					.tag("beanName", name);
			try {
				if (requiredType != null) {
					beanCreation.tag("beanType", requiredType::toString);
				}
				//通过 getMergedLocalBeanDefinition(beanName) 获取Bean的合并后的定义信息，即 RootBeanDefinition
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				//检查获取的 RootBeanDefinition。如果bean定义的信息有问题，比如说定义的类不能被实例化，那么这个方法会抛出异常
				checkMergedBeanDefinition(mbd, beanName, args);
				//获取该beam所依赖的bean
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						//存在循环依赖的问题，抛出异常
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						//将这些依赖关系注册到依赖管理的数据结构中（通过 registerDependentBean(dep, beanName)）
						registerDependentBean(dep, beanName);
						try {
							//获取Bean
							getBean(dep);
						}
						catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
						}
					}
				}
                //判断是不是单例bean
				// Create bean instance.
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, () -> {
						try {
							//创建bean
							return createBean(beanName, mbd, args);
						}
						catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					});
					//将新创建的单例对象赋值给bean
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}
                 //如果是原型作用域
				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						//beforePrototypeCreation(beanName) 和 afterPrototypeCreation(beanName) 是在Bean创建前后的扩展点，
						// 用于执行一些自定义的逻辑。
						beforePrototypeCreation(beanName);
						//创建原型bean
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					//将创建的原型bean赋值给bean
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}
                //处理自定义作用域
				else {
					//获得作用域
					String scopeName = mbd.getScope();
					//判断Scope是否为空
					if (!StringUtils.hasLength(scopeName)) {
						throw new IllegalStateException("No scope name defined for bean ´" + beanName + "'");
					}
					//获得作用域的名称
					Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						//这个get方法是scope.java中的一个方法，这里用lamda表达式实现了生成该bean的工厂
						Object scopedInstance = scope.get(beanName, () -> {
							//Bean创建前的扩展点
							beforePrototypeCreation(beanName);
							try {
								//创建Bean
								return createBean(beanName, mbd, args);
							}
							finally {
								//Bean创建后的扩展点
								afterPrototypeCreation(beanName);
							}
						});
						//将生成的Bean赋值给bean
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new ScopeNotActiveException(beanName, scopeName, ex);
					}
				}
			}
			catch (BeansException ex) {
				beanCreation.tag("exception", ex.getClass().toString());
				beanCreation.tag("message", String.valueOf(ex.getMessage()));
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
			finally {
				//标记创建bean完成
				beanCreation.end();
			}
		}
        //这段代码处理的是返回Bean的类型转换。当用户在获取Bean时指定了目标类型，Spring会确保返回的Bean是指定类型或者可以转换为指定类型的实例
		if (requiredType != null && !requiredType.isInstance(bean)) {
			//检查Bean是否是指定的类型以及用户是有指定了类型
			try {
				//如果不是指定的类型，则尝试进行类型转换。这是通过 getTypeConverter().convertIfNecessary(bean, requiredType) 方法完成的，
				// 其中 getTypeConverter() 返回Bean工厂使用的类型转换器，convertIfNecessary 尝试将Bean转换为目标类型。
				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					//如果类型转换成功，返回转换后的Bean。如果转换失败或者转换后的Bean为null，抛出 BeanNotOfRequiredTypeException 异常。
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		//返回bean
		return (T) bean;
	}
}
```



- SingletonBeanRegistry

> SingletonBeanRegistry是Spring框架中的一个接口，主要用于管理单例Bean的注册。它提供了一种机制，可以在运行时将预先实例化的对象（单例对象）注册到Spring容器中，这些对象可以在之后被应用程序中的其他部分获取和使用。在Spring的IoC（控制反转）容器中，这个接口主要被用于管理那些不由容器本身创建的对象，例如由用户手动创建的对象，或者是由其他框架或工厂方法创建的对象。

```java
public interface SingletonBeanRegistry {
    // 这个方法用于向注册表中注册一个新的单例Bean。参数beanName是Bean的名称，而singletonObject则是要注册的单例对象。
	void registerSingleton(String beanName, Object singletonObject);
	//根据单例bean的名称获取这个单例bean
	@Nullable
	Object getSingleton(String beanName);
     //判断是不是包含指定名称的单例bean
	boolean containsSingleton(String beanName);
	 //这个方法返回注册表中所有已经注册的单例Bean的名称。返回的是一个字符串数组
	String[] getSingletonNames();
     //这个方法返回注册表中单例bean的数量
	int getSingletonCount();
     //单例模式下带来的最严重的问题就是线程安全问题，
	 //getSingletonMutex() 方法在 SingletonBeanRegistry 接口中返回一个mutex（互斥锁）对象，该对象用于单例Bean的外部同步。
	 // 当你需要自定义的同步逻辑或者在进行某些需要线程安全保障的操作时，你可以使用这个返回的mutex对象来进行同步控制。
	Object getSingletonMutex();
}

```



- DefaultSingletonBeanRegistry

> Spring 框架中一个非常重要的类，主要用于提供单例 bean 的缓存和注册服务。这个类实现了 SingletonBeanRegistry 接口，并定义了一些管理单例 bean 的核心方法



```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
  //用来缓存单例bean的容器
  private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
  
  
  	//该方法用于添加新的单例bean
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			//向singletonObjects这个map中添加新的单例的键值对
			this.singletonObjects.put(beanName, singletonObject);
			//singletonFactories保存一个单例bean被创建但未初始化，加入到singletonObjects意味着这个单例bean被创建了，所以需要从移除这个信息
			this.singletonFactories.remove(beanName);
			//和上面同样的道理
			this.earlySingletonObjects.remove(beanName);
			//registeredSingletons存储已经被实例化的单例bean的名称，这里将新创建的单例bean的名称保存到set集合中
			this.registeredSingletons.add(beanName);
		}
  public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}
	//主要目的是获取指定名称的单例对象。如果需要，它还会创建这个单例对象的早期引用，这段代码的作用是获取给定bean名称的单例实例。
	// 它首先尝试从singletonObjects映射中获取完全初始化的单例实例。如果无法获取，则检查该bean是否正在创建中，如果是，则尝
	// 试从earlySingletonObjects映射中获取已实例化但未完全初始化的单例实例。如果仍然无法获取，则根据allowEarlyReference
	// 参数的值，决定是否创建早期引用（解决循环依赖问题）。如果允许创建早期引用，则尝试从singletonFactories映射中获取bean工
	// 厂，并使用该工厂创建早期引用。最后，将创建的单例实例存储在singletonObjects或earlySingletonObjects映射中，以便后续使用。
	@Nullable
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		//尝试从singletonObjects映射（已经完全初始化的单例对象映射）中获取bean实例
		Object singletonObject = this.singletonObjects.get(beanName);
		//如果在singletonObjects中找不到对应的实例，且该bean当前正在创建中（通过isSingletonCurrentlyInCreation(beanName)检查），则进入下一步
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			//尝试从earlySingletonObjects映射（已实例化但未完全初始化的对象映射）中获取bean实例
			singletonObject = this.earlySingletonObjects.get(beanName);
			//如果在earlySingletonObjects中也找不到，且参数allowEarlyReference为true，表示允许创建早期引用(主要是解决循环依赖问题)，则进入下一步
			if (singletonObject == null && allowEarlyReference) {
				synchronized (this.singletonObjects) {
					// 在singletonObjects上同步，确保线程安全，然后再次尝试从singletonObjects和earlySingletonObjects映射中获取bean实例
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						singletonObject = this.earlySingletonObjects.get(beanName);
						if (singletonObject == null) {
							//如果以上尝试都未能获取到bean实例，那么尝试从singletonFactories映射（存储bean工厂的映射）中获取对应的bean工厂
							ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
							if (singletonFactory != null) {
								//如果获取到了bean工厂，就用它来创建bean实例（早期引用），然后将这个早期引用存储在earlySingletonObjects映射中，并从singletonFactories映射中移除对应的bean工厂
								singletonObject = singletonFactory.getObject();
								this.earlySingletonObjects.put(beanName, singletonObject);
								this.singletonFactories.remove(beanName);
							}
						}
					}
				}
			}
		}
		return singletonObject;
	}
}
```



- AbstractAutowireCapableBeanFactory

>  `AbstractAutowireCapableBeanFactory`  是 Spring Framework 中的一个类，它提供了  `AutowireCapableBeanFactory`  接口的实现。它是  `DefaultListableBeanFactory`  的父类，实现了大部分的 Bean 创建和依赖注入的逻辑。 `AbstractAutowireCapableBeanFactory`  中的方法可以用于创建 Bean 实例、对 Bean 进行依赖注入、应用 BeanPostProcessor 等。此外，它还提供了一些扩展点，可以让子类对 Bean 创建和依赖注入的过程进行定制化。总的来说， `AbstractAutowireCapableBeanFactory`  是 Spring 中 Bean 工厂的核心实现之一。

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {
  @Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {
    //是否开启了Trace级别的日志
		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		//创建RooteBenaDefinition对象mbdToduse
		RootBeanDefinition mbdToUse = mbd;
		//用来解析类名（这是父类AbstractBeanFactory中实现的方法）它尝试解析给定的类名并返回Class对象
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
		  //如果mbd（Bean定义）还没有类对象，并且mbd的类名不为null，则创建一个新的RootBeanDefinition对象，并设置其类
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
		}
    //准备方法覆盖
		//这一步调用mbdToUse的prepareMethodOverrides方法，用于验证和准备覆盖的方法。如果验证失败，则抛出一个异常
		try {
		//prepareMethodOverrides是RootBeanDefinition 的一个方法，主要用于处理和验证Bean定义中的方法覆盖（method overrides）设置。
		//这个设置主要用于在 Spring IoC容器中覆盖或者替换Spring管理的 Bean 中的某个方法的行为，这样在后续创建 Bean 实例时，就可以根据这些设置来确定方法的行为。
			mbdToUse.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex);
		}
		try {
			//在实例化 Bean 之前，给 BeanPostProcessors 一个机会返回一个代理实例而不是目标 Bean 实例。如果这个步骤返回的Bean不为null，那么就直接返回这个 Bean。如果在这个步骤出现异常，则抛出一个异常
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
			//这里开始创建真正的bean实例
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// A previously detected exception with proper bean creation context already,
			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}
protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {
		//在Spring框架中，BeanWrapper接口是用于处理Bean属性的主要接口。BeanWrapper的作用是设置和获取属性值（单个或批量），获取属性描述符，以及查询设置属性值的能力。
		//BeanWrapper扩展了PropertyAccessor，这是所有Spring的属性访问器实现的基本接口，包括BeanWrapper。 BeanWrapper也提供了分析和管理的方法，以处理嵌套的路径和类型转换。
		//当创建一个新的Bean实例并对其进行填充（例如，从XML配置文件中读取的属性值）时，Spring使用BeanWrapper。同样，当Spring需要读取或修改现有Bean实例的属性时，也会使用BeanWrapper。
		BeanWrapper instanceWrapper = null;
		//判断RootbeanDefinition对象的类型
		if (mbd.isSingleton()) {
      //factoryBeanInstanceCache这个集合中，一个bean的名称对应一个BeanWrapper，如果是单例模式我们就删除这对映射关系
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		//表示不是单例模式
		if (instanceWrapper == null) {
			// 使用 createBeanInstance 方法实例化 Bean。这个过程可能会调用构造函数或工厂方法，或者在特殊情况下，例如对于 FactoryBean 或者通过CGLIB创建的 Bean，可能会使用特定的实例化策略(cglib是一种动态创建类的过程)
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		//由BeanWrapper获得Bean对象
		Object bean = instanceWrapper.getWrappedInstance();
		//获得该bean的类型（即对应的class对象）
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		//用于在应用程序上下文中创建bean时执行后处理程序。具体来说，它确保应用所有合并的bean定义的后处理程序，并且只在第一次创建bean时执行。如果在后处理程序期间发生异常，则会抛出BeanCreationException
		synchronized (mbd.postProcessingLock) {
			//这段代码的作用是确保在创建应用程序上下文中的bean时，应用所有合并的bean定义的后处理程序，并且只在第一次创建bean时执行。如果在后处理程序期间发生异常，则会抛出BeanCreationException
			if (!mbd.postProcessed) {
				try {
					//调用applyMergedBeanDefinitionPostProcessors方法，该方法用于应用所有已注册的MergedBeanDefinitionPostProcessor对象，
					// 以修改BeanDefinition对象的属性值
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex);
				}
				//将BeanDefinition对象的postProcessed属性设置为true，表示已经完成了所有的后处理操作。
				mbd.postProcessed = true;
			}
		}
    //检查BeanDefinition对象的isSingleton方法是否返回true，检查是否允许循环引用，以及检查当前单例对象是否正在创建中
		// 用于检查是否允许在创建Bean对象时提前曝光一个单例对象（这里设置提前曝光，且这个bean本身是运行循环依赖的，且该bena还正在创建中，注意如果该bean已经创建好的话就不是循环依赖的问题了，所以，这句语句的作用就是来解决循环依赖问题的）
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			if (logger.isTraceEnabled()) {
				logger.trace("Eagerly caching bean '" + beanName +
						"' to allow for resolving potential circular references");
			}
			//将当前对象添加到一个单例工厂
			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}
    //上面都几乎是在解决bean的循环依赖问题，下面才是真正的开始创建bean
		//初始化bean实例
		Object exposedObject = bean;
		try {
			//调用 populateBean 方法，该方法用于填充 Bean 的属性值。
			populateBean(beanName, mbd, instanceWrapper);
			//调用 initializeBean 方法，该方法用于初始化 Bean，并返回一个可公开的 Bean 对象
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
				throw (BeanCreationException) ex;
			}
			else {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
			}
		}
        //检查是够需要提前暴露单例bean，以避免循环引用问题
		if (earlySingletonExposure) {
			//根据bean的名称获得这个单例bean
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {//如果Bean实例是集合BeanWrapper获得的，则将其替换为提前暴露的单例 Bean 实例（类型检查）
					exposedObject = earlySingletonReference;
				}
				//这段代码的作用是在检查循环依赖时，如果某个 Bean 的依赖中存在“原始”版本的 Bean，则抛出异常。具体来说，它会检查当前 Bean 是否存在依赖关系，
				// 如果存在，则遍历依赖关系中的每个 Bean，如果该 Bean 不是仅用于类型检查，则将其添加到 actualDependentBeans 集合中。如果 actualDependentBeans
				// 不为空，则抛出 BeanCurrentlyInCreationException 异常，该异常表示当前 Bean 正在创建过程中，但其依赖的其他 Bean 已经使用了它的“原始”版本，
				// 而不是最终版本。这通常是类型匹配过于“热切”的结果，可以通过关闭 allowEagerInit 标志来解决。
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
					//遍历每一依赖的bean
					for (String dependentBean : dependentBeans) {
						//如果一个单例 Bean 只是为了类型检查而被创建，就从单例缓存中移除该 Bean。
						//在 Spring 容器中，当一个 Bean 的依赖被注入时，Spring 会检查这些依赖的类型是否匹配。如果依赖的类型不匹配，Spring 会抛出异常。
						// 为了避免这种情况，Spring 会在创建 Bean 实例之前，先创建一个“原型” Bean 实例，用来检查依赖的类型是否正确。如果类型匹配，再创
						// 建真正的 Bean 实例。这个“原型” Bean 实例就是为了类型检查而被创建的。
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							// actualDependentBeans.add(dependentBean) 的作用是将dependentBean添加到
							// `actualDependentBeans`  集合中。在这段代码中，它的作用是将当前 Bean 的依赖中不是仅用于
							// 类型检查的 Bean 添加到  `actualDependentBeans`  集合中，以便后续判断是否存在循环依赖。
							actualDependentBeans.add(dependentBean);
						}
					}
					//"类型匹配过于热切" 是指在 Spring 容器中，当容器在创建 Bean 的时候，会尝试去匹配该 Bean 所依赖的其他 Bean 的类型。
					// 如果匹配成功，就会将这些依赖注入到该 Bean 中。但是有时候，容器会过于热切地去匹配这些依赖，导致匹配出来的 Bean 并不
					// 是最终的 Bean 实例，而是用于类型检查的“原型” Bean 实例。这样就可能会导致循环依赖等问题。因此，建议在使用类型匹配时，
					// 要谨慎使用，避免出现这种情况。
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
								"] in its raw version as part of a circular reference, but has eventually been " +
								"wrapped. This means that said other beans do not use the final version of the " +
								"bean. This is often the result of over-eager type matching - consider using " +
								"'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
					}
				}
			}
		}

		// Register bean as disposable.
		try {
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
		}

		return exposedObject;
	}
}
```

- InstantiationStrategy

>  `InstantiationStrategy`  是 Spring Framework 中的一个接口，它定义了 Bean 实例化策略的规范。Bean 实例化是创建 Bean 实例的过程， `InstantiationStrategy`  定义了如何创建 Bean 实例的方法。它提供了两个方法： `instantiate()`  和  `instantiateWithMethodInjection()` 。 `instantiate()`  方法用于创建 Bean 实例， `instantiateWithMethodInjection()`  方法用于创建 Bean 实例并注入依赖。 `InstantiationStrategy`  接口的实现类可以根据具体的需求来实现 Bean 实例化的逻辑，例如使用反射、CGLIB 或者其他技术来创建 Bean 实例。总的来说， `InstantiationStrategy`  接口提供了一种灵活的方式来创建 Bean 实例，可以根据具体的需求进行定制化。

```java
public interface InstantiationStrategy {
	/**
	 * 这个方法主要是用来实例化具体的Bean
	 * @param bd 这是一个 Spring BeanDefinition 对象，它是 Spring 对 Bean 配置信息的内部表示。这个对象包含了 Bean 的类名、
	 *           Bean 的作用域、Bean 的属性值、Bean 是否懒加载、Bean 的初始化方法、Bean 的销毁方法等信息
	 * @param beanName   要实例化的bean的名称
	 * @param owner     这是一个 BeanFactory 对象，它是所有 BeanFactory 类型的根接口。在 Spring 中，BeanFactory 是一个创建、
	 *                  配置和管理 Bean 的工厂
	 * @return   我们实例化后的bean对象
	 * @throws BeansException
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner)
			throws BeansException;

	/**
	 * 与上一个 instantiate 方法的不同之处在于它允许你指定一个特定的构造函数和相应的参数来创建这个 Bean。
	 * @param bd
	 * @param beanName
	 * @param owner
	 * @param ctor 这是要用于实例化 Bean 的 Java 构造函数。这个参数允许你控制用哪个构造函数来创建 Bean
	 * @param args  这是传递给构造函数的参数。这个参数列表应该与你在 ctor 中指定的构造函数的参数列表匹配
	 * @return
	 * @throws BeansException
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			Constructor<?> ctor, Object... args) throws BeansException;

	/**
	 * 这是要调用工厂方法的 Bean 实例。如果工厂方法是静态的，那么这个参数可以为空
	 * @param bd
	 * @param beanName
	 * @param owner
	 * @param factoryBean 这是要调用工厂方法的 Bean 实例。如果工厂方法是静态的，那么这个参数可以为空
	 * @param factoryMethod 这是工厂方法，用于创建新的 Bean 实例。这个方法的返回类型应该与我们想要创建的 Bean 的类型相匹配
	 * @param args 这是传递给工厂方法的参数。这个参数列表应该与你在 factoryMethod 中指定的工厂方法的参数列表匹配
	 * @return
	 * @throws BeansException
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Object factoryBean, Method factoryMethod, Object... args)
			throws BeansException;

}
```

- SimpleInstantiationStrategy

> `SimpleInstantiationStrategy` 是 Spring Framework 中的一个类，它是 `InstantiationStrategy` 接口的一个实现。这个接口定义了实例化 Spring Bean 的策略。`SimpleInstantiationStrategy` 提供了一种简单直接的方式来实例化 Spring Beans。当实例化一个 Bean 时，它首先会尝试使用反射 API 来直接调用 Bean 的默认（无参）构造方法。如果这个 Bean 没有默认的构造方法，或者调用默认构造方法失败，那么它会抛出一个异常。`SimpleInstantiationStrategy` 也支持使用工厂方法和带参数的构造方法来实例化 Beans。它可以调用指定的静态或者非静态的工厂方法，或者使用反射 API 来调用带参数的构造方法。

```java
public class SimpleInstantiationStrategy implements InstantiationStrategy {
  @Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			final Constructor<?> ctor, Object... args) {
     //判断bean的方法有没有被重写
		if (!bd.hasMethodOverrides()) {
			if (System.getSecurityManager() != null) {
				// use own privileged to change accessibility (when security is on)
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
          //关闭java的安全检查，这样所有的构造函数就可以通过反射访问了，不管是proteted方法还是private方法
					ReflectionUtils.makeAccessible(ctor);
					return null;
				});
			}
      //直接调用BeanUtils.instantiateClass方法，通过反射机制实例化Bean
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
      //如果有方法覆写，那么会调用instantiateWithMethodInjection方法，通过方法注入的方式实例化Bean。然而，你可以看到instantiateWithMethodInjection方法内部直接抛出了UnsupportedOperationException，表示不支持方法注入。这一般是因为这是一个简单的实例化策略，如果需要支持方法注入，可能需要使用CGLIB等更复杂的实例化策略
			return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
		}
	}
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName,
			BeanFactory owner, @Nullable Constructor<?> ctor, Object... args) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}
}
```

> 在Spring框架中，有两种特殊的方法覆盖机制：lookup-method和replaced-method，它们都是在Bean定义时进行配置的。这两种机制都涉及到动态改变Spring Bean行为，因此处理起来要比普通的实例化复杂。
>
> 1. `lookup-method`: 这是一种方法注入的方式，允许在Spring容器的Bean中注入其他Bean的新实例。它通常用于注入原型Bean到单例Bean中，因为普通的注入方式只会在初始化时注入一次，无法获取到原型Bean的新实例。
> 2. `replaced-method`: 这是一种替换方法的方式，允许在运行时用新的方法替换Spring容器的Bean中的已有方法。这是一种AOP的应用，可以用于插入日志、事务等额外的行为。
>
> 上述两种方法都需要使用CGLIB库来生成Bean的子类，并在子类中覆盖这些方法，所以处理起来相对复杂。
>
> 而当Bean定义中没有这些方法覆盖时，Spring就可以采用简单的实例化策略，直接通过反射API调用构造函数或工厂方法来创建Bean实例，这种方式效率更高，也更简单。所以，`instantiate`方法中会先判断是否存在方法覆盖，如果不存在，就采用简单的方式来实例化Bean。

- CglibSubclassingInstantiationStrategy

> CglibSubclassingInstantiationStrategy是一个类，它是Spring Framework中的一个实例化策略。它的作用是使用CGLIB库生成子类来实例化对象，以便在运行时对目标对象进行增强和代理。它通常用于AOP和事务管理等方面。



```java
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {
  public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
    //返回值为bean
			Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
			Object instance;
      /// 这部分代码是用来创建增强子类的实例。如果没有指定构造函数（ctor为null），那么就使用默认的无参构造函数来创建实例。如果指定了构造函数，那么就通过反射API找到增强子类中相应的构造函数，并使用给定的参数来创建实例。
			if (ctor == null) {
				instance = BeanUtils.instantiateClass(subclass);
			}
			else {
				try {
					Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
					instance = enhancedSubclassConstructor.newInstance(args);
				}
				catch (Exception ex) {
					throw new BeanInstantiationException(this.beanDefinition.getBeanClass(),
							"Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
				}
			}
			//  这部分代码是用来设置增强子类的回调函数。这里使用了CGLIB的Factory接口，该接口定义了一个setCallbacks方法，可以用来设置回调函数。每个回调函数对应一个覆盖的方法，当调用该方法时，就会执行相应的回调函数。这里设置了两种回调函数：LookupOverrideMethodInterceptor和ReplaceOverrideMethodInterceptor，分别对应lookup-method和replaced-method。
			Factory factory = (Factory) instance;
			factory.setCallbacks(new Callback[] {NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner),
					new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner)});
    //返回创建的实例
			return instance;
		} 
  private Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
			//使用CGLIB框架生成目标类的子类（代理类）实现增强
			Enhancer enhancer = new Enhancer();
			//设置代理类的父类为要实例化的Bean
			enhancer.setSuperclass(beanDefinition.getBeanClass());
			//这段代码的作用是设置CGLIB Enhancer对象的命名策略。它使用SpringNamingPolicy.INSTANCE命名策略，该策略在生成类名时使用Spring的命名约定。
			// 这样做可以确保生成的类名与Spring应用程序的其他部分保持一致，从而提高代码的可读性和可维护性。
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			//这段代码的作用是检查当前对象的所有者(owner)是否是ConfigurableBeanFactory类的实例。如果是，那么会根据这个条件执行一些逻辑。
			// ConfigurableBeanFactory接口是Spring框架的一部分，提供了在Spring应用程序上下文中配置和管理bean的方法。
			if (this.owner instanceof ConfigurableBeanFactory) {
				//获得工厂的类加载器
				ClassLoader cl = ((ConfigurableBeanFactory) this.owner).getBeanClassLoader();
				//这段代码的作用是设置CGLIB Enhancer对象的生成策略。它使用ClassLoaderAwareGeneratorStrategy类作为生成策略，
				// 并将ClassLoaderAwareGeneratorStrategy的实例化对象作为参数传递给setStrategy()方法。
				// ClassLoaderAwareGeneratorStrategy是CGLIB库的一个类，它提供了一种生成类的方式，
				// 它可以使用指定的类加载器来加载生成的类。这样做可以确保生成的类可以被正确地加载和使用，从而提高应用程序的可靠性和稳定性。
				enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
			}
			//这段代码的作用是为CGLIB Enhancer对象设置回调过滤器。它使用MethodOverrideCallbackFilter类作为回调过滤器，
			// 并将一个bean定义对象作为参数传递给MethodOverrideCallbackFilter类的构造函数。MethodOverrideCallbackFilter
			// 是一个类，它可以过滤掉需要被代理的方法，以便在运行时对目标对象进行增强和代理。这样做可以提高应用程序的性能和可扩展性。
			enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
			enhancer.setCallbackTypes(CALLBACK_TYPES);
			//返回值为代理对象
			return enhancer.createClass();
		}
  
}
```

- DefaultListableBeanFactory

```java
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {
  //这是一个映射，键是bean的名称，值是对应的bean定义
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
  @Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
        //判断beanDefinition的类型是不是AbstractBeanDefinition
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				/**
				 * validate方法是AbstractBeanDefinition的一个方法，它用于验证bean定义的内容是否有效。具体来说，它会检查如下几个方面：
				 * 1. bean的类名是否已经设置，或者至少factory bean的名称和工厂方法已经设置
				 * 2. 如果bean是单例，那么它不能同时是抽象的和lazy-init的
				 * 3. bean的方法覆盖是否有效
				 * 4. 如果bean有父bean，那么父bean必须已经存在
				 * 5. 其他一些基本的合法性检查
				 */
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}
        //查看当前的bean是否已经存在beanDefinition了
		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		if (existingDefinition != null) {
			//isAllowBeanDefinitionOverriding方法是DefaultListableBeanFactory类的一个方法，它返回一个布尔值，用来判断是否允许覆盖同名的Bean定义。
			//如果isAllowBeanDefinitionOverriding返回true，那么可以用新的Bean定义覆盖旧的Bean定义。
			//如果返回false，则不允许覆盖。如果尝试覆盖，将抛出BeanDefinitionOverrideException异常。
			if (!isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			//现在已经存在的beanDefinition的role和新的beanDefinition的定义
			else if (existingDefinition.getRole() < beanDefinition.getRole()) {
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				if (logger.isInfoEnabled()) {
					logger.info("Overriding user-defined bean definition for bean '" + beanName +
							"' with a framework-generated bean definition: replacing [" +
							existingDefinition + "] with [" + beanDefinition + "]");
				}
			}
			else if (!beanDefinition.equals(existingDefinition)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Overriding bean definition for bean '" + beanName +
							"' with a different definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Overriding bean definition for bean '" + beanName +
							"' with an equivalent definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			}
			//将新的beanDefinition加入到beanDefinitionMap中，替换到原来的定义
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
		//如果当前的集合中不存在指定bean名称的BeanDefinition
		else {
			//用于检查当前 BeanFactory 是否已经开始创建bean
			if (hasBeanCreationStarted()) {
				synchronized (this.beanDefinitionMap) {
					//将新的BeanDefinition添加到map中
					this.beanDefinitionMap.put(beanName, beanDefinition);
					//BeadDefinition的数量+1
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					//将原来的beanDefinition的名称都添加到新的集合
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					//在 Spring 中，通常情况下，Bean 的创建和注册是由 Spring 容器自动完成的。但在某些情况下，你可能需要手动创建和注册 Bean。比如，
					// 在编程式地使用 Spring 容器时，或者在需要动态地创建和注册 Bean 时。
					//
					//当你手动注册了一个单例 Bean 时，Spring 容器会把这个 Bean 的名称添加到一个特定的列表中，这个列表用于存储所有手动注册的单例
					// Bean 的名称。removeManualSingletonName 方法就是用于从这个列表中移除指定的 Bean 名称
					removeManualSingletonName(beanName);
					//这样做的原因主要是考虑到线程安全性和不可变性。在多线程环境下，如果有多个线程同时读写 beanDefinitionNames 列表，那么可能会出现数据不一致的情况。
					// 为了避免这种情况，我们在修改列表之前，先创建一个新的列表，然后再进行修改。修改完成之后，再将新的列表赋值给 beanDefinitionNames。这样可以保证
					// 在任何时刻，其他线程看到的 beanDefinitionNames 列表都是一个完整且一致的列表，而不会出现中间状态。
				}
			}
			else {
				// Still in startup registration phase
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}
        //如果新的beanDefinition是一个单例
		if (existingDefinition != null || containsSingleton(beanName)) {
			//resetBeanDefinition 方法是 Spring DefaultListableBeanFactory 类中的一个方法，
			// 主要功能是清除 Bean 定义缓存，以及所有关联的 Bean 的相关信息
			resetBeanDefinition(beanName);
		}
		else if (isConfigurationFrozen()) {
			clearByTypeCache();
		}
	}
  @Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}
}
  
```

# 四、注入属性和依赖对象

## 1. Bean对象拆解思考

前面我们介绍了容器、定义和注册Bean对象、将Bean对象实例化、按照是否包含带入参的构造函数实现不同的实例化策略。但是，在创建对象的实例化细节拆解过程中，其实我们还没有思考关于“类中是否有属性”的问题，如果类中包含属性，那么在实例化过程中就需要填充属性信息，这样才能创建一个完整的对象。创建对象过程所需填充的属性不只是int、long、Double等基本类型，还包括可能没有实例化的对象属性，这些都需要在创建Bean对象时填充。

## 2. 属性填充设计

因为属性填充是在使用newInstance或者Cglib创建Bean对象后开始执行的，所以可以在AbstractAutowireCapableBeanFactory类的createBean方法中添加属性填充操作applyPropertyValues，如下图所示：

![](./img/4-1.jpg)

由于在创建bean对象时要执行属性填充操作，所以要在定义Bean对象的BeanDefinition类中添加对象创建时所需要的PropertyValues属性集合。填充的信息还包括Bean对象的类型，即需要再定义一个BeanReference对象（相当于借壳），里面只是一个简单的Bean对象名称，在具体实例化时进行递归创建和填充，与Spring源码中的实现一样

## 3. 属性填充实现

![](./img/4-2.png)

本节需要增加3个类：BeanReference（类引用）、PropertyValue（属性值）、PropertyValues（属性集合）。分别用于类和其他类型属性填充

**定义属性**

```java
public class PropertyValue {
    private final String name;
    private final Object value;
    public PropertyValue(String name,Object value){
        this.name=name;
        this.value=value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
```

```java
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
}
```

PropertyValue和PropertyValues这两个类的作用就是传递Bean对象创建过程中所要的属性信息，因为可能会有很多个属性，所以需要定义一个PropertyValues类进行属性集合的包装

**补全Bean对象定义**

```java
public class BeanDefinition {
    private Class beanClass;
    private PropertyValues propertyValues;

    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
        this.propertyValues=new PropertyValues();
    }
    public BeanDefinition(Class beanClass ,PropertyValues propertyValues){
        this.beanClass=beanClass;
       this.propertyValues=propertyValues !=null ? propertyValues: new PropertyValues();
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }
}
```

**Bean对象的属性填充**

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();
    private PropertyValues propertyValues;

    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition,Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
            //给bean对象填充属性
            applyPropertyValues(beanName,bean,beanDefinition);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition,String beanName,Object[] args) throws BeansException {
        Constructor constructorToUser=null;
        Class<?> beanClass=beanDefinition.getBeanClass();
        Constructor<?>[] declaredConstructors=beanClass.getDeclaredConstructors();
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            if(args!=null && declaredConstructor.getParameterTypes().length==args.length)
            {
                constructorToUser=declaredConstructor;
                break;
            }
        }
        return  getInstantiationStrategy().instatiate(beanDefinition,beanName,constructorToUser,args);
    }
    //该方法用于给Bean对象填充属性
    protected  void applyPropertyValues(String beanName, Object bean , BeanDefinition beanDefinition){
        try{
            //或的bean的属性集合
            PropertyValues propertyValues = beanDefinition.getPropertyValues();
            for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                //取出属性名和属性值
                String name=propertyValue.getName();
                Object value=propertyValue.getValue();
                if(value instanceof BeanReference){
                    //例如A依赖B，获取B的实例化对象
                    BeanReference beanReference=(BeanReference) value;
                    value=getBean(beanReference.getBeanName());
                }
                //属性填充
                BeanUtil.setFieldValue(bean,name,value);
            }
        } catch (Exception | BeansException e) {
            throw new BeanException("Error setting property values："+beanName);
        }
    }
    public InstantiationStrategy getInstantiationStrategy(){
        return instantiationStrategy;
    }
    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy){
        this.instantiationStrategy=instantiationStrategy;
    }
}
```

**测试**

> 事先准备

```java
public class UserDao {
    private static Map<String,String> hashmap=new HashMap<>();
    static {
        hashmap.put("10001","张三");
        hashmap.put("10002","李四");
        hashmap.put("10003","王五");
    }
    public String queryUserName(String uId){
        return hashmap.get(uId);
    }
}
```

```java
public class Userservice {
    private String uId;
    private UserDao userDao;
    public void queryUserInfo(){
        System.out.println("查询用户信息："+userDao.queryUserName(uId));
    }
}
```

Dao、Service是开发时经常使用的场景，如果在UserService中注册UserDao，就体现出了Bean对象的依赖关系

> 测试实例

```java
public class ApiTest {
    @Test
    public void test_BeanFactory() throws BeansException {
        //初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //注册UserDao
        beanFactory.registerBeanDefinition("userDao",new BeanDefinition(UserDao.class));
        //使用UserService填充属性
        PropertyValues propertyValues=new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("uId","10001"));
        propertyValues.addPropertyValue(new PropertyValue("userDao",new BeanReference("userDao")));
        //使用UserService注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(Userservice.class,propertyValues);
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //使用UserServices获取Bean对象
        Userservice userservice=(Userservice) beanFactory.getBean("userService");
        userservice.queryUserInfo();
    }
}
```

![](./img/4-3.png)

## 4. Spring相关源码解析

**BeanDefinition**

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
  //该方法用于获取 Bean 的属性值。
	//当 Spring 容器创建一个 Bean 时，它会根据你在 BeanDefinition 中定义的属性值来配置这个 Bean。这些属性值可以是其他的 Beans，也可以是基本类型的值。
	//MutablePropertyValues 是一个持有这些属性值的类。每个 PropertyValue 对象都持有一个属性名和一个属性值。getPropertyValues() 方法返回一个
	// MutablePropertyValues 对象，该对象包含了所有的属性值。
	//这个方法主要在 Spring 内部使用，当 Spring 容器创建一个 Bean 的时候，它会调用这个方法来获取属性值。在你的应用代码中，通常不需要直接使用这个方法
	MutablePropertyValues getPropertyValues();
	//判断这个类是否有任何属性
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}
}
```

**PropertyValue**

> PropertyValue类是Spring框架中的一个类，它用于表示bean的属性值。它通常用于将属性值注入到bean中。PropertyValue类包含属性名和属性值两个属性，它们可以通过构造函数或setter方法进行设置。在Spring中，PropertyValue对象通常被封装在PropertyValues对象中，以便在bean实例化时进行属性注入。通过使用PropertyValue对象，可以实现灵活的属性注入，从而提高应用程序的可扩展性和可维护性

```java
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {
  //属性名
  private final String name;
  //属性值
	@Nullable
	private final Object value;
  //构造函数
  public PropertyValue(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}
}
```

**PropertyValues**

> PropertyValues是Spring框架中的一个类，它用于表示bean的属性集合。它通常用于将多个属性值注入到bean中。PropertyValues对象包含多个PropertyValue对象，每个PropertyValue对象表示一个属性的名称和值。在Spring中，PropertyValues对象通常被封装在BeanDefinition对象中，以便在bean实例化时进行属性注入。通过使用PropertyValues对象，可以实现灵活的属性注入，从而提高应用程序的可扩展性和可维护性。

```java
public interface PropertyValues extends Iterable<PropertyValue> {
  //获得Bean数组的迭代器
	@Override
	default Iterator<PropertyValue> iterator() {
		return Arrays.asList(getPropertyValues()).iterator();
	}
  //这段代码是Java代码，它实现了Iterable接口中的spliterator()方法。这个方法返回一个Spliterator对象，它可以遍历PropertyValue对象的集合。具体来说，这个方法会调用getPropertyValues()方法获取PropertyValue对象的集合，然后使用Spliterators.spliterator()方法将这个集合转换为一个Spliterator对象。最后，这个Spliterator对象被返回给调用者。在Spring中，这段代码通常用于实现属性注入的功能。它可以遍历PropertyValue对象的集合，将属性值注入到bean中。通过实现Iterable接口和spliterator()方法，可以实现对属性集合的灵活遍历和处理。
	@Override
	default Spliterator<PropertyValue> spliterator() {
		return Spliterators.spliterator(getPropertyValues(), 0);
	}
	default Stream<PropertyValue> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	PropertyValue[] getPropertyValues();
	@Nullable
	PropertyValue getPropertyValue(String propertyName);
	PropertyValues changesSince(PropertyValues old);
	boolean contains(String propertyName);
	boolean isEmpty();
}
```

> spliterator和iterator的区别?
>
> Spliterator和Iterator都是Java中用于遍历集合的接口。它们的主要区别在于Spliterator是Java 8中新引入的接口，它支持并行遍历集合，而Iterator只支持单线程遍历集合。具体来说，Iterator接口只有hasNext()和next()两个方法，它们用于检查集合中是否还有下一个元素，并返回下一个元素。而Spliterator接口除了具有hasNext()和next()方法外，还有tryAdvance()和forEachRemaining()方法，它们用于支持并行遍历集合。tryAdvance()方法用于尝试获取下一个元素，如果成功获取，则返回true；否则返回false。forEachRemaining()方法用于对剩余的元素执行指定的操作。另外，Spliterator还有一些其他的方法，如estimateSize()和characteristics()等，用于获取集合的大小和特征等信息，以便更好地支持并行遍历。总的来说，Iterator主要用于单线程遍历集合，而Spliterator则更适用于并行遍历集合。

**BeanReference**

> BeanReference是Spring框架中的一个类，它用于表示对其他bean的引用。在Spring中，bean之间可以相互依赖，一个bean可以引用另一个bean，这个被引用的bean可以是同一个容器中的另一个bean，也可以是另一个容器中的bean。BeanReference对象就是用来表示这种引用关系的。BeanReference对象包含一个bean名称属性，它指定了被引用的bean的名称。在Spring容器启动时，当需要引用这个bean时，Spring会根据这个名称从容器中获取对应的bean实例，并将它注入到当前bean中。通过使用BeanReference对象，可以实现bean之间的依赖注入，从而提高应用程序的可扩展性和可维护性。

```java
public interface BeanReference extends BeanMetadataElement {

	/**
	 * Return the target bean name that this reference points to (never {@code null}).
	 */
	String getBeanName();

}
```

**AbstractAutowireCapableBeanFactory**

```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {
  //bean实例化的策略
  private InstantiationStrategy instantiationStrategy;
  //返回创建bean的实例化策略
  protected InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}
  @Override
	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
    //这段代码是Spring框架中的一个方法调用。 `markBeanAsCreated(beanName)` 方法用于标记一个bean已经在当前的bean创建过程中被创建。这个方法通常在bean创建过程中被Spring框架调用，在bean被实例化之后、属性被设置之前进行调用。通过调用 `markBeanAsCreated(beanName)` 方法，可以确保在bean的创建过程中不会出现循环依赖，从而保证应用程序的正常运行。
		markBeanAsCreated(beanName);
    //获得RootBeandefinition
		BeanDefinition bd = getMergedBeanDefinition(beanName);
    //获得BeanWrapper对象
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
    //初始化BeanWrapper对象
    //BeanWrapper对象是Spring框架中的一个类，它用于包装JavaBean对象，提供了对JavaBean属性的访问和修改方法。通过BeanWrapper对象，可以方便地访问和修改JavaBean对象的属性，而不需要直接访问JavaBean对象的属性。BeanWrapper对象可以用于获取和设置JavaBean对象的属性值，以及对JavaBean对象进行类型转换等操作。在Spring中，BeanWrapper对象通常被用于实现数据绑定和属性编辑器等功能，从而提高应用程序的灵活性和可维护性。
		initBeanWrapper(bw);
    //填充Bean的属性
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}
  protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
    //判断bean的属性列表是不是为空
		if (pvs.isEmpty()) {
			return;
		}
    //安全问题处理
		if (System.getSecurityManager() != null && bw instanceof BeanWrapperImpl) {
			((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
		}
    
		MutablePropertyValues mpvs = null;
		List<PropertyValue> original;
 //这段代码是Spring框架中的一个方法，它用于获取bean的属性值，并将其设置到BeanWrapper对象中。首先，它通过判断传入的属性值（pvs）是否为MutablePropertyValues对象来进行不同的处理。如果是MutablePropertyValues对象，则将其转换为MutablePropertyValues类型，并判断是否已经进行了类型转换。如果已经进行了类型转换，则直接将转换后的属性值设置到BeanWrapper对象中，如果设置失败，则抛出BeanCreationException异常。如果还没有进行类型转换，则获取原始的属性值列表，并将其设置到BeanWrapper对象中。如果传入的属性值不是MutablePropertyValues对象，则将其转换为PropertyValue数组，并将其设置到BeanWrapper对象中。通过这段代码，可以方便地获取bean的属性值，并将其设置到BeanWrapper对象中，从而实现对JavaBean对象属性的访问和修改。
		if (pvs instanceof MutablePropertyValues) {
			mpvs = (MutablePropertyValues) pvs;
			if (mpvs.isConverted()) {
				// Shortcut: use the pre-converted values as-is.
				try {
          //设置属性值
					bw.setPropertyValues(mpvs);
					return;
				}
				catch (BeansException ex) {
					throw new BeanCreationException(
							mbd.getResourceDescription(), beanName, "Error setting property values", ex);
				}
			}
			original = mpvs.getPropertyValueList();
		}
		else {
			original = Arrays.asList(pvs.getPropertyValues());
		}
   //这段代码是Spring框架中的一个方法，它用于获取类型转换器（TypeConverter）对象，并将其设置到BeanDefinitionValueResolver对象中。首先，它通过调用getCustomTypeConverter()方法来获取自定义的类型转换器（如果有的话），如果没有自定义的类型转换器，则使用BeanWrapper对象（bw）作为类型转换器。然后，它创建一个BeanDefinitionValueResolver对象，并将当前bean的名称（beanName）、BeanDefinition对象（mbd）、以及获取到的类型转换器（converter）作为参数传递给该对象。通过这段代码，可以方便地获取类型转换器，并将其设置到BeanDefinitionValueResolver对象中，从而实现对JavaBean对象属性的类型转换。d
		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

		// Create a deep copy, resolving any references for values.
		List<PropertyValue> deepCopy = new ArrayList<>(original.size());
		boolean resolveNecessary = false;
		for (PropertyValue pv : original) {
			if (pv.isConverted()) {
        //如果属性转换成功直接添加到deepCopy集合中
				deepCopy.add(pv);
			}
			else {
				String propertyName = pv.getName();
				Object originalValue = pv.getValue();
        //AutowiredPropertyMarker.INSTANCE 常量的作用是标记一个属性已经被自动注入过，避免重复注入或死循环等问题的发生。
				if (originalValue == AutowiredPropertyMarker.INSTANCE) {
          //这段代码是Spring框架中的一个方法调用，它用于获取JavaBean对象中指定属性的写方法。首先，它通过BeanWrapper对象的 `getPropertyDescriptor(propertyName)` 方法获取指定属性的属性描述符(PropertyDescriptor)对象，然后通过属性描述符对象的 `getWriteMethod()` 方法获取该属性对应的写方法(Write Method)。通过这个写方法，可以方便地对JavaBean对象的属性进行赋值操作，从而实现对JavaBean对象的属性修改。
					Method writeMethod = bw.getPropertyDescriptor(propertyName).getWriteMethod();
					if (writeMethod == null) {
						throw new IllegalArgumentException("Autowire marker for property without write method: " + pv);
					}
          //这段代码的作用是创建一个DependencyDescriptor对象，用于描述一个依赖项的信息。它通过传入一个MethodParameter对象和一个boolean类型的标志位来创建DependencyDescriptor对象。其中，MethodParameter对象表示一个方法参数，它包含了方法参数的类型、名称、索引等信息。在这里，MethodParameter对象的构造方法中传入了一个写方法(writeMethod)和一个参数索引(0)，表示该依赖项对应的方法参数是写方法的第一个参数。而boolean类型的标志位则表示是否允许依赖项的值为null。通过创建DependencyDescriptor对象，可以方便地获取依赖项的信息，从而实现对依赖项的注入和管理。
					originalValue = new DependencyDescriptor(new MethodParameter(writeMethod, 0), true);
				}
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;
				boolean convertible = bw.isWritableProperty(propertyName) &&
						!PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
				if (convertible) {
          //转换属性
					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
				}
				// Possibly store converted value in merged bean definition,
				// in order to avoid re-conversion for every created bean instance.
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				}
				else if (convertible && originalValue instanceof TypedStringValue &&
						!((TypedStringValue) originalValue).isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				}
				else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		if (mpvs != null && !resolveNecessary) {
			mpvs.setConverted();
		}

		// Set our (possibly massaged) deep copy.
		try {
			bw.setPropertyValues(new MutablePropertyValues(deepCopy));
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Error setting property values", ex);
		}
	}
}
```

# 五、资源加载器解析文件注册对象

## 1. 本节重点

定义用于解析XML文件的XmlBeanDefinitionReader类，处理用于配置在XML文件中的Bean对象信息，完成自动配置和注册Bean对象

## 2. 对象创建问题

在完成Spring框架的雏形后，我们可以通过单元测试，手动完成Bean对象的定义、注册和属性填充，以及获取对象调用的方法。但在实际的使用过程中，是不太可能让用户通过手动创建对象的，而是通过Spring配置文件来简化创建过程，具体需要完成下面动作 。首先我们需要将步骤2、步骤3、步骤4整合到Spring框架中，通过Spring配置文件将Bean对象实例化。然后在现在的Spring框架中添加能实现Spring配置文件的读取、解析和Bean对象注册。

![](./img/5-1.jpg)

## 3. 资源加载和解析设计

我们需要在现有的Spring框架中添加一个资源加载器，用于读取ClassPath、本地文件和远程云文件（HTTP）的配置内容。这些配置内容包括Bean对象的描述信息和属性信息，当读取配置文件中的内容后，就可以先见配置文件中的Bean描述信息解析再注册，将bean对象注册到Spring Bean容器中。整体设计架构如下图所示：

![](./img/5-2.jpg)

- 资源加载器属于相对独立的部分，位于Spring框架核心包下，主要用于读取ClassPath、本地文件（File）和远程云文件（HTTP文件）
- 资源加载后，接下来就是将Bean对象解析并注册到Spring框架中，这部分需要和DefaultListableBeanFactory核心类结合起来实现，因为所有解析后的注册动作，都会将Bean对象的定义信息放入到DefaultListableBeanFactopry类中。
- 在实现时，需要设计好接口的层级关系，包括对Bean对象的读取接口BeanDefinitionReader的定义，以及设计好对应的实现类，在实现类中完成对Bean对象的解析和注册

## 4. 资源加载和解析设计实现

![](./img/5-3.png)

为了将Bean对象的定义、注册和初始化交给Spring.xml配置文件进行处理，需要实现两部分—资源加载器、XML资源处理类。其实现过程主要是对Resource接口、ResourceLoader接口的实现，此外BeanDefinitionReader接口是对资源的具体使用，其功能是将配置信息注册到SpringBean容器中。在Resource资源加载器的实现中，包括ClassPath、本地文件（File）、远程云文件（HTTP文件），这3部分于Spring源码中的设计和实现保持基本一致，最终都会在DefaultResourceLoader中调用。BeanDefinitionReader接口、AbstractBeanDefinitionReader抽象类、XmlBeanDefinitionReader实现类合理、清晰地处理了资源读取后Bean对象的操作。BeanFactory是已经存在的Bean工厂接口 ，用于获取Bean对象，并增加了按照类型获取Bean对象的方法`<T> T getBean(String name, Class<T> requiredType)`。ListableBeanFactory是一个接口，其功能是扩展Bean工厂接口，该接口中新增了`getBeansOfType()`方法、`getBeanDefinitionNames()`方法。在Spring源码中，HierarchicalBeanFacotry是一个扩展Bean工厂层次的子接口，提供了可以获取父类BeanFacotry的方法。AutowireCapableBeanFactory是一个自动化处理Bean工厂配置的接口，目前在实际中还没有完成相应的实现，后续还会逐步完善。ConfigurableBeanFactory是一个可以获取BeanPostProcessor、BeanClassLoader等方法的配置化接口。ConfigurableListableBeanFactory是一个提供分析和修改Bean对象与预先的接口，不过目前我们只实现了getBeanFactory方法。

![](./img/5-4.png) 

**资源加载接口的实现和定义**

![](./img/5-5.png)

```java
public interface Resource {
    InputStream getInputStream() throws IOException;
}
```

在Spring框架下的core.io核心包，主要用于处理资源加载流，首先先定义了Resource接口，提供获取InputStream流的方法，然后分别实现3种不同的流文件—ClassPath、FileSystem和URL

```java
public class ClassPathResource implements Resource{
    //把资源的路径信息定义为常量
    private final String path;
    //定义类加载器
    private ClassLoader classLoader;

    public ClassPathResource(String path) {
        this(path,(ClassLoader)null);
    }

    public ClassPathResource(String path, ClassLoader classLoader) {
        //判断路径参数是否为空
        Assert.notNull(path,"Path must not be null");
        this.path=path;
        this.classLoader=(classLoader !=null ? classLoader: ClassUtils.getDefaultClassLoader());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        //使用类加载器加载资源（转换为流）
        InputStream is=classLoader.getResourceAsStream(path);
        if(is ==null){
            throw new FileNotFoundException(
                    this.path+" connot be opened because it does not exist"
            );
        }
        return is;
    }
}
```

这部分是通过ClassLoader读取ClassPath中的文件信息实现的，具体的读取命令是classLoader.getResourceAsStream(path)

```java
public class FileSystemResource implements Resource {
    //定义一个文件对象
    private final File file;
    //定义文件路径
    private final String path;

    public FileSystemResource(File file){
        this.file=file;
        this.path=file.getPath();
    }
    public FileSystemResource(String path){
        this.file=new File(path);
        this.path=path;
    }
    public final String getPath(){
        return this.path;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }
}
```

通过指定文件的路径的方式读取文件信息时会读取到一些TXT文件和Excel文件，将这些文件输出到控制台

```java
public class UrlResource implements Resource{
    private final URL url;
    
    public UrlResource(URL url){
        Assert.notNull(url,"URL must bot be null");
        this.url=url;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        //与指定的URL资源建立TCP连接
        URLConnection con=this.url.openConnection();
        try{
            //打开流，获取文件内容
            return con.getInputStream();
        }catch (IOException ex){
            if(con instanceof HttpURLConnection){
                //断开连接
                ((HttpURLConnection)con).disconnect();
            }
            throw ex;
        }
    }
}
```

可以通过HTTP 读取远程文件（HTTP文件），也可以将配置文件放到github或gitee平台中获取

**包装资源加载器**

资源加载的方式有多种，资源加载器可以将这些方式放到统一的类服务下进行处理，外部用户只需要传递资源地址

```java
public interface ResourceLoader {
    //文件资源位置标识的前缀
    String CLASSPATH_URL_PREFIX="classpath:";
    //获取资源
    Resource getResource(String location);
}
```

定义获取资源的接口，在接口中传递资源地址

```java
public class DefaultResourceLoader implements ResourceLoader{
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location,"Location must bot be null");
        //如果地址是以我们指定的前缀开头的
        if(location.startsWith(CLASSPATH_URL_PREFIX)){
            //使用ClassPathResource对象来解析Classpath类型的资源
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
        }else{
            try {
                //如果locatino不是ClassPath，则为网络资源
                URL url=new URL(location);
                return new UrlResource(url);
            } catch (MalformedURLException e) {
                //如果两者都不是就是系统文件资源
                return new FileSystemResource(location);
            }
        }
    }
}
```

在获取资源的过程中，主要对3种不同类型的资源处理方式，分别判断为ClassPath、FileSystem或URL文件。DefaultResourceLoader类的实现很简单，不会让外部调用方知道过多的细节，仅仅关心调用结果即可

**Bean对象定义读取接口**

```java
public interface BeanDefinitionReader {
    //获得BeanDefinitionRegistry用来注册BeanDefinition
    BeanDefinitionRegistry getRegistry();
    //获取ResourceLoader用来加载资源
    ResourceLoader getResourceLoader();
    //这三个方法都是用来加载Bean定义的。其中，第一个方法通过Resource对象来加载Bean定义，
    // 第二个方法通过Resource数组来加载Bean定义，第三个方法通过字符串类型的资源位置来加载Bean定义。
    // 如果加载Bean定义时出现问题，会抛出BeansException异常。
    void loadBeanDefinitions(Resource resource)throws BeansException;
    void loadBeanDefinitions(Resource... resources)throws BeansException;
    void loadBeanDefinitions(String location)throws BeansException;
}
```

这是一个用于读取Bean对象定义的接口，其中定义了几个方法，如getRegistry方法、getResourceLoader方法以及3个加载Bean对象定义的方法。这里需要注意的是getRegistry方法，getResourceLoader方法，它们都为加载Bean对象定义的方法提供了工具，这两个方法的实现会被包装到抽象类中，以免与具体的接口实现方法产生冲突。

**Bean定义抽象类实现**

```java
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {
    private final BeanDefinitionRegistry beanDefinitionRegistry;
    private ResourceLoader resourceLoader;
    
    protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry){
        this(registry,new DefaultResourceLoader());
    }
    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry,ResourceLoader resourceLoader){
        this.beanDefinitionRegistry=registry;
        this.resourceLoader=resourceLoader;
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return beanDefinitionRegistry;
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
```

抽象类实现了BeanDefinitionReader接口的前两个方法，并提供了构造函数。此时外部调用将Bean对象的定义注入并传递到类中。这样在BeanDefinitionReader接口的具体实现类中，就可以将解析后的XML文件中的Bean对象信息注册到Spring Bean容器中。在之前的单元测试中，我们通过调用BeanDefinitionRegistry完成了Bean对象的注册，现在可以放到XML文件中了

**解析XML处理Bean注册**

```java
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
                Class<?> clazz = Class.forName(className);

                String beanName = id.isEmpty() ? name : id;
                if (beanName.isEmpty()) {
                    beanName = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
                }

                BeanDefinition beanDefinition = new BeanDefinition(clazz);
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
```

XmlBeanDefinitionReader类最核心的内容就是对XML文件的解析，通过解析XML文件，自动注册的方式来实现。loadBeanDefinitions方法用于处理资源加载。这里新增来一个内部方法，doLoadBeanDefinitions，其主要功能就是解析Xml文件。doLoadBeanDefinitions方法主要是对XML文件进行读取和对Element元素进行解析。在解析的过程中，我们循环获取Bean对象的配置，以及配置中的id、name、class和ref信息。先将读取出来的配置信息创建成BeanDefinition及PropertyValues，再将完整的Bean定义内容注册到Spring Bean容器中。

**测试**

> 实现准备

```java
public class UserDao {
    private static Map<String,String> hashmap=new HashMap<>();
    static {
        hashmap.put("10001","张三");
        hashmap.put("10002","李四");
        hashmap.put("10003","王五");
    }
    public String queryUserName(String uId){
        return hashmap.get(uId);
    }
}

```

```java
public class Userservice {
    private String uId;
    private UserDao userDao;
    public void queryUserInfo(){
        System.out.println("查询用户信息："+userDao.queryUserName(uId));
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
```

Dao和service是开发时经常会用到的类，在UserService中注入UserDao，就能体现出Bean属性的依赖

> 配置文件

```properties
#filename：important.properties
# config File
system.key=OLpj9823dZ
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <bean id="userDao" class="springframework.bean.UserDao"/>
    <bean id="userservice" class="springframework.bean.Userservice">
        <property name="uId" value="10001"></property>
        <property name="userDao" ref="userDao"></property>
    </bean>
</beans>
```

> 单元测试（资源加载）

```java
public class ApiTest {
    private DefaultResourceLoader resourceLoader;
    @Before
    public void init(){
        resourceLoader=new DefaultResourceLoader();
    }
    @Test
    public void test_classpath()throws IOException{
        Resource resource=resourceLoader.getResource("classpath:important.properties");
        InputStream inputStream=resource.getInputStream();
        String content= IoUtil.readUtf8(inputStream);
        System.out.println(content);
    }
    @Test
    public void test_url()throws IOException{
        Resource resource=resourceLoader.getResource("github地址");
        InputStream inputStream=resource.getInputStream();
        String content=IoUtil.readUtf8(inputStream);
        System.out.println(content);
    }
    @Test
    public void test_file() throws IOException{
        Resource resource=resourceLoader.getResource("src/test/resources/important.properties");
        InputStream inputStream=resource.getInputStream();
        String content=IoUtil.readUtf8(inputStream);
        System.out.println(content);
    }
}
```

![](./img/5-6.png)

> 单元测试（读取配置文件并加载bean）

```java
public class ApiTest {
    @Test
    public void test_xml() throws BeansException {
        //初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();

        //读取配置文件注册bean对象
        XmlBeanDefinitionReader reader=new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("classpath:spring.xml");

        //获取bean对象的调用方法
        Userservice userservice= (Userservice) beanFactory.getBean("userservice",Userservice.class);
        userservice.queryUserInfo();
    }
}
```

![](./img/5-7.png)

## 5. Spring相关源码解析

**Resource**

```java
public interface Resource extends InputStreamSource {
  //确定此资源是否以物理形式实际存在
	boolean exists();
  //这段代码的作用是返回一个布尔值，用于判断当前对象是否可读。如果该对象存在，则返回true，否则返回false
	default boolean isReadable() {
		return exists();
	}
  //返回一个布尔值判断文件是否可以打开
	default boolean isOpen() {
		return false;
	}
  //判断指定的资源是否是一个文件资源
	default boolean isFile() {
		return false;
	}
  //获得统一资源定位符
	URL getURL() throws IOException;
  //统一资源标识符
	URI getURI() throws IOException;
  //获得文件
	File getFile() throws IOException;
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}
  //获得资源文件的长度
	long contentLength() throws IOException;
  //获得资源文件的上一次修改时间
	long lastModified() throws IOException;
  //该方法的目的是创建一个新的 Resource 对象，该对象表示相对于当前资源的资源。 relativePath 参数指定了新资源相对于当前资源的路径
	Resource createRelative(String relativePath) throws IOException;
	@Nullable
  //获得文件名
	String getFilename();
  //获得文件的描述信息
	String getDescription();

}
```

**ClassPathResource**

```java
public class ClassPathResource extends AbstractFileResolvingResource {
  private final String path;
	@Nullable
	private ClassLoader classLoader;
  
  public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}
  
  public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		Assert.notNull(path, "Path must not be null");
		String pathToUse = StringUtils.cleanPath(path);
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		this.path = pathToUse;
    //这段代码的作用是初始化一个类加载器（ClassLoader）对象。如果传入的classLoader不为空，则使用传入的类加载器对象；否则，使用 ClassUtils.getDefaultClassLoader()方法获取默认的类加载器对象。该类加载器对象可以用于加载指定类的字节码文件，以便在程序中使用该类。
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}
  public final String getPath() {
		return this.path;
	}
  @Nullable
	public final ClassLoader getClassLoader() {
		return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
	}
  @Override
	public boolean exists() {
		return (resolveURL() != null);
	}
	@Nullable
	protected URL resolveURL() {
		if (this.clazz != null) {
			return this.clazz.getResource(this.path);
		}
		else if (this.classLoader != null) {
			return this.classLoader.getResource(this.path);
		}
		else {
			return ClassLoader.getSystemResource(this.path);
		}
	}
  @Override
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else if (this.classLoader != null) {
			is = this.classLoader.getResourceAsStream(this.path);
		}
		else {
			is = ClassLoader.getSystemResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}
}
```

**FileSysremResource**

```java
public class FileSystemResource extends AbstractResource implements WritableResource {
  private final String path;
	@Nullable
	private final File file;
	private final Path filePath;
  public FileSystemResource(String path) {
		Assert.notNull(path, "Path must not be null");
		this.path = StringUtils.cleanPath(path);
		this.file = new File(path);
		this.filePath = this.file.toPath();
	}
  @Override
	public InputStream getInputStream() throws IOException {
		try {
			return Files.newInputStream(this.filePath);
		}
		catch (NoSuchFileException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}
}
```

**UrlResource**

```java
public class UrlResource extends AbstractFileResolvingResource {
  @Nullable
	private final URI uri;
  private final URL url;
  	public UrlResource(URI uri) throws MalformedURLException {
		Assert.notNull(uri, "URI must not be null");
		this.uri = uri;
		this.url = uri.toURL();
	}
  @Override
	public InputStream getInputStream() throws IOException {
		URLConnection con = this.url.openConnection();
		ResourceUtils.useCachesIfNecessary(con);
		try {
			return con.getInputStream();
		}
		catch (IOException ex) {
			// Close the HTTP connection (if applicable).
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).disconnect();
			}
			throw ex;
		}
	}
}
```

**ResourceLoader**

```java
public interface ResourceLoader {
  String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;
  Resource getResource(String location);
  //获得类加载器
  @Nullable
	ClassLoader getClassLoader();
}
```

**DefaultResourceLoader**

```java
public class DefaultResourceLoader implements ResourceLoader {
  //如果没有找到 Resource 对象，则继续判断 location 参数的前缀，如果以 / 开头，则调用 getResourceByPath 方法来获取 Resource 对象。如果以 classpath: 开头，则使用 ClassPathResource 来获取 Resource 对象。如果以上两种情况都不满足，则尝试将 location 参数解析为URL，并使用 FileUrlResource 或 UrlResource 来获取 Resource 对象。 
  @Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");
    // `ProtocolResolver` 是一个接口，用于解析指定协议的资源。它定义了一个方法 `resolve` ，该方法接受一个资源位置和一个 `ResourceLoader` 对象，并返回一个 `Resource` 对象，表示指定位置的资源。 `ProtocolResolver` 的实现类可以解析各种协议的资源，例如 `http` 、 `ftp` 、 `file` 等。在 `DefaultResourceLoader` 类中，会遍历所有注册的 `ProtocolResolver` 实现类，通过调用 `resolve` 方法来解析指定位置的资源。
		for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
			Resource resource = protocolResolver.resolve(location, this);
			if (resource != null) {
				return resource;
			}
		}

		if (location.startsWith("/")) {
			return getResourceByPath(location);
		}
		else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
		}
		else {
			try {
				// Try to parse the location as a URL...
				URL url = new URL(location);
				return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
			}
			catch (MalformedURLException ex) {
				// No URL -> resolve as resource path.
				return getResourceByPath(location);
			}
		}
	}
  
}
```

**BeanDefinitionReader**

```java
public interface BeanDefinitionReader {
  BeanDefinitionRegistry getRegistry();
  @Nullable
	ResourceLoader getResourceLoader();
  //该方法用于生成bean的名称（为匿名bean）
  BeanNameGenerator getBeanNameGenerator();
  //从特定的资源加载bean的定义（返回值是加载到的beanDefinition的数量）
  int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;
  //从多个特定的资源加载bean（返回值是加载到的beanDefinition的数量）
  int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;
  //从指定资源的位置加载beanDefinition（返回值是加载到的beanDefinition的数量）
  int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;
  //从多个指定资源的位置加载beanDefinition（返回值是加载到的beanDefinition的数量）
  int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;
  
}
```

**AbstractBeanDefinitionReader**

> AbstractBeanDefinitionReader是一个抽象类，它是用来读取和解析Bean定义的。它提供了一些通用的方法和属性，用于从不同的资源（比如XML文件）中读取Bean定义，并将它们解析成BeanDefinition对象。它的具体实现类可以根据需要来选择，比如XmlBeanDefinitionReader用于读取XML文件中的Bean定义，而PropertiesBeanDefinitionReader用于读取Properties文件中的Bean定义等。

```java
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader, EnvironmentCapable {
  private final BeanDefinitionRegistry registry;
  @Nullable
	private ResourceLoader resourceLoader;
  //  beanClassLoader ：表示当前类的类加载器对象。该变量的类型为 ClassLoader ，并且可以为 null ，即如果未指定，则默认为 null 。该变量的作用是加载该类所依赖的其他类的字节码文件。
  @Nullable
	private ClassLoader beanClassLoader;
//environment ：表示当前类的运行环境。该变量的类型为 Environment ，并且可以为 null ，即如果未指定，则默认为 null 。该变量的作用是提供当前类的配置信息，例如配置文件中的属性值。 
	private Environment environment;
//beanNameGenerator ：表示当前类的Bean名称生成器。该变量的类型为 BeanNameGenerator ，并且默认为 DefaultBeanNameGenerator.INSTANCE ，即使用默认的Bean名称生成器。该变量的作用是自动生成Bean的名称，以便在应用程序中使用。
	private BeanNameGenerator beanNameGenerator = DefaultBeanNameGenerator.INSTANCE;
  
  //构造函数
  protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;
		// Determine ResourceLoader to use.
    //代码判断 registry 对象是否实现了 ResourceLoader 接口。如果是，则将 registry 对象强制转换为 ResourceLoader 类型，并将其赋值给该类的成员变量 resourceLoader
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		}
		else {
      //创建一个 PathMatchingResourcePatternResolver 对象，并将其赋值给 resourceLoader 变量。 PathMatchingResourcePatternResolver 是一个用于解析资源路径的类。 
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}
     //代码判断 registry 对象是否实现了 EnvironmentCapable 接口。如果是，则调用 getEnvironment 方法获取其环境对象，并将其赋值给该类的成员变量 environment 。如果不是，则创建一个 StandardEnvironment 对象，并将其赋值给 environment 变量。 StandardEnvironment 是一个用于表示标准环境的类
		if (this.registry instanceof EnvironmentCapable) {
			this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
		}
		else {
			this.environment = new StandardEnvironment();
		}
	}
  @Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}
  @Override
	@Nullable
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}
}
```

**XmlBeanDefinitionReader**

```java
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
  //这行代码定义了一个成员变量 `documentReaderClass` ，它是一个 `Class` 对象，表示用于读取Bean定义文档的类。该变量的类型为 `Class<? extends BeanDefinitionDocumentReader>` ，即它是一个泛型类，泛型参数是 `BeanDefinitionDocumentReader` 的子类。默认情况下， `documentReaderClass` 的值是 `DefaultBeanDefinitionDocumentReader.class` ,即使用默认的Bean定义文档读取器类。可以通过更改该变量的值来指定自定义的Bean定义文档读取器类。
 private Class<? extends BeanDefinitionDocumentReader> documentReaderClass =
			DefaultBeanDefinitionDocumentReader.class; 
  public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}
  //这段代码是一个方法 `setValidating` ，该方法接受一个布尔类型的参数 `validating` ，用于设置XML文档是否进行验证。如果 `validating` 为 `true` ，则将该类的成员变量 `validationMode` 设置为 `VALIDATION_AUTO` ，否则设置为 `VALIDATION_NONE` 。 `VALIDATION_AUTO` 和 `VALIDATION_NONE` 是该类的常量，分别表示自动验证和不验证。同时，将该类的成员变量 `namespaceAware` 设置为 `!validating` ，即如果进行验证，则启用命名空间感知，否则禁用命名空间感知。
  public void setValidating(boolean validating) {
		this.validationMode = (validating ? VALIDATION_AUTO : VALIDATION_NONE);
		this.namespaceAware = !validating;
	}
  	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {

		try {
			Document doc = doLoadDocument(inputSource, resource);
			int count = registerBeanDefinitions(doc, resource);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + count + " bean definitions from " + resource);
			}
			return count;
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"XML document from " + resource + " is invalid", ex);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"IOException parsing XML document from " + resource, ex);
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Unexpected exception parsing XML document from " + resource, ex);
		}
}
```

# 六、实现应用上下文

## 1. 本节重点

随着对Spring框架的深入学习，我们开始接触核心的流程设计和处理部分，其难度与我们把控一个具有复杂需求的研发设计和实现过程是类似的。解决这类复杂场景的设计主要分为—分治、抽象和知识3个方面，运用架构和设计模式的知识，在分治层面将一个大问题分为若干个子问题，而问题越小就越容易被理解和处理。本节将在Spring框架中继续扩展新的功能。例如对一个Bean对象进行定义和实例化的过程中，是否可以满足自定义扩展需求，此时需要引入什么样子的界限上下文，对bean对象执行修改、记录和替换等动作呢？（引入应用上下文，进行资源扫描与加载，为bean对象实例化过程添加扩展机制，运行加载Bean对象和在其实例化前后进行修改和扩展。

## 2. 分治Bean对象功能

如果在工作中开发过基于Spring的技术组件，或者学习过关于SpringBoot中间件的设计和开发等内容，那么一定做过以下内容：继承或者实现了Spring对外暴露的类或接口，在接口的实现中获取BeanFactory及Bean对象等内容，如修改Bean的信息、添加日志打印、处理数据库路由对数据源进行切换、给RFC服务连接注册中心等。在对Bean对象进行实例化的过程中，不仅需要添加扩展机制，还需要优化Spring.xml配置文件的初始化和加载策略（因为不能将面向Spring本身开发的DefaultListableBeanFactory服务直接给用户使用）

![](./img/6-1.jpg)

在目前的Spring框架中，DefaultListableBeanFactory、XmlBeanDefinitionReader是我们对于服务功能进行测试时使用的方法。它们能很好的体现出Spring框架是如何加载XML文件以及注册Bean对象的，但这种方法是面向Spring框架本身的，不具备一定的扩展性，就像需要在Bean初始化过程中完成对Bean的扩展，但是很难做到自动化处理，所以我们需要要将Bean对象的扩展机制功能和对Spring扩展上下文的包装进行整合，一提供完整的服务。

> Spring框架上下文是指Spring框架中的一个容器，它负责管理应用程序中的所有对象。这个容器包含了应用程序中所有的Bean对象，以及它们之间的依赖关系。这样，当应用程序需要使用某个Bean对象时，Spring框架就可以从上下文中获取它，并将它注入到需要它的地方。因此，Spring框架上下文是Spring框架中非常重要的一个概念。

## 3. Bean对象扩展和上下文设计

为了在Bean对象从注入到实例化的过程中执行用户的自定义操作，需要在Bean的定义和初始化过程中插入接口类，这个接口类再由外部实现自己需要的服务事项。再结合Spring框架上下文的应用，就可以满足我们的目标需求。整体架构设计如下图所示：

![](./img/6-2.jpg)

满足Bean对象扩展的BeanFactoryPostProcessor和BeanPostProcessor是Spring框架非常重量级的两个接口，也是使用Spring框架新增开发组建需求的两个必备接口。BeanFactoryPostProcessor接口是由Spring框架组件提供的容器扩展机制，允许在定Bean对象注册后、未实例化之前，修改Bean对象的定义信息BeanDefinition。BeanPostProcessor接口也是Spring提供的扩展机制，不同的是BeanPostProcessor接口是在Bean对象执行初始化方法前后，对Bean对象进行修改、记录和替换。这部分扩展功能与后面AOP的实现有着密切的关系。如果只添加BeanFactoryPostProcessor和BeanPostProcessor这两个接口，不做任何包装，那么使用是非常困难的。我们的目的是开发Spring的上下文类，将相应的XML文件的加载、注册和实例化以及新增的修改和扩展功能全部融合起来，使Spring可以自动扫描到新增的服务，便于用户使用。

## 4. Bean对象扩展和上下文实现

