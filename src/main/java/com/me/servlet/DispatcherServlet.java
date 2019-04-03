package com.me.servlet;

import com.me.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    //存放所有class文件的全路径集合
    private List<String> classNames = new ArrayList<>();

    //存放所有的类实例对象
    //ioc容器就是个map
    private Map<String,Object> instanceMap = new HashMap<>();

    //存放所有的路径-方法映射
    private List<Handler> handlerMapping = new ArrayList<>();

    public DispatcherServlet() {

    }

    /**
     * 启动tomcat时会加载spring mvc的init方法
     */
    @Override
    public void init(ServletConfig config) {

        //1、读取配置文件
        String basePackage = config.getInitParameter("scanPackage");

        //2.扫描项目下所有class类
        //doScanPackage("com.me");      //也可手动指定包路径
        doScanPackage(basePackage);     //使用配置的包路径，方便维护

        //3.创建类实例
        doInstance();

        //4.建立依赖关系，自动依赖注入
        autowired();

        //5.建立url与method的映射关系
        handlerMapping();

        System.out.println("my mvc init finished !!!");


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        this.doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        try{
            //根据用户请求的url去找到其对应的Method
            boolean isMatcher = pattern(request,response);
            if(!isMatcher){
                response.getWriter().write("404 Not Found");
            }
        }catch (Exception e){
            response.getWriter().write("500 Exception,Details:\r\n" +
                    e.getMessage() + "\r\n" +
                    Arrays.toString(e.getStackTrace()).replaceAll("\\[\\]", "")
                            .replaceAll(",\\s", "\r\n"));
        }
    }


    public void doScanPackage(String basePackage) {
        //根据配置的路径拿到class所在目录
        URL url = this.getClass()
                .getClassLoader()
                .getResource("/" + basePackage.replaceAll("\\.","/"));

        String fileStr = url.getFile();
        //路径转文件对象
        File fileDir = new File(fileStr);
        //遍历路径下的文件
        for (String path : fileDir.list()) {
            File file = new File(fileStr + path);

            //如果对象是文件夹, 递归
            if (file.isDirectory()) {
                doScanPackage(basePackage + "." + path);
            } else {
                //得到class 全类名路径 com.me.controller.TestController.class,com.me.service.TestService.lass,....
                classNames.add(basePackage + "." + file.getName());
            }
        }

    }

    //利用反射机制将扫描到的类名全部实例化
    public void doInstance() {
        //没有扫描到类
        if (classNames.size() <= 0) {
            System.out.println("scan  failed .......");
            return;
        }
        //遍历类数组
        for (String className : classNames) {
            String cname = className.replace(".class","");
            try{
                //使用Class的静态方法forName() 动态加载类,如果找不到会报异常ClassNotFundException
                Class<?> clazz = Class.forName(cname);

                //判断哪些类加了@MyController,@MyService注解
                if(clazz.isAnnotationPresent(MyController.class)) {
                    String beanName = lowerFirstChar(clazz.getSimpleName());
                    instanceMap.put(beanName, clazz.newInstance());

                } else if (clazz.isAnnotationPresent(MyService.class)) {

                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    if (!"".equals(beanName.trim())) {
                        instanceMap.put(beanName,clazz.newInstance());
                        continue;
                    }
                    //如果@service注解没有加名字，使用接口名称
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i:interfaces) {
                        instanceMap.put(i.getName(),clazz.newInstance());
                    }
                } else {
                    continue;
                }

                //判断类上是否有声明MyController这个注解
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object instance = clazz.newInstance();

                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 自动注入依赖
     */
    private void autowired() {
        if(instanceMap.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry:instanceMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields){
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }

                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                //如果注解没有加名称，使用类型名称进行注入
                if("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try{
                    field.set(entry.getValue(),instanceMap.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }


        }

    }

    private void handlerMapping() {
        if (instanceMap.isEmpty()) {
            return;
        }

        for(Map.Entry<String,Object> entry : instanceMap.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            //解析controller注解上的路径参数
            String url = "";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                url = requestMapping.value();
            }

            //解析方法上的路径参数
            Method[] methods = clazz.getMethods();
            for(Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }

                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String mappingUrl = requestMapping.value();
                String urlRegex = ("/" + url + mappingUrl).replaceAll("/+","/");
                String regex = urlRegex.replaceAll("\\*", ".*");

                HashMap<String, Integer> map = new HashMap<>();
                Annotation[][] pa = method.getParameterAnnotations();
                for(int i = 0; i < pa.length; i++) {
                    for(Annotation a : pa[i]) {
                        if (a instanceof MyRequestParams) {
                            String paramName = ((MyRequestParams) a).value();
                            if (!"".equals(paramName.trim())) {
                                map.put(paramName,i);
                            }
                        }
                    }
                }

                //提取Request和Response的索引
                Class<?>[] parameterTypes = method.getParameterTypes();
                for(int i = 0; i< parameterTypes.length; i++) {
                    Class<?> type = parameterTypes[i];
                    if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                        map.put(type.getName(),i);
                    }
                }

                handlerMapping.add(new Handler(Pattern.compile(regex),entry.getValue(),method,map));
                System.out.println("Mapping " + urlRegex + " " + method);
            }
        }
    }


    /**
     * 根据请求路径 匹配method
     * @param req
     * @param resp
     * @return
     * @throws Exception
     */
    public boolean pattern(HttpServletRequest req, HttpServletResponse resp) throws Exception{


        return true;

    }

    /**
     * 首字母转换成小写
     * @param str
     * @return
     */
    public String lowerFirstChar(String str){

        char [] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);

    }

    private class Handler{
        protected Pattern pattern;
        protected Object controller;
        protected Method method;
        protected Map<String,Integer> paramMapping;

        protected Handler(Pattern pattern,Object controller,Method method,Map paramMapping){
            this.pattern = pattern;
            this.controller = controller;
            this.method = method;
            this.paramMapping = paramMapping;
        }
    }
}
