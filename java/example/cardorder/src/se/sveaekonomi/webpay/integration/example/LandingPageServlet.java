package se.sveaekonomi.webpay.integration.example;

import java.io.IOException;

//import servlet classes (provided in /lib/servlet-api.jar)
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import Svea integration package (provided in /lib/sveawebpay.jar)
import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
import se.sveaekonomi.webpay.integration.config.SveaTestConfigurationProvider;
import se.sveaekonomi.webpay.integration.response.hosted.SveaResponse;
import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;

/**
 * example file, how to process a card order response at the return url
 * 
 * @author Kristian Grossman-madsen for Svea WebPay
 */
public class LandingPageServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 1L;

	public LandingPageServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		doPost(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
			
		// get configuration object holding the Svea service login credentials and the secret word corresponding to our merchant id
		ConfigurationProvider myConfig = new SveaTestConfigurationProvider();
		String mySecretWord = myConfig.getSecretWord(PAYMENTTYPE.HOSTED, COUNTRYCODE.SE); // countrycode should correspond to the request country code
		
		// process the response post data returned by Svea		
	    SveaResponse myResponse = 
	    		new SveaResponse( 
		    		request.getParameter("response"),	// POST "response", the base64 encoded response message
				request.getParameter("mac"),		// the mac received, used for response validation
		    		mySecretWord				// the secretword corresponding to the request merchant id
				)
	    ;		
						
		// Pass service response to landingpage.jsp view as attribute in HttpServletRequest
	    request.setAttribute("cardorder_response", myResponse);
	    request.setAttribute("raw_response", request.getParameter("response"));
	    request.setAttribute("raw_response_mac", request.getParameter("mac"));

	    request.getRequestDispatcher("/landingpage.jsp").forward(request, response);				
	}
}
