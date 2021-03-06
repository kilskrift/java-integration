package se.sveaekonomi.webpay.integration.hosted.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.sveaekonomi.webpay.integration.WebPay;
import se.sveaekonomi.webpay.integration.WebPayItem;
import se.sveaekonomi.webpay.integration.config.SveaConfig;
import se.sveaekonomi.webpay.integration.hosted.HostedOrderRowBuilder;
import se.sveaekonomi.webpay.integration.hosted.helper.ExcludePayments;
import se.sveaekonomi.webpay.integration.hosted.helper.HostedRowFormatter;
import se.sveaekonomi.webpay.integration.hosted.helper.PaymentForm;
import se.sveaekonomi.webpay.integration.order.create.CreateOrderBuilder;
import se.sveaekonomi.webpay.integration.util.constant.INVOICETYPE;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTPLANTYPE;
import se.sveaekonomi.webpay.integration.util.constant.SUBSCRIPTIONTYPE;
import se.sveaekonomi.webpay.integration.util.test.TestingTool;

public class HostedPaymentTest {

	// test setSubscriptionType
	@Test
    public void test_hostedPayment_getPaymentForm_with_setSubscriptionType() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig()) 
    		.setCountryCode(TestingTool.DefaultTestCountryCode)
    		.setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
    		.setCurrency(TestingTool.DefaultTestCurrency)
            .addOrderRow(TestingTool.createMiniOrderRow())
            .addFee(WebPayItem.shippingFee())
            .addDiscount(WebPayItem.fixedDiscount())
            .addDiscount(WebPayItem.relativeDiscount());
            
        FakeHostedPayment payment = new FakeHostedPayment(order);
        PaymentForm form = payment
            .setReturnUrl("myurl")
            .setSubscriptionType(SUBSCRIPTIONTYPE.RECURRING)
         	.getPaymentForm()
    	;
 
        // actual, expected response strings as parameter
        String expectedXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--{\"X-Svea-Integration-Version\":\"Integration package default SveaTestConfigurationProvider.\",\"X-Svea-Integration-Platform\":\"Integration package default SveaTestConfigurationProvider.\",\"X-Svea-Library-Name\":\"Java Integration Package\",\"X-Svea-Integration-Company\":\"Integration package default SveaTestConfigurationProvider.\",\"X-Svea-Library-Version\":\"2.0.2\"}--><payment><customerrefno>33</customerrefno><currency>SEK</currency><subscriptiontype>RECURRING</subscriptiontype><amount>500</amount><vat>100</vat><returnurl>myurl</returnurl><iscompany>false</iscompany><orderrows><row><sku></sku><name></name><description></description><amount>500</amount><vat>100</vat><quantity>1.0</quantity></row><row><name></name><description></description><amount>0</amount><vat>0</vat><quantity>1.0</quantity></row><row><name></name><description></description><amount>0</amount><vat>0</vat><quantity>1.0</quantity><unit></unit></row><row><name></name><description></description><amount>0</amount><vat>0</vat><quantity>1.0</quantity></row></orderrows><addinvoicefee>false</addinvoicefee></payment>";
        String actualXml = form.getXmlMessage();

        assertTrue( TestingTool.checkVersionInformationWithRequestXml( expectedXml, actualXml ) );
	}
	

	
    @Test
    public void testCalculateRequestValuesNullExtraRows() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig()) 
            .setCountryCode(TestingTool.DefaultTestCountryCode)
               .setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
               .setCurrency(TestingTool.DefaultTestCurrency)
            .addOrderRow(TestingTool.createMiniOrderRow())
            .addFee(WebPayItem.shippingFee())
            .addDiscount(WebPayItem.fixedDiscount())
            .addDiscount(WebPayItem.relativeDiscount());
            
        FakeHostedPayment payment = new FakeHostedPayment(order);
        payment
            .setReturnUrl("myurl")
            .calculateRequestValues();
        
        assertEquals(500L, (long)payment.getAmount());
    }

    @Test
    public void testVatPercentAndAmountIncVatCalculation() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())  
            .setCountryCode(TestingTool.DefaultTestCountryCode)
            .setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
            .setCurrency(TestingTool.DefaultTestCurrency)
            .addOrderRow(TestingTool.createMiniOrderRow());
        
        order.setShippingFeeRows(null);
        order.setFixedDiscountRows(null);
        order.setRelativeDiscountRows(null);
        FakeHostedPayment payment = new FakeHostedPayment(order);
        payment
            .setReturnUrl("myUrl")
            .calculateRequestValues();
        
        assertEquals(500L, (long)payment.getAmount());
    }
    
    @Test
    public void testAmountIncVatAndvatPercentShippingFee() {
      CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig()) 
            .setCountryCode(TestingTool.DefaultTestCountryCode)
            .setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
            .setCurrency(TestingTool.DefaultTestCurrency)  
            .addOrderRow(TestingTool.createMiniOrderRow())
            .addFee(WebPayItem.shippingFee()
                    .setAmountExVat(4)
                    .setVatPercent(25));
        
        order.setFixedDiscountRows(null);
        order.setRelativeDiscountRows(null);
        FakeHostedPayment payment = new FakeHostedPayment(order);
        payment
            .setReturnUrl("myUrl")
            .calculateRequestValues();
        
        assertEquals(1000L, (long)payment.getAmount());
    }
    
    @Test
    public void testAmountIncVatAndAmountExVatCalculation() {
         CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                 .setCountryCode(TestingTool.DefaultTestCountryCode)
                 .setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
                 .setCurrency(TestingTool.DefaultTestCurrency)
                 .addOrderRow(TestingTool.createMiniOrderRow());
        
        order.setShippingFeeRows(null);
        order.setFixedDiscountRows(null);
        order.setRelativeDiscountRows(null);
        FakeHostedPayment payment = new FakeHostedPayment(order);
        payment
            .setReturnUrl("myurl")
            .calculateRequestValues();
        
        assertEquals(500L, (long)payment.getAmount());
    }
    
    @Test
    public void testCreatePaymentForm() {
         CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig()) 
                 .setCountryCode(TestingTool.DefaultTestCountryCode)
                 .setClientOrderNumber(TestingTool.DefaultTestClientOrderNumber)
                 .setCurrency(TestingTool.DefaultTestCurrency)
                .addOrderRow(TestingTool.createMiniOrderRow())
                .addCustomerDetails(TestingTool.createCompanyCustomer());
        
        FakeHostedPayment payment = new FakeHostedPayment(order);
        payment.setReturnUrl("myurl");
        PaymentForm form;
        
        form = payment.getPaymentForm();
        
        Map<String, String> formHtmlFields = form.getFormHtmlFields();
        assertEquals("</form>", formHtmlFields.get("form_end_tag"));
    }
    
    @Test
    public void testExcludeInvoicesAndAllInstallmentsAllCountries() {
        FakeHostedPayment payment = new FakeHostedPayment(null);
        ExcludePayments exclude = new ExcludePayments();
        List<String> excludedPaymentMethods = payment.getExcludedPaymentMethods();
        excludedPaymentMethods.addAll(exclude.excludeInvoicesAndPaymentPlan());
        
        assertEquals(14, excludedPaymentMethods.size());
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICESE.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_SE.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_DE.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_DK.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_FI.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_NL.getValue()));
        assertTrue(excludedPaymentMethods.contains(INVOICETYPE.INVOICE_NO.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLANSE.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_SE.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_DE.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_DK.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_FI.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_NL.getValue()));
        assertTrue(excludedPaymentMethods.contains(PAYMENTPLANTYPE.PAYMENTPLAN_NO.getValue()));
    }
    
    /*
     * 30*69.99*1.25 = 2624.625 => 2624.62 w/Bankers rounding (half-to-even)
     * problem, sums to 2624.7, in xml request, i.e. calculates 30* round( (69.99*1.25), 2) :( 
     */
    @Test
    public void testAmountFromMultipleItemsDefinedWithExVatAndVatPercent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setArticleNumber("0")
                                                                    .setName("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setDescription("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setAmountExVat(69.99)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(30.0)
                                                                    .setUnit("st"));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(1, formatRowsList.size());
        assertEquals(262462, formattedTotalAmount); // 262462,5 rounded half-to-even
        assertEquals(52492, formattedTotalVat); // 52492,5 rounded half-to-even
    }

    @Test
    public void testAmountFromMultipleItemsDefinedWithIncVatAndVatPercent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setArticleNumber("0")
                                                                    .setName("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setDescription("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setAmountIncVat(87.4875) // if low precision here, i.e. 87.49, we'll get a cumulative rounding error
                                                                    .setVatPercent(25)
                                                                    .setQuantity(30.0)
                                                                    .setUnit("st"));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(1, formatRowsList.size());
        assertEquals(262462, formattedTotalAmount); // 262462,5 rounded half-to-even
        assertEquals(52492, formattedTotalVat); // 52492,5 rounded half-to-even
    }

    @Test
    public void testAmountFromMultipleItemsDefinedWithExVatAndIncVat() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setArticleNumber("0")
                                                                    .setName("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setDescription("testCalculateRequestValuesCorrectTotalAmountFromMultipleItems")
                                                                    .setAmountExVat(69.99)
                                                                    .setAmountIncVat(87.4875) // if low precision here, i.e. 87.49, we'll get a cumulative rounding error
                                                                    .setQuantity(30.0)
                                                                    .setUnit("st"));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(1, formatRowsList.size());
        assertEquals(262462, formattedTotalAmount); // 262462,5 rounded half-to-even
        assertEquals(52492, formattedTotalVat); // 52492,5 rounded half-to-even
    }


    // calculated fixed discount vat rate, single vat rate in order
    @Test
    public void testAmountFromMultipleItemsWithFixedDiscountIncVatOnly() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(69.99)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(30.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountIncVat(10.00));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(2, formatRowsList.size());
        assertEquals(261462, formattedTotalAmount); // 262462,5 - 1000 discount rounded half-to-even
        assertEquals(52292, formattedTotalVat); // 52492,5  -  200 discount (= 10/2624,62*524,92) rounded half-to-even
    }

    // explicit fixed discount vat rate, , single vat rate in order
    @Test
    public void testAmountFromMultipleItemsWithFixedDiscountIncVatAndVatPercent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(69.99)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(30.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountIncVat(12.50)
                                                                    .setVatPercent(25.0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(2, formatRowsList.size());
        assertEquals(261212, formattedTotalAmount); // 262462,5 - 1250 discount rounded half-to-even
        assertEquals(52242, formattedTotalVat); // 52492,5 - 250 discount rounded half-to-even
    }

    // calculated fixed discount vat rate, multiple vat rate in order
    @Test
    public void testAmountWithFixedDiscountIncVatOnlyWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(2.0))
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(6)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountIncVat(100.00));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();
        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);

        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(3, formatRowsList.size());
        // 100*250/356 = 70.22 incl. 25% vat => 14.04 vat as amount 
        // 100*106/356 = 29.78 incl. 6% vat => 1.69 vat as amount 
        // matches 15,73 discount (= 100/356 *56) discount
        assertEquals(25600, formattedTotalAmount); // 35600 - 10000 discount
        assertEquals(4027, formattedTotalVat); //  5600 -  1573 discount (= 10000/35600 *5600) discount
    }

    // explicit fixed discount vat rate, multiple vat rate in order
    @Test
    public void testAmountWithFixedDiscountIncVatAndVatPercentWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(2.0))
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(6)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountIncVat(125.00)
                                                                    .setVatPercent(25.0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(3, formatRowsList.size());
        assertEquals(23100, formattedTotalAmount); // 35600 - 12500 discount
        assertEquals(3100, formattedTotalVat); //  5600 -  2500 discount
    }

    @Test
    public void testAmountWithFixedDiscountExVatAndVatPercentWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(2.0))
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(6)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(3, formatRowsList.size());
        assertEquals(25600, formattedTotalAmount); // 35600 - 10000 discount
        assertEquals(5600, formattedTotalVat); //  5600 - 0 discount
    }

    @Test
    public void testAmountWithFixedDiscountExVatAndIncVatWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(2.0))
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(6)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.fixedDiscount()
                                                                    .setAmountExVat(80.00)
                                                                    .setAmountIncVat(100.00));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(3, formatRowsList.size());
        assertEquals(25600, formattedTotalAmount); // 35600 - 10000 discount
        assertEquals(3600, formattedTotalVat); //  5600 - 2000 discount
    }

    // calculated relative discount vat rate, single vat rate in order
    @Test
    public void testAmountFromMultipleItemsWithRelativeDiscountWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(69.99)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(30.0))
                                                   .addDiscount(WebPayItem.relativeDiscount()
                                                                    .setDiscountPercent(25.0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(2, formatRowsList.size());
        assertEquals(196847, formattedTotalAmount); // (262462,5  - 65615,625 discount (25%) rounded half-to-even
        assertEquals(39369, formattedTotalVat); //  52492,5  - 13123,125 discount (25%) rounded half-to-even
    }

    // calculated relative discount vat rate, single vat rate in order
    @Test
    public void testAmountFromMultipleItemsWithRelativeDiscountWithDifferentVatRatesPresent2() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(69.99)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.relativeDiscount()
                                                                    .setDiscountPercent(25.0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(2, formatRowsList.size());
        assertEquals(6562, formattedTotalAmount); // 8748,75 - 2187,18 discount rounded half-to-even
        assertEquals(1312, formattedTotalVat); // 1749,75 - 437,5 discount (1750*.25) rounded half-to-even
    }

    // calculated relative discount vat rate, multiple vat rate in order
    @Test
    public void testAmountWithRelativeDiscountWithDifferentVatRatesPresent() {
        CreateOrderBuilder order = WebPay.createOrder(SveaConfig.getDefaultConfig())
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(25)
                                                                    .setQuantity(2.0))
                                                   .addOrderRow(WebPayItem.orderRow()
                                                                    .setAmountExVat(100.00)
                                                                    .setVatPercent(6)
                                                                    .setQuantity(1.0))
                                                   .addDiscount(WebPayItem.relativeDiscount()
                                                                    .setDiscountPercent(25.0));

        // follows HostedPayment calculateRequestValues() outline:
        HostedRowFormatter formatter = new HostedRowFormatter();

        List<HostedOrderRowBuilder> formatRowsList = formatter.formatRows(order);
        long formattedTotalAmount = formatter.getTotalAmount();
        long formattedTotalVat = formatter.getTotalVat();

        assertEquals(3, formatRowsList.size());
        // 5000*.25 = 1250
        // 600*.25 = 150  
        // matches 1400 discount
        assertEquals(26700, formattedTotalAmount); // 35600 - 8900 discount
        assertEquals(4200, formattedTotalVat); //  5600 - 1400 discount (= 10000/35600 *5600) discount
    }
}
