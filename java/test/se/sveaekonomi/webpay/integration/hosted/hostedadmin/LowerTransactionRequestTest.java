package se.sveaekonomi.webpay.integration.hosted.hostedadmin;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import se.sveaekonomi.webpay.integration.config.SveaConfig;
import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderRowsBuilder;
import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;
import se.sveaekonomi.webpay.integration.util.security.Base64Util;
import se.sveaekonomi.webpay.integration.util.security.HashUtil;
import se.sveaekonomi.webpay.integration.util.security.HashUtil.HASHALGORITHM;

public class LowerTransactionRequestTest extends TestCase {

	private DeliverOrderRowsBuilder order;
	private LowerTransactionRequest request;
	
	@Before
	public void setUp() {
    	request = new LowerTransactionRequest(SveaConfig.getDefaultConfig()); 
    	request.setCountryCode(COUNTRYCODE.SE);
    	request.setTransactionId( "123456" );
    	request.setAmountToLower(100);
	}
	
    @Test
    public void test_LowerTransactionRequest_class_exists() {    	   	        
        assertThat( request, instanceOf(LowerTransactionRequest.class) );
        assertThat( request, instanceOf(HostedAdminRequest.class) );
    }    
    
    @Test 
    public void test_getRequestMessageXml() {    	
    	
    	String expectedXmlMessage = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    			"<!--Message generated by Integration package Java-->" +
    			"<loweramount>" +
    				"<transactionid>123456</transactionid>" +
    				"<amounttolower>100</amounttolower>" +
				"</loweramount>"
		;
    	
    	assertEquals( expectedXmlMessage, request.getRequestMessageXml() );    
    }
    
    @Test
    public void test_prepareRequest() {

		String merchantId = this.order.getConfig().getMerchantId(PAYMENTTYPE.HOSTED, request.getCountryCode());
		String secretWord = this.order.getConfig().getSecretWord(PAYMENTTYPE.HOSTED, request.getCountryCode());    	
    	
		String expectedXmlMessage = request.getRequestMessageXml();
    	String expectedXmlMessageBase64 = Base64Util.encodeBase64String(expectedXmlMessage);
    	String expectedMacSha512 =  HashUtil.createHash(expectedXmlMessageBase64 + secretWord, HASHALGORITHM.SHA_512);
    	
    	Hashtable<String, String> requestFields = this.request.prepareRequest();
    	assertEquals( expectedXmlMessageBase64, requestFields.get("message") );
    	assertEquals( expectedMacSha512, requestFields.get("mac") );
    	assertEquals( merchantId, requestFields.get("merchantid") );
    	// TODO replace with fixed request string!
    }
    
}
