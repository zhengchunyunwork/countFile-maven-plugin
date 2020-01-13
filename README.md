## Maven插件开发

命名规范：插件命名为<yourplugin> -maven-plugin。
maven-<yourplugin>-plugin是Maven官方插件命名规范，不可使用。

### 创建插件
##### 1、新建Maven项目

```
groupId：com.maven.zcy
artufactId:countFile-maven-plugin
```


##### 2、pom文件设置打包方式<packaging>maven-plugin</packaging>，这是maven插件打包方式。


> pom.xml文件如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.maven.zcy</groupId>
    <artifactId>countFile-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>

    <packaging>maven-plugin</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-plugin-api</artifactId>
            <version>3.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.2</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```




##### 3、新建mojo类，继承AbstractMojo并重写execute方法。


```
@Mojo(name="sayhello")
public class SayHello extends AbstractMojo{

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Hello MavenPlugin");
    }
}
```

##### 4、编写统计文件的maven插件

文件与代码行数统计插件

```
package com.fxtahe.plugin;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Mojo(name="count")
public class CountFileAndDirary extends AbstractMojo{

    private static final String[] INCLUDES_DEFAULT={"properties","xml","java","yml"};

    @Parameter(defaultValue = "${basedir}")
    private File baseDir;

    @Parameter(defaultValue = "${project.build.resources}",readonly = true,required = true)
    private List<Resource> resources;

    @Parameter(defaultValue = "${project.build.sourceDirectory}",required = true,readonly = true)
    private File sourceDir;

    @Parameter(defaultValue = "${project.build.testResources}",readonly = true,required = true)
    private List<Resource> testResources;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}",readonly = true,required = true)
    private File testSourceDir;

    @Parameter(property ="count.include")
    private String[] includes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("baseDir目录"+baseDir);
        if(includes.length==0 || includes==null){
            includes = INCLUDES_DEFAULT;
        }

        try {
            countDir(sourceDir);

            countDir(testSourceDir);

            for (Resource resource : resources) {
                countDir(new File(resource.getDirectory()));
            }

            for (Resource testResource : testResources) {
                countDir(new File(testResource.getDirectory()));
            }
        }catch (IOException e){
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void showInclude() {
        getLog().info("include包括"+ Arrays.asList(includes));
    }

    public void countDir(File file) throws IOException {

        for(String fileType:includes){
            getLog().info(file.getAbsolutePath()
                    .substring(baseDir.getName().length())
                    +"目录："+fileType+"文件共计"+countFile(file,fileType));
            getLog().info(file.getAbsolutePath()
                    .substring(baseDir.getName().length())
                    +"目录"+fileType+"文件代码共计行数："+countLine(file,fileType));
        }


    }

    public int countFile(File file,String fileType){
        int num =0;
        if(file.isFile() && file.getName().endsWith("."+fileType)){
            return num++;
        }

        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f : files){
                if(f.isFile() && f.getName().endsWith("."+fileType)){
                    num++;
                }else{
                    num += countFile(f,fileType);
                }
            }
        }
        return num;
    }

    public int countLine(File file,String fileType) throws IOException {
        int countline=0;

        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f:files){
                if(f.isFile() && f.getName().endsWith("."+fileType)){
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    while(br.readLine()!=null){
                        countline++;
                    }
                }else{
                    countline+=countLine(f,fileType);
                }
            }
        }
        return countline;
    }
}
```


### 使用插件

##### 1、在项目中添加插件

```
<build>
    <plugins>
        <plugin>
            <groupId>com.maven.zcy</groupId>
            <artifactId>countFile-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

##### 2、配置运行：
可以通过完全格式的命令执行插件


```
mvn groupId ：artifactId ：version ：goal
```


则执行本插件命令为：


```
mvn com.maven.zcy:countFile-maven-plugin:1.0-SNAPSHOT:count
```


因为插件命名遵守${prefix}-maven-plugin,所以可以这样执行


```
countFile:count
```

##### 3、配置到生命周期

配置文件配置生命周期

    
```
<plugin>
        <groupId>com.maven.zcy</groupId>
        <artifactId>countFile-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <!-- 配置compile执行 -->
                <phase>compile</phase>
                <goals>
                    <!-- 配置执行目标 -->
                    <goal>count</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
```


##### 4、参数配置

###### 可以通过Mojo私有参数配置插件的可选配置

   
```
    @Parameter(property = "url",defaultValue = "fxtahe.com")
    private String str;
```


###### 在插件配置中可配置


```
<configuration>
    <url>www.baidu.com</url>
</configuration>
```


###### property属性可以让用户通过命令行-D的方式配置参数，当pom已经配置了遵从pom中的配置。

```
mvn compile -Durl=http://maven.apache.org
```


相当于


```
<configuration>
    <str>http://maven.apache.org</str>
</configuration>
```


> 注意：本地配置优先级高于命令行

> 可以配置参数类型


```
boolean
Integer整型
Double/double、Float/float
Date
File
URL
Text
Enums
数组
集合
Map
Properties
对象
```

具体配置：https://maven.apache.org/guides/plugin/guide-java-plugin-development.html


```

@Paramter类
public @interface Parameter {
	//别名
    String alias() default "";
	//属性，通过命令行可配置
    String property() default "";
	//默认值
    String defaultValue() default "";
	//是否必须，默认false
    boolean required() default false;
	//是否只读，默认false
    boolean readonly() default false;
}
```


##### 5、隐式变量
maven提供三个隐式变量，用来访问系统环境变量、POM信息和maven的setting

```
${basedir} 项目根目录
${project.build.directory} 构建目录，缺省为target
${project.build.outputDirectory} 构建过程输出目录，缺省为target/classes
${project.build.finalName} 产出物名称，缺省为 ${project.artifactId}- ${project.version}
${project.build.resources} main文件夹下的java代码的目录
${project.build.sourceDirectory} main文件夹下的resources的目录
${project.build.testResources} test文件夹下的java代码的目录
${project.build.testSourceDirectory} test文件夹下的resources的目录
${project.packaging} 打包类型，缺省为jar
${project.xxx} 当前pom文件的任意节点的内容
```





> 参考

maven插件解析机制：https://www.cnblogs.com/AlanLee/p/6208562.html

插件开发中文官方文档：http://ifeve.com/maven-index-2/