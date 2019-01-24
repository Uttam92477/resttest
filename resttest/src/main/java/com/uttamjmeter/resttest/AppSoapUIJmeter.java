package com.uttamjmeter.resttest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.SoapUIException;

public class AppSoapUIJmeter {
	

	/*
	 * Working directory for our Application. The log files, config files will be created here
	 */
	
	private static final String workingDirectory="C:\\uttam\\";
	
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

		AppSoapUIJmeter app= new AppSoapUIJmeter();
		app.generateConfigFile("");
	}
	
	
	//TODO Read the WSDL and write the URLs, inputs to the config file
	/*
	 * This method reads the WSDL file, prints the URL,
	 * the sample request to a config file
	 * 
	 */
	private void generateConfigFile(String wsdlFilePath) {
		// create new project
		
		try {
			WsdlProject project = new WsdlProject();

			// import amazon wsdl
			WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project,
			 "file:///C:\\Users\\uttam.r\\SoapUI-Tutorials\\WSDL-WADL\\sample-service.wsdl", true )[0];

			// get desired operation
			WsdlOperation operation = 
			  (WsdlOperation) iface.getOperationByName( "addPerson" );
			System.out.println("Action: "+operation.getAction());

			// create a new empty request for that operation
			WsdlRequest request = operation.addNewRequest( "My request" );

			// generate the request content from the schema
			request.setRequestContent( operation.createRequest( true ) );
			
			//Read the request content
			//Replace each ? with a parameter name if found in the CSV of Jmeter project
			//Remember that we arent sending the request from SOAPUI
			//SOAPUI is being used only to generate a sample request
			
			String requestContent=request.getRequestContent();
			requestContent=requestContent.replaceAll(">\\s+<", "><");
					
			System.out.println("Sample before Request:\n"+request.getRequestContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(requestContent));

			Document doc = builder.parse(src);
//			doc.getElementsByTagName("login");
			
			
//			NodeList nodeList= doc.getElementsByTagName("*");
			for(int i=0;i<doc.getElementsByTagName("*").getLength();i++) {
			System.out.println(doc.getElementsByTagName("*").item(i).getTextContent());
			if(doc.getElementsByTagName("*").item(i).getTextContent().equals("?")) {
				
				
				doc.getElementsByTagName("*").item(i).setTextContent("${"+doc.getElementsByTagName("*").item(i).getNodeName()+"}");
				System.out.println("BBBBBBB: The content of "+doc.getElementsByTagName("*").item(i).getNodeName()+" is changed from ? to "+"${"+doc.getElementsByTagName("*").item(i).getNodeName()+"}");
			}
			System.out.println("AAAAAAAAAA"+i);
			}
			
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			System.out.println("XML IN String format is: \n" + writer.toString());
			
//			System.out.println("Sample after Request:\n"+doc);
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(0));
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(1));
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(2));
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(3));
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(4));
			System.out.println("aaaaaaaaaaaa");
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(0).getTextContent());
			System.out.println("bbbbbbbbbbbb");
			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().item(0).getNodeName());
			System.out.println("cccccccccccc");
//			System.out.println(doc.getElementsByTagName("Body").item(0).getChildNodes().item(0).getNodeName());
//			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().getLength());
//			System.out.println(doc.getElementsByTagName("sam:login").item(0).getChildNodes().getLength());
			
			

			System.exit(0);
		} catch (XmlException | IOException | SoapUIException | ParserConfigurationException | SAXException | TransformerException e) {
			e.printStackTrace();
		}

	}
	
	
	//TODO execute smoke test using the config file, add the responses of smoke test to the config file
	/*
	 * Reads config file and executes a smoke run
	 * Fills the config file with responses received during the smoke run
	 * Logs any errors to the log file - timestamp based file
	 */
	private void createAndExecuteSmokeRun() {

		//TODO Modify; currently contains sample code to create and run a jmeter test. The first code


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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run Test Plan
		jmeter.configure(testPlanTree);
		jmeter.run();

		System.out.println("Test completed. See test.jtl file for results");
		System.out.println("JMeter .jmx script is available at test.jmx");
		System.exit(0);

	
	}
	
	//TODO execute actual run based on test config file
	/*
	 * Read the test config file and create test plan, generate jmx from the test plan,
	 * execute the test and generate report
	 * 
	 */
	private void createAndEecuteActualRun() {
		
	}
	
	/*
	 * TODO pending things
	 * 1. APIConfig file contents: URLs, requests, (expected)responses
	 * 2. TestConfig file contains, threads for each request, loop count for each request, any other delays etc..
	 * 2a. Test config file will be populated with URLs or services after smoke run
	 * 
	 * 
	 * Tries
	 * 1. See if Result collector and summarizer are working as expected if you are running
	 * the test in JMeter GUI
	 * 2.
	 * 
	 * What to print in the config file:
	 * 1. Service Name and documentation
	 * 2. Each complete URL (along with path example: www.example.com/exxxService/getUser)
	 * 3. What is the input
	 * 4. What is the expected output
	 * 
	 */

}
