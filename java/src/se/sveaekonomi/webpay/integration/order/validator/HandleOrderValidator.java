package se.sveaekonomi.webpay.integration.order.validator;

import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderBuilder;

public class HandleOrderValidator {

    private String errors = "";
    
    /**
     * @param order
     * @return string containing cumulative list of error
     */
    public String validate(DeliverOrderBuilder order) {
        errors = "";
        validateCountry(order);
        validateOrderType(order);
        validateOrderId(order);
        validateInvoiceDetails(order);
        return errors;
    }
    
    private void validateCountry(DeliverOrderBuilder order) {
        if (order.getCountryCode() == null) {
            this.errors += "MISSING VALUE - CountryCode is required, use setCountryCode(...).\n";
        }
    }
    
    private void validateOrderType(DeliverOrderBuilder order) {
        if (order.getOrderType() == null) {
            this.errors += "MISSING VALUE - OrderType is missing for DeliverOrder, use setOrderType().\n";
        }
    }
    
    private void validateOrderId(DeliverOrderBuilder order) {
        if (order.getOrderId() == null) {
            this.errors += "MISSING VALUE - setOrderId is required.\n";
        }
    }
    
    private void validateInvoiceDetails(DeliverOrderBuilder order) {
        if (	(order.getOrderId() != null) &&
                order.getOrderType().toString().equals("Invoice") &&
                order.getInvoiceDistributionType() == null
            ) 
        {
            this.errors += "MISSING VALUE - setInvoiceDistributionType is required for deliverInvoiceOrder.\n";
        }
    }    
}
