package com.learning.servlet;

import com.learning.annotation.MyController;
import com.learning.annotation.MyRequestMapping;
import com.learning.annotation.MyResource;
import com.learning.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 自定义servlet
 * @author bxd
 * @date 2019/5/27.
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String,Object> ioc = new HashMap<String,Object>();
    private Map<String,Method> handlerMapping = new HashMap<>();
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        this.doPost(req,resp);
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)  {
        //调度，分发
        try {
            doDispatch( req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("------------------");
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{

        String url  = req.getRequestURI();
        String contentPath = req.getContextPath();
        url = url.replaceAll(contentPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 not found!");
            return;
        }
        Method method = this.handlerMapping.get(url);

        Map<String,String[]> paramterMap = req.getParameterMap();
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),new Object[]{req,resp,paramterMap.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config){
        //1,加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2,扫描所有相关的类
        doScanner(properties.getProperty("scanPackage"));
        //初始化相关类，并放到IOC容器
        doInstance();
        //完成依赖注入
        doResource();
        //初始化handlerMapping映射
        initHandlerMapping();
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {

            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className = scanPackage+"."+file.getName().replace(".class","");
                classNames.add(className);
            }

        }
    }


    private void doResource() {
        if(ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
           Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(MyResource.class)){
                    continue;
                }
                MyResource resource= field.getAnnotation(MyResource.class);
                String beanName = resource.name().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }
        try{
            for (String className : classNames) {
                Class clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(MyController.class)){
                    Object instance = clazz.newInstance();
                    String beanName=toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(MyService.class)){

                    // 默认首字母小写作为beanName
                    String beanName=toLowerFirstCase(clazz.getSimpleName());
                    //自定义beanName
                    MyService service = (MyService) clazz.getAnnotation(MyService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    //创建接口实例
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists!");
                        }
                        ioc.put(i.getName(),instance);
                    }


                }else{
                    continue;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void initHandlerMapping() {

        if(ioc.isEmpty()){return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
          Class<?> clazz =  entry.getValue().getClass();
          if(!clazz.isAnnotationPresent(MyController.class)){
              continue;
          }

          String baseUrl = "";
          if(clazz.isAnnotationPresent(MyRequestMapping.class)){
              baseUrl = clazz.getAnnotation(MyRequestMapping.class).value();
          }

          for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(MyRequestMapping.class)){
                    continue;
                }
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url =("/" +baseUrl+"/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
            }

        }

    }

    //首字母小写
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
