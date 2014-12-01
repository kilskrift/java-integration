package se.sveaekonomi.webpay.integration.hosted.hostedadmin;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import se.sveaekonomi.webpay.integration.config.SveaConfig;
import se.sveaekonomi.webpay.integration.hosted.helper.PaymentForm;
import se.sveaekonomi.webpay.integration.order.create.CreateOrderBuilder;
import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderRowsBuilder;
import se.sveaekonomi.webpay.integration.order.handle.QueryOrderBuilder;
import se.sveaekonomi.webpay.integration.order.row.NumberedOrderRowBuilder;
import se.sveaekonomi.webpay.integration.response.hosted.HostedPaymentResponse;
import se.sveaekonomi.webpay.integration.response.hosted.hostedadmin.AnnulTransactionResponse;
import se.sveaekonomi.webpay.integration.response.hosted.hostedadmin.QueryTransactionResponse;
import se.sveaekonomi.webpay.integration.response.webservice.CloseOrderResponse;
import se.sveaekonomi.webpay.integration.response.webservice.CreateOrderResponse;
import se.sveaekonomi.webpay.integration.response.webservice.DeliverOrderResponse;
import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTMETHOD;
import se.sveaekonomi.webpay.integration.util.test.TestingTool;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * contains end-to-end integration tests of WebPayAdmin entrypoints
 * used as acceptance test for package functionality, performs requests to Svea services (test environment)
 * 
 * see unit tests for validation of required entrypoint methods for the various payment methods/customers/countries
 * 
 * @author Kristian Grossman-Madsen
 */
public class QueryTransactionTest {    
	
    /// WebPayAdmin.queryOrder() ---------------------------------------------------------------------------------------
    // card
    @Test
    public void test_queryOrder_queryCardOrder_single_order_row() {
        // use known order to test all returned values     
        long createdOrderId = 587673L;  

//    	// create an order using defaults
//    	HostedPaymentResponse order = (HostedPaymentResponse)TestingTool.createCardTestOrder("test_cancelOrder_cancelCardOrder");
//        assertTrue(order.isOrderAccepted());
//        
        // query order
        QueryOrderBuilder queryOrderBuilder = new QueryOrderBuilder( SveaConfig.getDefaultConfig() )
        .setTransactionId( Long.toString(createdOrderId) )
//        .setTransactionId( order.getTransactionId() )
            .setCountryCode( COUNTRYCODE.SE )
        ;                
        QueryTransactionResponse response = queryOrderBuilder.queryCardOrder().doRequest();         
        
        assertTrue( response.isOrderAccepted() );     
        
        // expected xml response:
		//        <?xml version="1.0" encoding="UTF-8"?><response>
		//        <transaction id="587673">
		//          <customerrefno>test_cancelOrder_cancelCardOrder1413208130564</customerrefno>
		//          <merchantid>1130</merchantid>
		//          <status>SUCCESS</status>
		//          <amount>12500</amount>
		//          <currency>SEK</currency>
		//          <vat>2500</vat>
		//          <capturedamount>12500</capturedamount>
		//          <authorizedamount>12500</authorizedamount>
		//          <created>2014-10-13 15:48:56.487</created>
		//          <creditstatus>CREDNONE</creditstatus>
		//          <creditedamount>0</creditedamount>
		//          <merchantresponsecode>0</merchantresponsecode>
		//          <paymentmethod>KORTCERT</paymentmethod>
		//          <callbackurl/>
		//          <capturedate>2014-10-14 00:15:10.287</capturedate>
		//          <subscriptionid/>
		//          <subscriptiontype/>
		//          <customer id="13662">
		//            <firstname>Tess</firstname>
		//            <lastname>Persson</lastname>
		//            <initials>SB</initials>
		//            <email>test@svea.com</email>
		//            <ssn>194605092222</ssn>
		//            <address>Testgatan</address>
		//            <address2>c/o Eriksson, Erik</address2>
		//            <city>Stan</city>
		//            <country>SE</country>
		//            <zip>99999</zip>
		//            <phone>0811111111</phone>
		//            <vatnumber/>
		//            <housenumber>1</housenumber>
		//            <companyname/>
		//            <fullname/>
		//          </customer>
		//          <orderrows>
		//            <row>
		//              <id>55950</id>
		//              <name>orderrow 1</name>
		//              <amount>12500</amount>
		//              <vat>2500</vat>
		//              <description>description 1</description>
		//              <quantity>1.0</quantity>
		//              <sku/>
		//              <unit/>
		//            </row>
		//          </orderrows>
		//        </transaction>
		//        <statuscode>0</statuscode>
		//      </response>
               
		assertEquals("587673", response.getTransactionId() );
		assertEquals("test_cancelOrder_cancelCardOrder1413208130564", response.getClientOrderNumber() );
		assertEquals("1130", response.getMerchantId());
		assertEquals("SUCCESS", response.getStatus());
		assertEquals("12500", response.getAmount());
		assertEquals("SEK", response.getCurrency());
		assertEquals("2500", response.getVat());
		assertEquals("12500", response.getCapturedAmount());
		assertEquals("12500", response.getAuthorizedAmount());									
		assertEquals("2014-10-13 15:48:56.487", response.getCreated());					
		assertEquals("CREDNONE", response.getCreditstatus());
		assertEquals("0", response.getCreditedAmount());						// TODO test
		assertEquals("0", response.getMerchantResponseCode());					
		assertEquals("KORTCERT", response.getPaymentMethod());
		assertEquals(null, response.getCallbackUrl());						// TODO test
		assertEquals("2014-10-14 00:15:10.287", response.getCaptureDate());
		assertEquals(null, response.getSubscriptionId());					// TODO test
		assertEquals(null, response.getSubscriptionType());
		assertEquals(null, response.getCardType());								
		assertEquals(null, response.getMaskedCardNumber());						
		assertEquals(null, response.getEci());										
		assertEquals(null, response.getMdstatus());
		assertEquals(null, response.getExpiryYear());
		assertEquals(null, response.getExpiryMonth());
		assertEquals(null, response.getChname());
		assertEquals(null, response.getAuthCode());			        


        assertEquals( 1, response.getNumberedOrderRows().get(0).getRowNumber() );
        assertEquals( Double.valueOf(1.00), response.getNumberedOrderRows().get(0).getQuantity() ); // first Double.valueOf disambiguates double/Double
        assertEquals( Double.valueOf(100.00), response.getNumberedOrderRows().get(0).getAmountExVat() );
        assertEquals( Double.valueOf(25.00), response.getNumberedOrderRows().get(0).getVatPercent() ); 
        assertEquals( "orderrow 1", response.getNumberedOrderRows().get(0).getName() );
        assertEquals( "description 1", response.getNumberedOrderRows().get(0).getDescription() );        	
    }

