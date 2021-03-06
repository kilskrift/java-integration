package se.sveaekonomi.webpay.integration.webservice.handleorder;

import javax.xml.bind.ValidationException;

import org.w3c.dom.NodeList;

import se.sveaekonomi.webpay.integration.Requestable;
import se.sveaekonomi.webpay.integration.exception.SveaWebPayException;
import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderBuilder;
import se.sveaekonomi.webpay.integration.order.validator.HandleOrderValidator;
import se.sveaekonomi.webpay.integration.response.webservice.DeliverOrderResponse;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;
import se.sveaekonomi.webpay.integration.webservice.helper.WebServiceXmlBuilder;
import se.sveaekonomi.webpay.integration.webservice.helper.WebserviceRowFormatter;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaAuth;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaDeliverInvoiceDetails;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaDeliverOrder;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaDeliverOrderInformation;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaRequest;
import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaSoapBuilder;

public class HandleOrder implements Requestable {

    private DeliverOrderBuilder order;
    private SveaDeliverOrder sveaDeliverOrder;
    private SveaDeliverOrderInformation orderInformation;
    
    public HandleOrder(DeliverOrderBuilder orderBuilder) {
        this.order =  orderBuilder;
    }
    
    protected SveaAuth getStoreAuthorization() {
         SveaAuth auth = new SveaAuth();
         PAYMENTTYPE orderType = (order.getOrderType().toString().equals("Invoice") ? PAYMENTTYPE.INVOICE : PAYMENTTYPE.PAYMENTPLAN);
         auth.Username = order.getConfig().getUsername(orderType, order.getCountryCode());
         auth.Password = order.getConfig().getPassword(orderType, order.getCountryCode());
         auth.ClientNumber = order.getConfig().getClientNumber(orderType, order.getCountryCode());
         return auth;
    }
    
    public String validateOrder() {
        try {
            HandleOrderValidator validator = new HandleOrderValidator();
            return validator.validate(this.order);
        } catch (NullPointerException e) {
            return "NullPointer in validaton of HandleOrder";
        }
    }
    
    public SveaRequest<SveaDeliverOrder> prepareRequest() {
    	return prepareRequest(null);
    }
    
    public SveaRequest<SveaDeliverOrder> prepareRequest(Boolean usePriceIncludingVat) {
        String errors = "";
        errors = validateOrder();
        
        if (errors.length() > 0) {
            throw new SveaWebPayException("Validation failed", new ValidationException(errors));
        }
        
        sveaDeliverOrder = new SveaDeliverOrder();
        sveaDeliverOrder.auth = getStoreAuthorization(); 
        orderInformation = new SveaDeliverOrderInformation(order.getOrderType().toString());
        orderInformation.setOrderId(String.valueOf(order.getOrderId()));
        orderInformation.setOrderType(order.getOrderType().toString());
        
        if (order.getOrderType().toString().equals("Invoice")) {
            SveaDeliverInvoiceDetails invoiceDetails = new SveaDeliverInvoiceDetails();
            invoiceDetails.InvoiceDistributionType = order.getInvoiceDistributionType().toString();
            invoiceDetails.IsCreditInvoice = (order.getCreditInvoice()!=null ? true : false);
            if (order.getCreditInvoice()!=null)
                invoiceDetails.InvoiceIdToCredit = String.valueOf(order.getCreditInvoice());
            invoiceDetails.NumberofCreditDays = (order.getNumberOfCreditDays()!=null 
                    ? order.getNumberOfCreditDays() : 0);
            
            WebserviceRowFormatter formatter = new WebserviceRowFormatter(order);
                        
            invoiceDetails.OrderRows  = formatter.formatRows(usePriceIncludingVat); 
            orderInformation.deliverInvoiceDetails = invoiceDetails;
        }
        
        sveaDeliverOrder.deliverOrderInformation = orderInformation;
        SveaRequest<SveaDeliverOrder> request = new SveaRequest<SveaDeliverOrder>();
        request.request = sveaDeliverOrder;
        return request;
    }
    
    public DeliverOrderResponse doRequest() {
        PAYMENTTYPE orderType = (order.getOrderType().toString().equals("Invoice") ? PAYMENTTYPE.INVOICE : PAYMENTTYPE.PAYMENTPLAN);
        
        // prepare request xml
        SveaRequest<SveaDeliverOrder> request = this.prepareRequest();
        WebServiceXmlBuilder xmlBuilder = new WebServiceXmlBuilder();
        String xml = xmlBuilder.getDeliverOrderEuXml(request.request);
        //System.out.println( xml ); // debug, print xml
        
        // send soap request
        SveaSoapBuilder soapBuilder = new SveaSoapBuilder();
        String soapMessage = soapBuilder.makeSoapMessage("DeliverOrderEu", xml);
        NodeList soapResponse = soapBuilder.deliverOrderEuRequest(soapMessage, order.getConfig(), orderType );
        DeliverOrderResponse response = new DeliverOrderResponse(soapResponse); 
        
        // if we received error 50036 from webservice , resend request with PriceIncludingVat flipped in request
        String resultCode = response.getResultCode();
		if( resultCode.equals("50036") ) {         				
			Boolean oldVatFlag = request.request.deliverOrderInformation.getDeliverInvoiceDetails().OrderRows.get(0).PriceIncludingVat;   
        	SveaRequest<SveaDeliverOrder> flippedVatRequest = this.prepareRequest(!oldVatFlag);
            String flippedVatXml = xmlBuilder.getDeliverOrderEuXml(flippedVatRequest.request);
            SveaSoapBuilder newSoapBuilder = new SveaSoapBuilder();
            String flippedVatSoapMessage = newSoapBuilder.makeSoapMessage("DeliverOrderEu", flippedVatXml);
            NodeList flippedVatSoapResponse = newSoapBuilder.deliverOrderEuRequest(flippedVatSoapMessage, order.getConfig(), orderType);
            DeliverOrderResponse flippedVatResponse = new DeliverOrderResponse(flippedVatSoapResponse);   
            response = flippedVatResponse;
        }
        
        return response;
    }
}
