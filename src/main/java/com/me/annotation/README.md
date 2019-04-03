### java注解说明

#### 1.什么是注解
什么是注解，注解就是一种描述源码的元数据。我们可以通过注解给类、方法或字段提供额外的信息以便了解更多信息。
举个例子，java中常见的`@Override`就是一个注解。它的作用是提示由它修饰的方法是一个重写方法，如果父类没有这个方法编译器会报错。这样这个注解就给我们传达了重写方法这个信息，在使用时就会多加注意。
又如，在Spring体系中使用的注解，如`Service`、`Controller`等，告知Spring这是一个bean,并进行相应处理。

#### 2.四种元注解
想要自定义自己的注解，首先就需要知道怎样定义注解，在自定义注解之前，先了解下java4种元注解：
 - `@Target` 描述注解用于哪里，如类、方法或是字段。
    表明该注解是用于修饰类还是方法或字段。取值是一个`ElementType`枚举类。
    主要有以下值：
    ```
    ElementType.TYPE  //描述类、接口或enum声明
    ElementType.FIELD  //描述实例变量
    ElementType.METHOD  //方法
    ElementType.PARAMETER  //参数
    ElementType.CONSTRUCTOR  //构造函数
    ElementType.LOCAL_VARIABLE  //本地变量
    ElementType.ANNOTATION_TYPE  //另一个注释
    ElementType.PACKAGE //用于记录java文件的package信息
    ```
 - `@Retention` 指明注解的声明周期
    定义该注解信息在什么期间有效，取值为`RetentionPolicy`枚举类,主要有3个：
    ```
    RetentionPolicy.SOURCE：在编译期就丢弃，仅存在于源码中。如`@Override`等就属于此类
    RetentionPolicy.CLASS ：在类加载期丢弃，即注解将存在于字节码中。默认注解为此类型。
    RetentionPolicy.RUNTIME ：注解信息一直保留，不会丢弃。这意味着可以反射获取到注解信息。一般自定义注解大都使用此方式。
    ```
 - `@Documented` 注解是否包含在javaDoc中
 - `@Inherited` 是否允许子类继承该注解
 
### 3.自定义注解
注解使用以下`@interface`关键字定义：如定义一个名为`MyService`的注解：
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)  //自定义的注解大都是RetentionPolicy.RUNTIME类型的。
public @interface MyService {
}
```
**定义注解**只是编写注解的第一步,我们需要在合适的时机给她赋予逻辑,即我们怎么使用它？怎样获取他的信息？

举个例子来说明怎么获取和使用注解：
```java
public class TestAnno {

    @Hello(value = "world")
    public String hello() {
        return "hello";
    }

    public void callHello()  {
        try {
           TestAnno testAnno = new TestAnno();
            Method method = testAnno.getClass().getMethod("hello", null);
             //获取注解
            Hello hh = method.getAnnotation(Hello.class); 
            String str = method.invoke(testAnno, null).toString();
            
            //获取注解的值参数：hh.value()
            System.out.println(str + "," +hh.value() );
            
            Class<?> clazz = testAnno.getClass();
            //判断类上是否有声明MyController这个注解
            System.out.println(clazz.isAnnotationPresent(MyController.class));
            
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestAnno testAnno = new TestAnno();
        testAnno.callHello();
    }
}
```
