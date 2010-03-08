package org.pillarone.riskanalytics.core.initialization;

import grails.util.GrailsUtil;
import groovy.lang.ExpandoMetaClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.codehaus.groovy.ant.FileScanner;
import org.codehaus.groovy.grails.commons.BootstrapArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.plugins.DefaultGrailsPlugin;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Iterator;

/**
 * Starting point of the standalone application.
 * Initializes log4j and starts grails, then executes all BootStraps and starts the UI.
 */
public class GrailsEnvironment {

    private static Log LOG = LogFactory.getLog(GrailsEnvironment.class);


    public static void setUp() throws Exception {

        String environment = System.getProperty("grails.env");
        if (environment == null) {
            environment = "development";
            System.setProperty("grails.env", environment);

        }
        StandaloneConfigLoader.loadLog4JConfig(environment);
        LOG.info("Starting RiskAnalytics with environment " + environment);


        LOG.info("Loading grails..");
        ExpandoMetaClass.enableGlobally();
        ApplicationContext ctx = GrailsUtil.bootstrapGrailsFromClassPath();
        GrailsApplication app = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID);

        String thisPluginClassName = app.getMetadata().get("app.name") + "GrailsPlugin";
        try {
            Class thisPluginClass = app.getClassLoader().loadClass(thisPluginClassName);
            DefaultGrailsPlugin grailsPlugin = new DefaultGrailsPlugin(thisPluginClass, app);
            grailsPlugin.doWithApplicationContext(app.getMainContext());
        } catch (ClassNotFoundException e) {
            //starting app is no plugin
        }

        LOG.info("Executing bootstraps..");
        GrailsClass[] bootstraps = app.getArtefacts(BootstrapArtefactHandler.TYPE);
        for (GrailsClass bootstrap : bootstraps) {
            final GrailsBootstrapClass bootstrapClass = (GrailsBootstrapClass) bootstrap;
            //Quartz bootstrap needs a servlet context
            if (!bootstrapClass.getClazz().getSimpleName().startsWith("Quartz")) {
                bootstrapClass.callInit(null);
            }
        }

        LOG.info("Grails successfully initialized");
    }
}
