package com.uttamjmeter.resttest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;


/**
 * Hello world!
 *
 */
public class App 
{
	private static final String SAVESERVICE_PROPERTIES_FILE = "\\bin\\saveservice.properties"; 

	// Property name used to define file name
	private static final String SAVESERVICE_PROPERTIES = "saveservice_properties"; // $NON-NLS-1$

    public static void main( String[] args )
    {
    	  
    
               
        
    	  //JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
      
        JMeterUtils.setJMeterHome("C:\\jmeter\\apache-jmeter-5.0");
        //JMeter initialization (properties, log levels, locale, etc)

        JMeterUtils.loadJMeterProperties("C:\\jmeter\\apache-jmeter-5.0\\bin\\jmeter.properties");
//        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();

        // JMeter Test Plan, basic all u JOrphan HashTree
        HashTree testPlanTree = new HashTree();

        // HTTP Sampler
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("www.google.com");
//        httpSampler.setPort(80);
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");

//        // Loop Controller
//        LoopController loopController = new LoopController();
//        loopController.setLoops(10);
//        loopController.addTestElement(httpSampler);
//        loopController.setFirst(true);
//        loopController.initialize();
//
//        // Thread Group
//        ThreadGroup threadGroup = new ThreadGroup();
//        threadGroup.setNumThreads(1);
//        threadGroup.setRampUp(1);
//        threadGroup.setSamplerController(loopController);
//
//        // Test Plan
//        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
//
//        // Construct Test Plan from previously initialized elements
//        testPlanTree.add("testPlan", testPlan);
//        testPlanTree.add("loopController", loopController);
//        testPlanTree.add("threadGroup", threadGroup);
//        testPlanTree.add("httpSampler", httpSampler);
//
//        Summariser summer = null;
//        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
//        if (summariserName.length() > 0) {
//            summer = new Summariser(summariserName);
//        }
//
//        String logFile = "C:\\jmeter\\file.jtl";
//        ResultCollector logger = new ResultCollector(summer);
//        logger.setFilename(logFile);
//        testPlanTree.add(testPlanTree.getArray()[0], logger);
//        
//        // Run Test Plan
//        jmeter.configure(testPlanTree);
//        jmeter.run();
//        
//        
        
        
        //Second type

        // Loop Controller
        TestElement loopController = new LoopController();
        ((LoopController) loopController).setLoops(10);
        loopController.addTestElement(httpSampler);
        ((LoopController) loopController).setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        ((LoopController) loopController).initialize();

        // Thread Group

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(3);
        threadGroup.setRampUp(1);
        threadGroup.setName("Thread Group");
        threadGroup.setSamplerController(((LoopController) loopController));
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        // Test Plan
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Construct Test Plan from previously initialized elements
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);


        // save generated test plan to JMeter's .jmx file format
        try {
			SaveService.saveTree(testPlanTree, new FileOutputStream("C:\\jmeter\\test.jmx"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }


        // Store execution results into a .jtl file
        String logFile = "C:\\jmeter\\test1.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Run Test Plan
        jmeter.configure(testPlanTree);
        jmeter.run();

        System.out.println("Test completed. See test.jtl file for results");
        System.out.println("JMeter .jmx script is available at test.jmx");
        System.exit(0);
        
        
    }
}
