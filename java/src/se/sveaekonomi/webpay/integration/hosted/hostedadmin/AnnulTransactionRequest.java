package se.sveaekonomi.webpay.integration.hosted.hostedadmin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.xml.bind.ValidationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;




import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
import se.sveaekonomi.webpay.integration.exception.SveaWebPayException;
import se.sveaekonomi.webpay.integration.response.hosted.hostedadmin.AnnulTransactionResponse;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;
import se.sveaekonomi.webpay.integration.util.request.GetRequestProperties;
import se.sveaekonomi.webpay.integration.util.security.Base64Util;
import se.sveaekonomi.webpay.integration.util.security.HashUtil;
import se.sveaekonomi.webpay.integration.util.security.HashUtil.HASHALGORITHM;

/**
 * AnnulTransaction is used to cancel (annul) a card transaction. The
 * transaction must have status AUTHORIZED or CONFIRMED at Svea. After a
 * successful request the transaction will get the status ANNULLED.
 * 
 * @author Kristian Grossman-Madsen
 */
public class AnnulTransactionRequest extends HostedAdminRequest<AnnulTransactionRequest> {

	String transactionId;

	public String getTransactionId() {
		return transactionId;
	}

	public AnnulTransactionRequest setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public AnnulTransactionRequest(ConfigurationProvider config) {
		super(config, "annul");
	}

	/**
	 * should return the request message xml for the method in question
	 */
	public String getRequestMessageXml( ConfigurationProvider config ) {

		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");

			xmlw.writeStartDocument("UTF-8", "1.0");
			xmlw.writeComment( GetRequestProperties.getLibraryAndPlatformPropertiesAsJson(config) );
				xmlw.writeStartElement("annul");
					xmlw.writeStartElement("transactionid");
						xmlw.writeCharacters(this.transactionId);
					xmlw.writeEndElement();
				xmlw.writeEndElement();
			xmlw.writeEndDocument();
			xmlw.close();
		} catch (XMLStreamException e) {
			throw new SveaWebPayException("Error when building XML", e);
		}

		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SveaWebPayException("Unsupported encoding UTF-8", e);
		}
	}	
	    
	/**
	 * should return string indicating any missing order builder setter methods on validation failure, or empty string
	 */
	public String validateRequest() {
        String errors = "";
        if (this.getCountryCode() == null) {
            errors += "MISSING VALUE - CountryCode is required, use setCountryCode(...).\n";
        }
        
        if (this.getTransactionId() == null) {
            errors += "MISSING VALUE - OrderId is required, use setOrderId().\n";
    	}
        return errors;    
    }
	
	/**
	 * returns the request fields to post to service
	 */
	public Hashtable<String,String> prepareRequest() {

    	// validate request and throw exception if validation fails
        String errors = validateRequest();
        
        if (!errors.equals("")) {
        	System.out.println(errors);
            throw new SveaWebPayException("Validation failed", new ValidationException(errors));
        }
        
        // build inspectable request object and return
		Hashtable<String,String> requestFields = new Hashtable<>();

		String merchantId = this.config.getMerchantId(PAYMENTTYPE.HOSTED, this.getCountryCode());
		String secretWord = this.config.getSecretWord(PAYMENTTYPE.HOSTED, this.getCountryCode());		
		
    	String xmlMessage = getRequestMessageXml( this.config );
    	String xmlMessageBase64 = Base64Util.encodeBase64String(xmlMessage);
    	String macSha512 =  HashUtil.createHash(xmlMessageBase64 + secretWord, HASHALGORITHM.SHA_512);			

    	requestFields.put("message", xmlMessageBase64);
    	requestFields.put("mac", macSha512);
    	requestFields.put("merchantid", merchantId);
    	
		return requestFields;
	}
    
	/**
	 * validate, prepare and do request
	 * @return AnnulTransactionResponse
	 * @throws SveaWebPayException
	 */
	public AnnulTransactionResponse doRequest() throws SveaWebPayException {

		try {
			// prepare request fields
	    	Hashtable<String, String> requestFields = this.prepareRequest();

	    	// send request 
	    	String xmlResponse = sendHostedAdminRequest(requestFields);
	
	    	// parse response	
			return new AnnulTransactionResponse( getResponseMessageFromXml(xmlResponse), getResponseMacFromXml(xmlResponse), this.config.getSecretWord(PAYMENTTYPE.HOSTED, this.getCountryCode()));
			
	    } catch (IllegalStateException ex) {
	        throw new SveaWebPayException("IllegalStateException", ex);
	    } 
		catch (IOException ex) {
			//System.out.println(ex.toString());
			//System.out.println(((HttpResponseException)ex).getStatusCode());
	        throw new SveaWebPayException("IOException", ex);
	    }		
	}
	
}
