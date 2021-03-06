package se.sveaekonomi.webpay.integration.order.handle;

import java.util.Date;

import javax.xml.bind.ValidationException;

import se.sveaekonomi.webpay.integration.Requestable;
import se.sveaekonomi.webpay.integration.adminservice.DeliverOrdersRequest;
import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
import se.sveaekonomi.webpay.integration.exception.SveaWebPayException;
import se.sveaekonomi.webpay.integration.hosted.hostedadmin.ConfirmTransactionRequest;
import se.sveaekonomi.webpay.integration.order.OrderBuilder;
import se.sveaekonomi.webpay.integration.order.validator.HandleOrderValidator;
import se.sveaekonomi.webpay.integration.util.constant.DISTRIBUTIONTYPE;
import se.sveaekonomi.webpay.integration.util.constant.ORDERTYPE;
import se.sveaekonomi.webpay.integration.util.test.TestingTool;
import se.sveaekonomi.webpay.integration.webservice.handleorder.HandleOrder;

/**
 * @author klar-sar, Kristian Grossman-Madsen
 */
public class DeliverOrderBuilder extends OrderBuilder<DeliverOrderBuilder> {

    private HandleOrderValidator validator;
    
    private Long orderId;
    private ORDERTYPE orderType;
    private DISTRIBUTIONTYPE distributionType;
    private Long invoiceIdToCredit;
    private Integer numberOfCreditDays;
    private String captureDate;
    
    public DeliverOrderBuilder(ConfigurationProvider config) {
        this.config = config;
    }
    
    public HandleOrderValidator getValidator() {
        return validator;
    }
    
    public DeliverOrderBuilder setValidator(HandleOrderValidator validator) {
        this.validator = validator;
        return this;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    /**
     * required, invoice or payment plan only, order to deliver
     */
    public DeliverOrderBuilder setOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }
    
    public ORDERTYPE getOrderType() {
        return orderType;
    }
    public void setOrderType(ORDERTYPE orderType) {
    	this.orderType = orderType;
    }        
    /** @deprecated */
    public void setOrderType(String orderType) {
    	if( orderType.equals(ORDERTYPE.Invoice.toString()) ) {
    		this.orderType = ORDERTYPE.Invoice;
    	}
    	else if( orderType.equals(ORDERTYPE.PaymentPlan.toString()) ) {
    		this.orderType = ORDERTYPE.PaymentPlan;
    	}
    	else {
    		throw new IllegalArgumentException( "OrderType must be one of 'Invoice' or 'PaymentPlan' when given as String" );
    	}    		
    }

    public DISTRIBUTIONTYPE getInvoiceDistributionType() {
        return distributionType;
    }
    
    public DeliverOrderBuilder setInvoiceDistributionType(DISTRIBUTIONTYPE type) {
        this.distributionType = type;
        return this;
    }
    
    public Long getCreditInvoice() {
        return invoiceIdToCredit;
    }
    public DeliverOrderBuilder setCreditInvoice(Long invoiceId) {
        this.invoiceIdToCredit = invoiceId;
        return this;
    }
    /** @deprecated */
    public DeliverOrderBuilder setCreditInvoice(String invoiceId) {
        this.invoiceIdToCredit = Long.valueOf(invoiceId);
        return this;
    }
    
    public Integer getNumberOfCreditDays() {
        return numberOfCreditDays;
    }  
    public DeliverOrderBuilder setNumberOfCreditDays(Integer numberOfCreditDays) {
        this.numberOfCreditDays = numberOfCreditDays;
        return this;
    }
    /** @deprecated */
    public DeliverOrderBuilder setNumberOfCreditDays(int numberOfCreditDays) {
        this.numberOfCreditDays = numberOfCreditDays;
        return this;
    }
    
	/**
	 * card only, optional
	 * @param date -- date on format YYYY-MM-DD (similar to ISO8601 date)
	 * @return DeliverOrderBuilder
	 */
	public DeliverOrderBuilder setCaptureDate(String captureDate) {
		this.captureDate = captureDate;
		return this;
	}
	
    public String getCaptureDate() {
        return captureDate;
    }
    
	/**
	 * card only, optional -- alias for setOrderId
	 * @param transactionId as string, i.e. as transactionId is returned in HostedPaymentResponse
	 * @return DeliverOrderBuilder
	 */
    public DeliverOrderBuilder setTransactionId( String transactionId) {        
        return setOrderId( Long.parseLong(transactionId) );
    }
    
    public long getTransactionId() {
        return getOrderId();
    }
    
    
    /**
     * Updates the order builder with additional information and passes the
     * order builder to the correct request class.
     * @return DeliverOrdersRequest (extends Requestable) or HandleOrder (extends Requestable)
     */
	public <T extends Requestable> T deliverInvoiceOrder() {

    	if( this.orderRows.isEmpty() ) {
	    	this.setOrderType(ORDERTYPE.Invoice);
    		return (T) new DeliverOrdersRequest(this);
    	}
	    else {
	    	this.setOrderType(ORDERTYPE.Invoice);
	        return (T) new HandleOrder(this);
	    }
    }
    
    /**
     * Prepares the PaymentPlan order for delivery.
     * @return HandleOrder
     */
    public <T extends Requestable> T deliverPaymentPlanOrder() {
    	this.setOrderType(ORDERTYPE.PaymentPlan);
		return (T) new HandleOrder(this);
    }
    
    /**
     * deliverCardOrder() is used to set the status of a card order to CONFIRMED
     * 
     * A default capturedate equal to the current date will be supplied. This 
     * may be overridden using the ConfirmTransaction setCaptureDate() method 
     * on the returned ConfirmTransaction object.
     * 
     * @return DeliverPaymentPlan
     */    
	public ConfirmTransactionRequest deliverCardOrder() {
		// no need to .setOrderType(), as ConfirmTransactionRequest ignores it

		// validate request and throw exception if validation fails
        String errors = validateDeliverCardOrder(); 
        if (!errors.equals("")) {
            throw new SveaWebPayException("Validation failed", new ValidationException(errors));
        } 
					
        // if no captureDate set, use today's date as default.
        if( this.getCaptureDate() == null ) {
        	this.setCaptureDate( String.format("%tF", new Date()) ); //'t' => time, 'F' => ISO 8601 complete date formatted as "%tY-%tm-%td"
        }
		
        ConfirmTransactionRequest request = new ConfirmTransactionRequest(this.getConfig())
        	.setCaptureDate( this.getCaptureDate() )
        	.setTransactionId( Long.toString(this.getTransactionId()) )
        	.setCountryCode(TestingTool.DefaultTestCountryCode)
    	;
        
        return request;
	}

	// validates deliverCardOrder (confirm) required attributes
    public String validateDeliverCardOrder() {
        String errors = "";
        if (this.getCountryCode() == null) {
            errors += "MISSING VALUE - CountryCode is required, use setCountryCode(...).\n";
        }
        
        if (this.getOrderId() == null) {
            errors += "MISSING VALUE - OrderId is required, use setOrderId().\n";
    	}
        return errors;    
    }	
}
