package com.maven.zcy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name="sayhello")
public class SayHello extends AbstractMojo{

	@Parameter(property="url", defaultValue="hello maven plugins on properties")
	private String str;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("hello maven plugins");
		getLog().info(str);
	}
}
