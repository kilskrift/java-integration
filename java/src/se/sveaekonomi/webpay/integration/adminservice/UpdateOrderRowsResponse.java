package se.sveaekonomi.webpay.integration.adminservice;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class UpdateOrderRowsResponse extends AdminServiceResponse {


	public UpdateOrderRowsResponse(SOAPMessage soapResponse) throws SOAPException {
		// set common response attributes
		super(soapResponse.getSOAPPart().getEnvelope().getBody().getElementsByTagName("*"));

    	if( this.isOrderAccepted() ) {
    		//nothing to be done for UpdateOrderRows
    	}
	}
}
