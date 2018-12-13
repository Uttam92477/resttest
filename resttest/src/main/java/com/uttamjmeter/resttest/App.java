package com.uttamjmeter.resttest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;

/**
 * Created by uttam.r v1 This App does the following: 1. Creates a simple JMX
 * with a HTTP Sampler(type HTTPSamplerProxy), ThreadGroup, LoopController,
 * ResultCollector 2. Runs the JMeter test 3. Saves the result in file
 * 
 *
 */
public class App {
	/*
	 * TEST PLAN
	 */
	private static final int THREAD_COUNT = 1;
	private static final int RAMP_UP_SECONDS = 1;
	private static final int LOOP_COUNT = 1;

	/*
	 * HTTP Sampler props
	 */
	private static final String PROTOCOL = "https";// https or http
	private static final String DOMAIN = "api.github.com";// domain
	private static final int PORT = 443;// port
	private static final String ACCESS_TOKEN = "e1ed8c07fde0c56f167b02f5295c622c964c347d";// TODO sensitive information:
																							// Delete before sharing
	private static final String PATH = "/repos/Uttam92477/test?access_token=" + ACCESS_TOKEN;// path
	private static final String METHOD = "PATCH";// GET, POST, etc..
	private static final String RAW_DATA = "{\r\n" + "  \"name\": \"test\",\r\n"
			+ "  \"description\": \"This is the actual description\",\r\n"
			+ "  \"homepage\": \"https://github.com\",\r\n" + "  \"private\": false,\r\n"
			+ "  \"has_issues\": true,\r\n" + "  \"has_projects\": true,\r\n" + "  \"has_wiki\": true\r\n" + "}";// DATA
																													// to
																													// be
																													// sent

	/*
	 * JMETER PATHS
	 */

	private static final String JMETER_HOME = "C:\\jmeter\\apache-jmeter-5.0";
	private static final String JMETER_PROPERTIES_FILEPATH = "C:\\jmeter\\apache-jmeter-5.0\\bin\\jmeter.properties";

	public static void main(String[] args) {

		// JMeter Engine
		StandardJMeterEngine jmeter = new StandardJMeterEngine();

		JMeterUtils.setJMeterHome(JMETER_HOME);
		// JMeter initialization (properties, log levels, locale, etc)

		JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES_FILEPATH);
//        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
		JMeterUtils.initLocale();

		// JMeter Test Plan, basic all u JOrphan HashTree
		HashTree testPlanTree = new HashTree();

		// CSV
		// TODO Test pending. Following code is to test if we can add variables using a
		// csv file
		CSVDataSet csv = new CSVDataSet();
		csv.setProperty("filename", "C:/jmeter/test.csv");
		csv.setProperty("variableNames", "domain");
		csv.setProperty("ignoreFirstLine", false);
		csv.setProperty("quotedData", false);
		csv.setProperty("delimiter", ",");
		csv.setProperty("recycle", true);
		csv.setProperty("stopThread", false);
		csv.setProperty("shareMode", "shareMode.all");
		csv.setProperty(TestElement.ENABLED, true);

		csv.setProperty(CSVDataSet.TEST_CLASS, CSVDataSet.class.getName());
		csv.setProperty(CSVDataSet.GUI_CLASS, TestBeanGUI.class.getName());

		// HTTP Sampler
		HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
		httpSampler.setDomain(DOMAIN);
		httpSampler.setPort(PORT);
		httpSampler.setProtocol(PROTOCOL);
		httpSampler.setPath(PATH);
		httpSampler.setMethod(METHOD);
		httpSampler.setProperty(HTTPSampler.PROXYHOST, "localHost");
		httpSampler.setProperty(HTTPSampler.PROXYPORT, "8888");
		httpSampler.setProperty(HTTPSampler.POST_BODY_RAW, true);
		httpSampler.addNonEncodedArgument("", RAW_DATA, "=");
		httpSampler.setProperty(TestElement.ENABLED, true);
		httpSampler.setResponseTimeout("20000");
		httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
		httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

		// Loop Controller
		TestElement loopController = new LoopController();
		((LoopController) loopController).setLoops(LOOP_COUNT);
		loopController.addTestElement(httpSampler);
		((LoopController) loopController).setFirst(true);
		loopController.setProperty(TestElement.ENABLED, true);
		loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
		loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
		((LoopController) loopController).initialize();

		// Thread Group
		ThreadGroup threadGroup = new ThreadGroup();
		threadGroup.setNumThreads(THREAD_COUNT);
		threadGroup.setRampUp(RAMP_UP_SECONDS);
		threadGroup.setName("Thread Group");
		threadGroup.setSamplerController(((LoopController) loopController));
		threadGroup.setProperty(TestElement.ENABLED, true);
		threadGroup.setProperty(TestElement.NAME, "Thread Group");
		threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
		threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

		// Test Plan
		TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
		testPlan.setProperty(TestElement.ENABLED, true);
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
		testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

		// Construct Test Plan from previously initialized elements
		testPlanTree.add(testPlan);
		HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
		HashTree httpSamplerHashTree = threadGroupHashTree.add(httpSampler, csv);

		// add Summarizer output to get test progress in stdout like:
		// summary = 2 in 1.3s = 1.5/s Avg: 631 Min: 290 Max: 973 Err: 0 (0.00%)
		Summariser summer = null;
		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
		if (summariserName.length() > 0) {
			summer = new Summariser(summariserName);
		}

		SampleSaveConfiguration sampleSaveConfiguration = SampleSaveConfiguration.staticConfig();
		sampleSaveConfiguration.setSamplerData(true);
		;

		// Store execution results into a .jtl file
		String summaryReportFile = "C:\\jmeter\\test1.jtl";
		ResultCollector summaryReport = new ResultCollector(summer);
		summaryReport.setProperty(TestElement.GUI_CLASS, SummaryReport.class.getName());
		summaryReport.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
		summaryReport.setFilename(summaryReportFile);
		summaryReport.setProperty("ResultCollector.error_logging", "true");
		testPlanTree.add(testPlanTree.getArray()[0], summaryReport);

		// Store execution results into a .jtl file
		String viewResultTreeFile = "C:\\jmeter\\test2.jtl";
		ResultCollector viewResultTree = new ResultCollector();
		viewResultTree.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());
		viewResultTree.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
		viewResultTree.setFilename(viewResultTreeFile);
		viewResultTree.setProperty("ResultCollector.error_logging", "true");
		testPlanTree.add(testPlanTree.getArray()[0], viewResultTree);

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

		// Run Test Plan
		jmeter.configure(testPlanTree);
		jmeter.run();

		System.out.println("Test completed. See test.jtl file for results");
		System.out.println("JMeter .jmx script is available at test.jmx");
		System.exit(0);

	}
}
