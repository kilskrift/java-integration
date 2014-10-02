//package se.sveaekonomi.webpay.integration.hosted.hostedadmin;
//
//import static org.hamcrest.CoreMatchers.instanceOf;
//import static org.junit.Assert.assertThat;
//
//import java.io.IOException;
//
//import junit.framework.TestCase;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
//import se.sveaekonomi.webpay.integration.config.SveaConfig;
//import se.sveaekonomi.webpay.integration.response.hosted.hostedadminresponse.CreditTransactionResponse;
//import se.sveaekonomi.webpay.integration.response.hosted.hostedadminresponse.HostedAdminResponse;
//import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
//
//public class CreditTransactionRequestTest extends TestCase {
//
//	private ConfigurationProvider config;
//	private CreditTransactionRequest request;
//	
//	@Before
//	public void setUp() {
//		config = SveaConfig.getDefaultConfig();
//		request = new CreditTransactionRequest(config); 
//		request.setCountryCode(COUNTRYCODE.SE);
//	}
//	
//    @Test
//    public void test_CreditTransactionRequest_class_exists() {    	   	        
//        assertThat( request, instanceOf(CreditTransactionRequest.class) );
//        assertThat( request, instanceOf(HostedAdminRequest.class) );
//    }    
//    
//    @Test 
//    public void test_getRequestMessageXml() {    	
//    	this.request.setTransactionId( "123456" );    	
//    	this.request.setCreditAmount(10);
//    	
//    	String expectedXmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--Message generated by Integration package Java--><credit><transactionid>123456</transactionid><amounttocredit>10</amounttocredit></credit>";
//    	
//    	assertEquals( expectedXmlMessage, request.getRequestMessageXml() );    
//    }
//        
//    @Test
//    public void test_doRequest_returns_CreditTransactionResponse_failure() {
//    	this.request.setTransactionId( "987654" );
//    	this.request.setCreditAmount(10);
//    	
//		CreditTransactionResponse response = null;
//		
//		try {
//			response = this.request.doRequest();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		//System.out.println( response.getRawResponse() );		
//    	assertThat( response, instanceOf(CreditTransactionResponse.class) );
//        assertThat( response, instanceOf(HostedAdminResponse.class) );
//          
//        // if we receive an error from the service, the integration test passes
//        assertFalse( response.isOrderAccepted() );
//    	assertEquals( response.getResultCode(), "128 (NO_SUCH_TRANS)" );      	
//    }
//    
//    @Test
//    public void manual_test_doRequest_returns_CreditTransactionResponse_success() {
//    	this.request.setTransactionId( "584556" );
//    	this.request.setCreditAmount(10);
//    	
//    	CreditTransactionResponse response = null;
//    	
//    	try {
//			response = this.request.doRequest();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//    	assertThat( response, instanceOf(CreditTransactionResponse.class) );
//        assertThat( response, instanceOf(HostedAdminResponse.class) );
//          
//        // if we receive an error from the service, the integration test passes
//        assertTrue( response.isOrderAccepted() );
//        assertEquals( response.getTransactionId(), "584556" );
//    	assertEquals( response.getCustomerRefNo(), "test_1405936409912" );      	
//	}        
//
////    <?xml version="1.0" encoding="UTF-8"?><!--Message generated by Integration package Java--><confirm><transactionid>584556</transactionid>
////    <capturedate>2014-07-21</capturedate></confirm>
////
////    <?xml version="1.0" encoding="UTF-8"?><response>
////    <transaction id="584556">
////      <customerrefno>test_1405936409912</customerrefno>
////    </transaction>
////    <statuscode>0</statuscode>
////  </response>
//
//}
