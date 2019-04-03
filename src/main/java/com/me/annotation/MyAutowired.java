package com.me.annotation;

import java.lang.annotation.*;

/**
 * 4种元注解
 *  @Target 描述注解用于哪里，如类、方法或是字段。
 *      表明该注解是用于修饰类还是方法或字段。取值是一个ElementType枚举类。
 *      主要有以下值：
 *          ElementType.TYPE  //描述类、接口或enum声明
 *          ElementType.FIELD  //描述实例变量
 *          ElementType.METHOD  //方法
 *          ElementType.PARAMETER  //参数
 *          ElementType.CONSTRUCTOR  //构造函数
 *          ElementType.LOCAL_VARIABLE  //本地变量
 *          ElementType.ANNOTATION_TYPE  //另一个注释
 *          ElementType.PACKAGE //用于记录java文件的package信息
 *
 *  @Retention 指明注解的声明周期
 *      定义该注解信息在什么期间有效，取值为RetentionPolicy枚举类,
 *      主要有3个：
 *      RetentionPolicy.SOURCE：在编译期就丢弃，仅存在于源码中。如@Override等就属于此类
 *      RetentionPolicy.CLASS ：在类加载期丢弃，即注解将存在于字节码中。默认注解为此类型。
 *      RetentionPolicy.RUNTIME ：注解信息一直保留，不会丢弃。这意味着可以反射获取到注解信息。一般自定义注解大都使用此方式。
 *  @Documented 注解是否包含在javaDoc中
 *  @Inherited 是否允许子类继承该注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";

}
