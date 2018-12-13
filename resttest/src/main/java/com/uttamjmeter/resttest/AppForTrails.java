package com.uttamjmeter.resttest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;

public class AppForTrails {

	public static void main(String[] args) {

		try {
			// Read a WSDL 1.1 or 2.0
			WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
			Description desc = reader.read(new URL("file:///C:/jmeter/sample.wsdl"));
			// Endpoints take place in services.
			// Select a service
			Service service = desc.getServices().get(0);
			List<Endpoint> endpoints = service.getEndpoints();
			// Gets address of first endpoint
			System.out.println(endpoints.get(0).getAddress());
			// Gets http method
			System.out.println(endpoints.get(0).getBinding().getBindingOperations().get(0).getHttpMethod());
			// Gets input type
			System.out.println(endpoints.get(0).getBinding().getInterface().getOperations().get(0).getInput()
					.getElement().getType().getQName().getLocalPart());

		} catch (WSDLException | IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
}

class HTTPRequest {
	private String address;
	private String serviceDocumentaion;
	private String httpMethod;

}
