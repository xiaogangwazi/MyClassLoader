# MyClassLoader
自定义类加载器并实现热部署
java内置的三大类加载器：

BootStrap Classloader：（根类加载器）
负责加载虚拟机的核心类库。
Ext ClassLoader：（扩展类加载器）
负责JAVA_HOME下的jre/lb/ext子目录下的类库的加载。
Application ClassLoader：（系统类加载器）
之前两个类加载器更多的是和jdk有关的类加载器，系统类加载器是我们自己添加到项目中（classpath下）的jar包的加载。也是我们自定义类加载器的父类，所以我们要实现自己的类加载器只需要继承Application ClassLoder。

java中的类加载机制：父委托机制

破坏父委托机制：
如果我们行在程序运行事务时候进行某个模块功能的升级，甚至是在不停止服务的前提下新增功能，这就是我们常说的热部署，热部首先要卸载改模板所有class的类加载器，卸载类加载器会导致所有类的卸载，很显然我们不能对jvm三大类加载器进行卸载，我们只有通过通知自定义的类加载器才能做到这一点。
我们可以继承我们的自定义的类加载器，重写其中的loadClass方法，更改选择类加载器的顺序。
/**
 * 破坏类加载机制的父委托机制
 * 重写自定义加载器的loadClass方法，更改类加载的使用类加载器的顺序
 * 首先查看缓存中有没有类信息，有则直接返回
 * 没有的话在看类的名字 是以什么开头，若是以java或者javax开头则直接用系统类加载器
 * 否则先用自定义类加载器，如果没有加载到则查看有没有父加载器，有则使用父加载器，没有则使用系统加载器
 * 这样就破坏了加载顺序，先有自动以的加载器先加载而不是父类加载器
 */

类加载器的命名空间，运行时包，类的卸载等
1类加载器的命名空间
对于同一个类加载器来说，不同的类加载器实例加载的同一个类的class是同一个

由图可知：
对于同一个类加载器，所有的类加载器实例共享同一个类的唯一一个class对象，class对象指向一个类的类信息。
对于不同的类加载器来说，加载同一个类的class是不同的

有图可知对于不同的类加载器，会在方法区中保存一份自己的A类数据结构，在堆中有自己的关于A的class对象。也就是每一个类加载器去加载同一个类的话，都会有自己的一块区域。
运行时包：
我们在编写程序的时候，会给类添加包名，防止不同包下的相同名的class引起冲突，还起到封装的功能，报名和类名组成了类的全限定名称。运行时包是有类加载器的空间命名和类的全限定名称共同组成的，这样的好处是出于安全和封装，比如java.lang.Stirng有仅包可见的方法getChar，如果用户自己开发了一个java.lang.HackString类，并用自己定义的类加载器加载看，但是由于HackString和String类的运行时包名不一样，所以Hack是无法访问String的包可见方法的。
初始类加载器：
为什么在我们自定义的的类可以访问java.lang下面的类呢？我们知道java.lang包下的类是由根类加载器加载的。而我们用户自己定义的类是由系统类加载器或者自定义加载器加载的，属于两个不同的运行时包，但是为什么可以相互呢？在jav中有出初始化加载器和	定义类加载器，jvm会在每个参与列加载的类加载器中维护一张表，由于记录已经加载的class，当根类加载器器加载了String的class之后，会在所有参与了类加载的列加载器中添加该类的class，这些类加载器都叫该类的初始类加载器，而真正加载该类的class的类加载器叫做定义类加载器。所以我们自定义的类class和String的class虽然是由不同的类加载器加载的但是还是可以访问Sttring.
类的卸载：
JVM只有在满足了一下三种情况才会回收class卸载类：
该类的实例对象全部被回收
加载该类的类加载器实例被回收
该类的class实例没有在其他地方被引用

注意：java不允许我们编写的列的完全限定名和java的黑心类库的类的完全限定名一样的，在类加的defineClass的时候会有安全性检查。
自定义类加载器：
1.所有的自定义类加载器都要继承ClassLoader抽象类，改类没有抽象方法，但是有findClass方法，一定要实现该方法，不然会报class not found错误。

package MyClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 自定义类加载器
 ClassLoader的构造方法可以指定父加载器
 *
 */
public class MyClassLoader  extends ClassLoader {

    private final  static Path  DEFAULT_PATH= Paths.get("E:","classloader1");
    private final  Path classdir;
    public MyClassLoader(){
        super();
        classdir=DEFAULT_PATH;
    }
    public MyClassLoader(String classPath){
        super();
        this.classdir=Paths.get(classPath);
    }
    public MyClassLoader(ClassLoader parent ,String classPath) throws ClassNotFoundException {
        super(parent);
       classdir= Paths.get(classPath);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = this.readClassBytes(name);
        if(null==bytes||bytes.length==0){
            throw new ClassNotFoundException("Can not load the class"+name);
        }
        return this.defineClass(name,bytes,0,bytes.length);

    }

    private byte[] readClassBytes(String name)  throws ClassNotFoundException{
        String classPath= name.replace(".","/");
        Path classPullPath = classdir.resolve(Paths.get(classPath)+".class");
        if(!classPullPath.toFile().exists()){
            throw new ClassNotFoundException("The class"+name+"not found.");
        }
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            Files.copy(classPullPath,byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();

        }
        catch (IOException e){
            throw new ClassNotFoundException("load the class"+name+"occur error."+e);
        }
    }

    @Override
    public String toString() {
       return "My  ClassLoader";
    }
}

2.根据双亲委托机制可知：
当我们调用了classLoader加载class的时候，这个classLoader并不会立马加载，而是委托父加载器加载，直到委托到根加载器，然后再由根加载器开始向下加载。但是对于我们自己定义的类来说我们可能希望直接由我们自定义的列加载器来加载。所以这个手我们就希望打破这种双亲委托机制。
