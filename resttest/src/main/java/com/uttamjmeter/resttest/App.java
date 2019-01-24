package com.uttamjmeter.resttest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.CSVDataSetBeanInfo;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.TransactionControllerGui;
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
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.xmlbeans.XmlException;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.BindingOperation;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.SoapUIException;

/*
 * 
 */

public class App {
	/*
	 * Working directory for our Application. The log files, config files will be created here
	 */
	private static final String WORKING_DIRECTORY="C:\\uttam\\";
	/*
	 * JMETER PATHS
	 */
	private static final String JMETER_HOME = "C:\\jmeter\\apache-jmeter-5.0";
	private static final String JMETER_PROPERTIES_FILEPATH = "C:\\jmeter\\apache-jmeter-5.0\\bin\\jmeter.properties";

	
	public static void main(String[] args) {
		App app= new App();
		app.generateConfigFile("file:///C:\\Users\\uttam.r\\SoapUI-Tutorials\\WSDL-WADL\\sample-service.wsdl");
		List<CustomOperation> operations=app.readConfigFile(Paths.get("C:\\uttam\\config.txt"));
		app.createAndExecuteSmokeRun(operations);
		System.exit(0);
	}

	
	//TODO Read the WSDL and write the URLs, inputs to the config file
	/*
	 * This method reads the WSDL file,
	 * Creates the config file
	 * Config file contains all the operations in the WSDL
	 * Sample request (from SOAP UI APIs)
	 * Sample response (from SOAP UI APIs)
	 * Any parameters which show as '?' will be replaced with '${Respective Tag Name}'
	 **This is to enable Jmeter variable integration from CSV file
	 **For example, a line '<username>?</username>' will be replaced with '<username>${username}</username>'
	 **Then when the request is submitted using the JMeter APIs it will search for the variable 'username'
	 **in the CSV file linked
	 *
	 **If the tag name is in format <soapetc..>:<name part>, only the <name part> will be used ad ${<name part>}
	 **Example, <soap:username>?</soap:username> will become <soap:username>${username}</soap:username>
	 * 
	 */
	private void generateConfigFile(String wsdlFilePath) {
		
		try {
			//Open or create a file
			ArrayList<String> configFilelines= new ArrayList<String>();
			Path file= Paths.get(WORKING_DIRECTORY+"config.txt");
			Files.deleteIfExists(file);
			
			//EasyWSDL
			WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
			Description desc = reader.read(new URL(wsdlFilePath));
			
			//Create new SOAP Project
			WsdlProject project = new WsdlProject();
			//Import the WSDL
			WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, wsdlFilePath , true )[0];
			
			Map<Endpoint,BindingOperation> map= new HashMap<>();
			
			List<Service> services = desc.getServices();
			int serviceCounter=0;
			int endpointCounter=0;
			int operationCounter=0;
			configFilelines.add("CONFIGSTART");
			for(Service service:services) {
				/*
				 * TODO
				 * 1. Print the Service Name in config
				 * 2. Get all the end points in that service and iterate
				 */
				configFilelines.add("Service"+(++serviceCounter)+"START");
				configFilelines.add(service.getQName().getLocalPart());
				List<Endpoint> endpoints= service.getEndpoints();
				for(Endpoint endpoint: endpoints) {
					/*
					 * TODO
					 * 1. Print the endpoint
					 * 2. Get the binding, binding operations list and name of each operation
					 * 3. For each operation name, get the SOAP UI wsdlOperation
					 * 4. Create SOAP request etc..
					 */
					configFilelines.add("Endpoint"+(++endpointCounter)+"START");
					List<BindingOperation> bindOperations=endpoint.getBinding().getBindingOperations();
					for(BindingOperation bindingOperation:bindOperations) {
						configFilelines.add("Operation"+(++operationCounter)+"START");
						map.put(endpoint, bindingOperation);//AND the soap ui thingy
						//OR
						
						WsdlOperation wsdlOperation= iface.getOperationByName(bindingOperation.getQName().getLocalPart());
						//Convert com.eviware.soapui.model.iface.Operation to com.eviware.soapui.impl.wsdl.WsdlOperation
						WsdlRequest request = wsdlOperation.addNewRequest( wsdlOperation.getName()+"SampleRequest" );
						// generate the request content from the schema - SOAPUI sample request
						request.setRequestContent( wsdlOperation.createRequest( true ) );
						//groom the request content to remove spaces
						String requestContent=request.getRequestContent();
						System.out.println("operationName: "+wsdlOperation.getName());
						System.out.println(requestContent);
						requestContent=requestContent.replaceAll(">\\s+<", "><");
						//Read the request content and modify to include JMeter parameters
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						InputSource src = new InputSource();
						src.setCharacterStream(new StringReader(requestContent));
						
						Document doc = builder.parse(src);
						NodeList requestNodeList= doc.getElementsByTagName("*");
						for(int i=0;i<requestNodeList.getLength();i++) {
							Node node= requestNodeList.item(i);
							System.out.println(node.getNodeName()+": "+node.getTextContent()+"BBB\n");
							if(node.getFirstChild() != null) {
							if(node.getTextContent().equals("?") && node.getFirstChild().getNodeName().contentEquals("#text")) {
							//							node.getLastChild().se
									String currentNodeName=node.getNodeName();
									String formattedNodeName=currentNodeName;
									if(currentNodeName.contains(":")) {
									formattedNodeName= currentNodeName.substring(currentNodeName.indexOf(":")+1);
									}
									doc.getElementsByTagName("*").item(i).setTextContent("${"+formattedNodeName+"}");
									System.out.println("BBBBBBB: The content of "+doc.getElementsByTagName("*").item(i).getNodeName()+" is changed from ? to "+"${"+formattedNodeName+"}");					
							}
							}
						}
						
						//Convert the modified version of request to String
						DOMSource domSource = new DOMSource(doc);
						StringWriter writer = new StringWriter();
						StreamResult result = new StreamResult(writer);
						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer transformer = tf.newTransformer();
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
						transformer.transform(domSource, result);
						String modifiedRequestContent=writer.toString();
						System.out.println("XML request in String format is: \n" +modifiedRequestContent);
						configFilelines.add(service.getQName().getLocalPart());
						configFilelines.add(endpoint.getAddress());
						configFilelines.add(wsdlOperation.getName());
						configFilelines.add(modifiedRequestContent);
	
						
						configFilelines.add("Operation"+(operationCounter)+"END");
					}
					configFilelines.add("Endpoint"+(endpointCounter)+"END");
				}
				configFilelines.add("Service"+(serviceCounter)+"END");
			}
			configFilelines.add("CONFIGEND");
			
			Files.write(file, configFilelines, Charset.forName("UTF-8"));
			return;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	
	
	
	//TODO execute smoke test using the config file, add the responses of smoke test to the config file
	/*
	 * Reads config file and executes a smoke run
	 * Fills the config file with responses received during the smoke run
	 * Logs any errors to the log file - timestamp based file
	 */
	private void createAndExecuteSmokeRun(List<CustomOperation> operations) {
				// JMeter Engine
				StandardJMeterEngine jmeter = new StandardJMeterEngine();

				JMeterUtils.setJMeterHome(JMETER_HOME);
				// JMeter initialization (properties, log levels, locale, etc)

				JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES_FILEPATH);
//		        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
				JMeterUtils.initLocale();

				// JMeter Test Plan, basic all u JOrphan HashTree
				HashTree testPlanTree = new HashTree();
				
				// Test Plan
				TestPlan testPlan = new TestPlan("SOAP WSDL");
				testPlan.setProperty("name", "SOAP WSDL");
				testPlan.setEnabled(true);
				testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
				testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
				testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
				
				testPlanTree.add(testPlan);
				
			
//				testPlanTree.add("csv",csv);
				
				int currentCount=1;
				
				for(CustomOperation operation: operations) {

					HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
					httpSampler.setName(operation.getOperaionName()+"HttpSampler");
					httpSampler.setDomain(operation.getEndPoint().getHost());
					httpSampler.setPort(operation.getEndPoint().getPort());
					httpSampler.setProtocol(operation.getEndPoint().getScheme());
					httpSampler.setPath(operation.getEndPoint().getPath());
					httpSampler.setMethod("POST");
					httpSampler.setProperty(HTTPSampler.POST_BODY_RAW, true);
					httpSampler.addNonEncodedArgument("", operation.getRequestBody(), "=");
					httpSampler.setProperty(TestElement.ENABLED, true);
					httpSampler.setResponseTimeout("20000");
					httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
					httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
				
					TransactionController transactionController= new TransactionController();
					transactionController.setEnabled(true);
					transactionController.setIncludeTimers(false);
					transactionController.setGenerateParentSample(false);
					transactionController.setName((++currentCount)+"_SOAPProject_"+operation.getOperaionName());
					transactionController.addTestElement(httpSampler);
					transactionController.setProperty(TestElement.TEST_CLASS,TransactionController.class.getName());
					transactionController.setProperty(TestElement.GUI_CLASS,TransactionControllerGui.class.getName());
					
					TestElement loopController = new LoopController();
					((LoopController) loopController).setLoops(10);//Smoke test and hence, 1 loop only
					loopController.addTestElement(transactionController);
					((LoopController) loopController).setFirst(true);
					loopController.setEnabled(true);
					loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
					loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
					((LoopController) loopController).initialize();
					
					ThreadGroup threadGroup = new ThreadGroup();
					threadGroup.setNumThreads(3);
					threadGroup.setRampUp(5);
					threadGroup.setName("Thread Group "+currentCount);
					threadGroup.setSamplerController(((LoopController) loopController));
					threadGroup.setEnabled(true);
					threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
					threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
					
					ConstantThroughputTimer constantThroughputTimer = new ConstantThroughputTimer();
					constantThroughputTimer.setEnabled(true);
					constantThroughputTimer.setName("Constant Throughput timer");
					constantThroughputTimer.setThroughput(10);
					constantThroughputTimer.setCalcMode(0);
					constantThroughputTimer.setProperty(ConstantThroughputTimer.TEST_CLASS, ConstantThroughputTimer.class.getName());
					constantThroughputTimer.setProperty(ConstantThroughputTimer.GUI_CLASS, TestBeanGUI.class.getName());
					
					
					HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
					HashTree transactionControllerHashTree = threadGroupHashTree.add(threadGroup,transactionController);
					threadGroupHashTree.add(threadGroup,constantThroughputTimer);
					HashTree httpSamplerHashTree=transactionControllerHashTree.add(transactionController,httpSampler);
					
				}
				
					
				CSVDataSet csv = new CSVDataSet();
				csv.setName("CSV Data Set Config");
				csv.setEnabled(true);
				csv.setFilename(WORKING_DIRECTORY+"test.csv");
				csv.setProperty("filename", WORKING_DIRECTORY+"test.csv");
				csv.setQuotedData(false);
				csv.setDelimiter(",");
				csv.setRecycle(true);
				csv.setStopThread(false);
				csv.setShareMode("shareMode.all");

				csv.setProperty(CSVDataSet.TEST_CLASS, CSVDataSet.class.getName());
				csv.setProperty(CSVDataSet.GUI_CLASS, TestBeanGUI.class.getName());
				
				testPlanTree.add(testPlan,csv);
				
				
				SampleSaveConfiguration saveConfig= new SampleSaveConfiguration();
				saveConfig.setTime(true);
				saveConfig.setLatency(true);
				saveConfig.setTimestamp(true);
				saveConfig.setSuccess(true);
				saveConfig.setLabel(true);
				saveConfig.setCode(true);
				saveConfig.setMessage(true);
				saveConfig.setThreadName(true);
				saveConfig.setDataType(true);
				saveConfig.setEncoding(true);
				saveConfig.setAssertions(true);
				saveConfig.setSubresults(true);
				saveConfig.setResponseData(true);
				saveConfig.setSamplerData(true);
				saveConfig.setAsXml(false);
				saveConfig.setFieldNames(true);
				saveConfig.setResponseHeaders(true);
				saveConfig.setRequestHeaders(true);
				saveConfig.assertionsResultsToSave();
				saveConfig.setBytes(true);
				saveConfig.setUrl(true);
				saveConfig.setFileName(true);
				saveConfig.setHostname(true);
				saveConfig.setThreadCounts(true);
				saveConfig.setSampleCount(true);
				saveConfig.setIdleTime(true);
				saveConfig.setConnectTime(true);
				
				ResultCollector resultCollector=new ResultCollector();
				resultCollector.setEnabled(true);
				resultCollector.setName("View Results tree");
				resultCollector.setErrorLogging(true);
				resultCollector.setProperty(ResultCollector.GUI_CLASS, ViewResultsFullVisualizer.class.getName());
				resultCollector.setProperty(ResultCollector.TEST_CLASS, ResultCollector.class.getName());
				resultCollector.setSaveConfig(saveConfig);
				resultCollector.setProperty("filename",WORKING_DIRECTORY+"smokeResults.csv");
				
				testPlanTree.add(testPlan,resultCollector);
				
				
				
				// save generated test plan to JMeter's .jmx file format
				try {
					SaveService.saveTree(testPlanTree, new FileOutputStream("C:\\jmeter\\smokeTest.jmx"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Run Test Plan
				jmeter.configure(testPlanTree);
				jmeter.run();
				
				while(jmeter.isActive()) {
					
				}
				try {
				 Runtime rt = Runtime.getRuntime();
		            Process pr = rt.exec("C:\\jmeter\\apache-jmeter-5.0\\bin\\jmeter.bat -g C:\\uttam\\smokeResults.csv -o c:\\uttam\\dashboard");  
				}catch(Exception e) {
					
				}
	}
	
	
	/*
	 * CONFIGSTART
	 * Service1START
	 * <ServiceName>
	 * Endpoint1START
	 * Operation1START
	 * Operation1END
	 * ....next operations
	 * Endpoint1END
	 * ....next Endpoints
	 * Service1END
	 * ....next Services
	 * CONFIGEND
	 */
	private List<CustomOperation> readConfigFile(Path path) {
		List<CustomOperation> operations= new ArrayList<>();
		List<String> configFileLines= new ArrayList();
		try {
			configFileLines=Files.readAllLines(path, StandardCharsets.UTF_8);
			
			System.out.println(configFileLines.size());
			System.out.println(configFileLines.get(0));
			System.out.println(configFileLines.get(configFileLines.size()-1));
			System.out.println(configFileLines.get(configFileLines.size()-2).substring(7,8));
			if(configFileLines.get(0).equals("CONFIGSTART") && 
					configFileLines.get(configFileLines.size()-1).equals("CONFIGEND")) {
				int configFileLinesCount=configFileLines.size();
				int serviceCount=Integer.parseInt(configFileLines.get(configFileLinesCount-2).substring(7,8));
				
				int operationCount=Integer.parseInt(configFileLines.get(configFileLinesCount-4).substring(9	, 10));
						
				for(int i=0;i<operationCount;i++) {
					
					CustomOperation operation= new CustomOperation();
					
					
					
					int operationStartIndex=configFileLines.indexOf("Operation"+(i+1)+"START");//from
					int operationEndIndex=configFileLines.indexOf("Operation"+(i+1)+"END");//to
					System.out.println(configFileLines.subList(operationStartIndex, operationEndIndex));
					
					operation.setServiceName(configFileLines.get(operationStartIndex+1));//TODO see how to find the service Name or if needed print the service name with all the operations similar to the actual Operation POJO
					operation.setEndPoint(new URI(configFileLines.get(operationStartIndex+2)));
					operation.setOperaionName(configFileLines.get(operationStartIndex+3));
					
					String requestBody= new String();
					
					
					for(int j=operationStartIndex;j<operationEndIndex-5;j++) {
						requestBody=requestBody+configFileLines.get(j+4);
					}
					
					operation.setRequestBody(requestBody);
					operations.add(operation);
					
				}
				
				
				
				
				System.out.println(serviceCount);
				
			}else {
				System.out.println("Config file empty or corrupted");
			}
			
			return operations;
			}catch(Exception e) {
			return null;
		}
		
		
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

class CustomOperation {
	private String serviceName;
	private URI endPoint;
	private String operaionName;
	private String requestBody;
	
	List<Operation> operations;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public URI getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(URI endPoint) {
		this.endPoint = endPoint;
	}
	public String getOperaionName() {
		return operaionName;
	}
	public void setOperaionName(String operaionName) {
		this.operaionName = operaionName;
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

}