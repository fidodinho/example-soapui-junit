package soapui.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.support.SoapUIException;

public class SOAPUI {

	@SuppressWarnings("unused")
	private static WsdlRequest createWsdlRequest(String url, String operationName, Map<String, String> paramList) throws XmlException, IOException, SoapUIException, SubmitException {
		// create new project
		WsdlProject project = new WsdlProject();
		
		project.setPropertyValue("SecurityType", "HTTPBasic");
		project.setPropertyValue("AuthUserName", "User");
		project.setPropertyValue("AuthPassword", "Pass");


		WsdlInterface[] wsdlInterfaces = WsdlInterfaceFactory.importWsdl(project, url, false);

		WsdlInterface service = (WsdlInterface) project.getInterfaceAt(0);
		System.out.println("SOAP Ver: " + service.getSoapVersion());
		System.out.println("SOAP BinName: " + service.getBindingName());
		Operation[] op = service.getAllOperations();
		for (Operation operation : op) {
			System.out.println(operation.getName());
		}

		WsdlOperation wsdlOperation = service.getOperationByName(operationName);

		// create a new empty request for that operation
		WsdlRequest wsdlRequest = wsdlOperation.addNewRequest("Basic Request");
		String requestContent = wsdlOperation.createRequest(true);
		wsdlRequest.setRequestContent(mapInputParams(requestContent, paramList));

		WsdlSubmitContext wsdlSubmitContext = new WsdlSubmitContext(wsdlRequest);
		WsdlSubmit<WsdlRequest> submit = (WsdlSubmit<WsdlRequest>) wsdlRequest.submit(wsdlSubmitContext, false);
		System.out.println("Status: " + submit.getStatus());
		System.out.println("Respone: " + submit.getResponse().getContentAsString());

		return wsdlRequest;
	}

	/**
	 * Map REST input params to the XML SOAP request
	 * 
	 * @param xmlRequest
	 *            The XML SOAP request
	 * @param params
	 *            The params to map
	 * @return A mapped XML request for SOAP
	 */
	private static String mapInputParams(String xmlRequest, Map<String, String> params) {
		// For each param key, replace the '?' in the xml request by the param
		// value
		for (String paramKey : params.keySet()) {
			// TODO make it works with multiple values and not only with the
			// first param
			xmlRequest = xmlRequest.replace(paramKey + ">?", paramKey + ">" + params.get(paramKey));
		}
		return xmlRequest;
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put("FromCurrency", "USD");
		paramList.put("ToCurrency", "VND");
		
		String url = "http://www.webservicex.com/CurrencyConvertor.asmx?wsdl";
		SOAPUI.createWsdlRequest(url, "ConversionRate", paramList);
	}
}
