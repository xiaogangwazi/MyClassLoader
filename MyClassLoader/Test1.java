package MyClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 同一个列加载器的不同实例加载的class是同一个
 * 不同类加载器加载的同一个类的class是不同的
 * 所以说同一个类的class对象在同一个类加载器命名空间下是同一个，不同的类加载器的类的class是不一样的
 */
public class Test1 {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, InterruptedException {

       while(true) {
           BrockdelegateClassLoader brockdelegateClassLoader = new BrockdelegateClassLoader("E:\\classloader1");
           Class<?> aClass1 = brockdelegateClassLoader.loadClass("MyClassLoader.HelloWorld");
           Object o = aClass1.newInstance();
           Method say = aClass1.getMethod("say");
           Object invoke = say.invoke(o);
           System.out.println(invoke);
        Thread.sleep(5000);
       }
    }
}
