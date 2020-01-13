package com.maven.zcy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name="count")
public class CountFileAndDirary extends AbstractMojo {

    private static final String[] INCLUDES_DEFAULT={"properties","xml","java","yml"};

    @Parameter(defaultValue = "${basedir}")
    private File baseDir;


    @Parameter(defaultValue = "${project.build.resources}",readonly = true,required = true)
    private List<Resource> resources;

    @Parameter(defaultValue = "${project.build.sourceDirectory}",required = true,readonly = true)
    private File sourceDir;

    @Parameter(defaultValue = "${project.build.testResources}",readonly = true,required = true)
    private List<Resource> testResources;

    @Parameter(property ="count.include")
    private String[] includes;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}",readonly = true,required = true)
    private File testSourceDir;

    @Parameter(defaultValue = "${project.build.directory}",readonly = true,required = true)
    private File buildDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}",readonly = true,required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.finalName}",readonly = true,required = true)
    private String finalName;

    @Parameter(defaultValue = "${project.packaging}",readonly = true,required = true)
    private String packagingName;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("baseDir:" + baseDir.getAbsolutePath());
        getLog().info("resources================== start");
        printdir(resources);
        getLog().info("resources================== end");
        getLog().info("sourceDir:" + sourceDir.getAbsolutePath());
        getLog().info("testResources================== start");
        printdir(testResources);
//		getLog().info("testResources:" + testResources);
        getLog().info("testResources================== end");
        getLog().info("testSourceDir:" + testSourceDir.getAbsolutePath());

        getLog().info("buildDirectory:" + buildDirectory.getAbsolutePath());

        getLog().info("outputDirectory:" + outputDirectory.getAbsolutePath());

        getLog().info("finalName:" + finalName);

        getLog().info("packagingName:" + packagingName);

        getLog().info("====================================================");
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


    public void countDir(File file) throws IOException {

        for(String fileType:includes){
            getLog().info(file.getAbsolutePath().substring(baseDir.getName().length())
                    +"目录："+fileType+"文件共计"+countFile(file,fileType));
            getLog().info(file.getAbsolutePath().substring(baseDir.getName().length())
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
                    br.close();
                }else{
                    countline+=countLine(f,fileType);
                }
            }
        }
        return countline;
    }

    private void printdir( List<Resource> list) {
        if(list != null && list.size() > 0) {
            for(Resource res : list) {
                getLog().info(res.getDirectory());
            }
        }
    }


}
