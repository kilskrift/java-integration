package se.sveaekonomi.webpay.integration.hosted.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.sveaekonomi.webpay.integration.WebPay;
import se.sveaekonomi.webpay.integration.hosted.helper.PaymentForm;
import se.sveaekonomi.webpay.integration.order.row.Item;
import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
import se.sveaekonomi.webpay.integration.util.constant.CURRENCY;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTMETHOD;
import se.sveaekonomi.webpay.integration.util.security.Base64Util;
import se.sveaekonomi.webpay.integration.util.test.TestingTool;

public class PaymentMethodTest {
    
    @Test
    public void testPayPagePaymentWithSetPaymentMethod() throws Exception {
        PaymentForm form = WebPay.createOrder()
        .addOrderRow(TestingTool.createOrderRow())
        .addDiscount(Item.relativeDiscount()
            .setDiscountId("1")
            .setDiscountPercent(50)
            .setUnit("st")
            .setName("Relative")
            .setDescription("RelativeDiscount"))
        .addCustomerDetails(Item.individualCustomer()
             .setNationalIdNumber("194605092222"))
        
            .setCountryCode(COUNTRYCODE.SE)
            .setClientOrderNumber("33")
            .setOrderDate("2012-12-12")
            .setCurrency(CURRENCY.SEK)          
            .usePaymentMethod(PAYMENTMETHOD.KORTCERT)
            	.setReturnUrl("http://myurl.se")
            	.getPaymentForm();
              
        String xml = form.getXmlMessage();
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--Message generated by Integration package Java--><payment><paymentmethod>KORTCERT</paymentmethod><customerrefno>33</customerrefno><returnurl>http://myurl.se</returnurl><currency>SEK</currency><amount>12500</amount><lang>en</lang><customer><ssn>194605092222</ssn><country>SE</country></customer><vat>2500</vat><orderrows><row><sku>1</sku><name>Prod</name><description>Specification</description><amount>12500</amount><vat>2500</vat><quantity>2</quantity><unit>st</unit></row><row><sku>1</sku><name>Relative</name><description>RelativeDiscount</description><amount>-12500</amount><vat>-2500</vat><quantity>1</quantity><unit>st</unit></row></orderrows><excludepaymentmethods></excludepaymentmethods><iscompany>false</iscompany><addinvoicefee>false</addinvoicefee></payment>";
        assertEquals(expectedXML, xml);
        
        String base64Payment = form.getXmlMessageBase64();
        String html = Base64Util.decodeBase64String(base64Payment);
        String amount = html.substring(html.indexOf("<paymentmethod>") + 15, html.indexOf("</paymentmethod>"));
        assertEquals("KORTCERT", amount);
    }
    
    @Test
    public void testPayPagePaymentWithSetPaymentMethodNL() throws Exception {
        
    	PaymentForm form = WebPay.createOrder()
        .addOrderRow(TestingTool.createOrderRow())
        .addDiscount(Item.relativeDiscount()
            .setDiscountId("1")
            .setDiscountPercent(50)
            .setUnit("st")
            .setName("Relative")
            .setDescription("RelativeDiscount"))
        .addCustomerDetails(Item.individualCustomer()
        	.setInitials("SB")
            .setBirthDate(1946, 5, 9)
            .setName("Sneider", "Boasman")
            .setStreetAddress("Gate", "42")
            .setLocality("BARENDRECHT")
            .setZipCode("1102 HG"))
        
            .setCountryCode(COUNTRYCODE.NL)
            .setClientOrderNumber("33")
            .setOrderDate("2012-12-12")
            .setCurrency(CURRENCY.SEK)
            .usePaymentMethod(PAYMENTMETHOD.INVOICE)
            	.setReturnUrl("http://myurl.se")
            	.getPaymentForm();
        
        
        String xml = form.getXmlMessage();
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--Message generated by Integration package Java--><payment><paymentmethod>INVOICE</paymentmethod><customerrefno>33</customerrefno><returnurl>http://myurl.se</returnurl><currency>SEK</currency><amount>12500</amount><lang>en</lang><customer><ssn>19460509</ssn><firstname>Sneider</firstname><lastname>Boasman</lastname><initials>SB</initials><address>Gate</address><housenumber>42</housenumber><zip>1102 HG</zip><city>BARENDRECHT</city><country>NL</country></customer><vat>2500</vat><orderrows><row><sku>1</sku><name>Prod</name><description>Specification</description><amount>12500</amount><vat>2500</vat><quantity>2</quantity><unit>st</unit></row><row><sku>1</sku><name>Relative</name><description>RelativeDiscount</description><amount>-12500</amount><vat>-2500</vat><quantity>1</quantity><unit>st</unit></row></orderrows><excludepaymentmethods></excludepaymentmethods><iscompany>false</iscompany><addinvoicefee>false</addinvoicefee></payment>";
        assertEquals(xml, expectedXML);
        
        String base64Payment = form.getXmlMessageBase64();
        String html = Base64Util.decodeBase64String(base64Payment);
        String amount = html.substring(html.indexOf("<paymentmethod>") + 15, html.indexOf("</paymentmethod>"));
        assertEquals("INVOICE", amount);
    }
}
