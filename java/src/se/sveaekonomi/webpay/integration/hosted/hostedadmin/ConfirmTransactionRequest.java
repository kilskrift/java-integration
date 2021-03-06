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
import se.sveaekonomi.webpay.integration.response.hosted.hostedadmin.ConfirmTransactionResponse;
import se.sveaekonomi.webpay.integration.response.hosted.hostedadmin.LowerTransactionResponse;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;
import se.sveaekonomi.webpay.integration.util.request.GetRequestProperties;
import se.sveaekonomi.webpay.integration.util.security.Base64Util;
import se.sveaekonomi.webpay.integration.util.security.HashUtil;
import se.sveaekonomi.webpay.integration.util.security.HashUtil.HASHALGORITHM;

public class ConfirmTransactionRequest extends HostedAdminRequest<ConfirmTransactionRequest> {
	
    /** Required. */
	public String transactionId;
	
    /** Required. Use ISO-8601 extended date format (YYYY-MM-DD) */
	public String captureDate;

    /** Optional. Iff set, will do a loweramount request before the confirm request. Use minor currency (i.e. 1 SEK => 100 in minor currency) */
	public Integer amountToLower;
	
	public String getCaptureDate() {
		return captureDate;
	}

	public ConfirmTransactionRequest setCaptureDate(String captureDate) {
		this.captureDate = captureDate;
		return this;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public ConfirmTransactionRequest setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public ConfirmTransactionRequest setAlsoDoLowerAmount(Integer amountToLower) {
		this.amountToLower = amountToLower;
		return this;
	}
	
	public ConfirmTransactionRequest( ConfigurationProvider config ) {
		super(config, "confirm");	
		this.amountToLower = null;
	}
	
	/**
	 * validates that all required attributes needed for the request are present in the builder object
	 * @return indicating which methods are missing, or empty String if no problems found
	 */
	public String validateRequest() {
		String errors = "";		
		errors += validateRequestId();
		errors += validateCountryCode();
		errors += validateCaptureDate();
		return errors;
	}
	
    private String validateRequestId() {
    	return (this.getTransactionId() == null) ? "MISSING VALUE - setOrderId is required.\n" : "";
    }
   
    private String validateCountryCode() {
        return (this.getCountryCode() == null) ? "MISSING VALUE - CountryCode is required, use setCountryCode(...).\n" : "";
    }

    private String validateCaptureDate() {
        return (this.getCaptureDate() == null) ? "MISSING VALUE - CaptureDate is required, use setCaptureDate(...).\n" : "";
    }
    
    /**
	 * returns xml for hosted webservice "confirm" request
	 */
	public String getRequestMessageXml( ConfigurationProvider config) {

		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");

			xmlw.writeStartDocument("UTF-8", "1.0");
				xmlw.writeComment( GetRequestProperties.getLibraryAndPlatformPropertiesAsJson(config) );
				xmlw.writeStartElement("confirm");
					xmlw.writeStartElement("transactionid");
						xmlw.writeCharacters( this.getTransactionId() );
					xmlw.writeEndElement();
					xmlw.writeStartElement("capturedate");
						xmlw.writeCharacters( this.getCaptureDate() );
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
	 * @return ConfirmTransactionResponse
	 * @throws SveaWebPayException
	 */
	public ConfirmTransactionResponse doRequest() throws SveaWebPayException {

		// iff amountToLower is set, first perform a loweramount request
		if( this.amountToLower != null ) {
			// loweramount uses already validated fields
			LowerTransactionRequest lowerRequest = new LowerTransactionRequest(this.config)
				.setTransactionId(this.getTransactionId())
				.setCountryCode(this.getCountryCode())
				.setAmountToLower(this.amountToLower)
			;
			LowerTransactionResponse lowerResponse = lowerRequest.doRequest();
					
			// if there were an error other than 305 (i.e. assuming that we tried to lower amount by 0), return a dummy ConfirmTransactionResponse w/errormessage
			if( (lowerResponse.isOrderAccepted() == false) && (lowerResponse.getResultCode().startsWith("305") == false) ) {
				ConfirmTransactionResponse dummyConfirmResponse = new ConfirmTransactionResponse( null, null ); // new empty response
				dummyConfirmResponse.setOrderAccepted(false);
				dummyConfirmResponse.setResultCode( "100" );  //INTERNAL_ERROR
				dummyConfirmResponse.setErrorMessage( 
					"IntegrationPackage: LowerAmount request with flag alsoDoConfirm failed:" +
					lowerResponse.getResultCode() + " " + lowerResponse.getErrorMessage() 
				);
		              
				return dummyConfirmResponse;
			}
		}
		
		try {
			// prepare request fields
	    	Hashtable<String, String> requestFields = this.prepareRequest();

	    	// send request 
	    	String xmlResponse = sendHostedAdminRequest(requestFields);
	
	    	// parse response	
			return new ConfirmTransactionResponse( getResponseMessageFromXml(xmlResponse), getResponseMacFromXml(xmlResponse),this.config.getSecretWord(PAYMENTTYPE.HOSTED, this.getCountryCode()) );
			
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
