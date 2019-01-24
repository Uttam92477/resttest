package com.uttamjmeter.resttest;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.easywsdl.schema.api.XmlException;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Binding;
import org.ow2.easywsdl.wsdl.api.BindingOperation;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.InterfaceType;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.springframework.util.SystemPropertyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;

public class AppForTrails {

	public static void main(String[] args) {
		Path path= Paths.get("C:\\uttam\\config.txt");
		List<String> configFileLines= new ArrayList<>();
		try {
		configFileLines=Files.readAllLines(path, StandardCharsets.UTF_8);
		
		System.out.println(configFileLines.size());
		System.out.println(configFileLines.get(0));
		System.out.println(configFileLines.get(configFileLines.size()-1));
		System.out.println(configFileLines.get(configFileLines.size()-2).substring(7,8));
		List<CustomOperation> operations= new ArrayList<>();
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
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}