    @Test
    public void test_queryOrder_queryCardOrder_multiple_order_row() {
        // use known order to test all returned values     
        long createdOrderId = 587679L;  
        
        QueryOrderBuilder queryOrderBuilder = new QueryOrderBuilder( SveaConfig.getDefaultConfig() )
            .setOrderId( createdOrderId )
            .setCountryCode( COUNTRYCODE.SE )
        ;                
        QueryTransactionResponse response = queryOrderBuilder.queryCardOrder().doRequest();         
        
        assertTrue( response.isOrderAccepted() );     
           
		assertEquals( 1, response.getNumberedOrderRows().get(0).getRowNumber() );
        assertEquals( Double.valueOf(1.00), response.getNumberedOrderRows().get(0).getQuantity() ); // first Double.valueOf disambiguates double/Double
        assertEquals( Double.valueOf(100.00), response.getNumberedOrderRows().get(0).getAmountExVat() );
        assertEquals( Double.valueOf(25.00), response.getNumberedOrderRows().get(0).getVatPercent() ); 
        assertEquals( "orderrow 1", response.getNumberedOrderRows().get(0).getName() );
        assertEquals( "description 1", response.getNumberedOrderRows().get(0).getDescription() );        	

		assertEquals( 2, response.getNumberedOrderRows().get(1).getRowNumber() );
        assertEquals( Double.valueOf(1.00), response.getNumberedOrderRows().get(1).getQuantity() ); // first Double.valueOf disambiguates double/Double
        assertEquals( Double.valueOf(100.00), response.getNumberedOrderRows().get(1).getAmountExVat() );
        assertEquals( Double.valueOf(25.00), response.getNumberedOrderRows().get(1).getVatPercent() ); 
        assertEquals( "orderrow 2", response.getNumberedOrderRows().get(1).getName() );
        assertEquals( "description 2", response.getNumberedOrderRows().get(1).getDescription() );  
    
	}
}
