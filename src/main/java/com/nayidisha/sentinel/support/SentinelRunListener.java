package com.nayidisha.sentinel.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pankajt on 7/12/17.
 */
public class SentinelRunListener implements SpringApplicationRunListener {

    Logger log = LoggerFactory.getLogger(SentinelRunListener.class);

    //@Value("${custom.jarsDirectory}")
    private String jarsDirectory = "/Users/pankajt/myTemp";

    private final SpringApplication application;
    private final String[] args;
    private final SimpleApplicationEventMulticaster initialMulticaster;

    public SentinelRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
        this.initialMulticaster = new SimpleApplicationEventMulticaster();
        Iterator var = application.getListeners().iterator();

        while(var.hasNext()) {
            ApplicationListener<?> listener = (ApplicationListener)var.next();
            this.initialMulticaster.addApplicationListener(listener);
        }
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment configurableEnvironment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {

        System.out.println("J: " + jarsDirectory);
        File[] jarFiles = new File(jarsDirectory).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jarFiles == null) {
            log.info("No jar files found. Not loading custom Jars");
            return;
        }
        log.info("Jar files found at " + jarsDirectory);

        List<URL> urlList = new ArrayList();

        Arrays.asList(jarFiles).forEach(j -> {
            try {
                urlList.add(j.toURI().toURL());
            } catch (Exception e){
                log.warn(String.format("Could not convert %s to URL", j.toString()));
            }
        });

        // create a new classloader which contains the jars...
        ClassLoader extendedClassloader = new URLClassLoader(((URL[])urlList.toArray(new URL[urlList.size()])), configurableApplicationContext.getClassLoader());

        // and replace the context's classloader
        ((DefaultResourceLoader) configurableApplicationContext).setClassLoader(extendedClassloader);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {

    }

    @Override
    public void finished(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {

    }
}
