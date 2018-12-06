package com.uttamjmeter.resttest;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileInputStream;

public class AppExistingJMX {
	
	
	
	private static final String JMX_FILE_PATH="C:\\Users\\uttam.r\\Desktop\\TestExistingJMX.jmx";
//	private static final String JMX_FILE_PATH="C:\\jmeter\\test.jmx";
	

	/*
	 * JMETER PATHS
	 */
	
	private static final String JMETER_HOME="C:\\jmeter\\apache-jmeter-5.0";
	private static final String JMETER_PROPERTIES_FILEPATH="C:\\jmeter\\apache-jmeter-5.0\\bin\\jmeter.properties";
	

    public static void main(String[] argv) throws Exception {
        // JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();


        // Initialize Properties, logging, locale, etc.
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES_FILEPATH);
        JMeterUtils.setJMeterHome(JMETER_HOME);
//        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();

        // Initialize JMeter SaveService
        SaveService.loadProperties();

        // Load existing .jmx Test Plan
        File in = new File(JMX_FILE_PATH);
        HashTree testPlanTree = SaveService.loadTree(in);
//        in.close();

      //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        
        SampleSaveConfiguration sampleSaveConfiguration= SampleSaveConfiguration.staticConfig();
        sampleSaveConfiguration.setSamplerData(true);;
        
        // Store execution results into a .jtl file
        String summaryReportFile = "C:\\jmeter\\test1.jtl";
        ResultCollector summaryReport = new ResultCollector(summer);
        summaryReport.setProperty(TestElement.GUI_CLASS,SummaryReport.class.getName());
        summaryReport.setProperty(TestElement.TEST_CLASS,ResultCollector.class.getName());
        summaryReport.setFilename(summaryReportFile);
        summaryReport.setProperty("ResultCollector.error_logging","true");
        testPlanTree.add(testPlanTree.getArray()[0], summaryReport);
        
        // Store execution results into a .jtl file
        String viewResultTreeFile = "C:\\jmeter\\test2.jtl";
        ResultCollector viewResultTree = new ResultCollector();
        viewResultTree.setProperty(TestElement.GUI_CLASS,ViewResultsFullVisualizer.class.getName());
        viewResultTree.setProperty(TestElement.TEST_CLASS,ResultCollector.class.getName());
        viewResultTree.setFilename(viewResultTreeFile);
        viewResultTree.setProperty("ResultCollector.error_logging","true");
        testPlanTree.add(testPlanTree.getArray()[0], viewResultTree);

     
        // Run JMeter Test
        jmeter.configure(testPlanTree);
        jmeter.run();
    }
}
