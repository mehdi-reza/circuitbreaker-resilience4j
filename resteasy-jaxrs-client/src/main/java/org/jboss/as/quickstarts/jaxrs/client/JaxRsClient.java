package org.jboss.as.quickstarts.jaxrs.client;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * This example demonstrates the use an external JAX-RS RestEasy client
 * which interacts with a JAX-RS Web service that uses CDI 1.0 and JAX-RS
 * in Red Hat JBoss Enterprise Application Platform.  Specifically,
 * this client "calls" the HelloWorld JAX-RS Web Service created in
 * quickstart helloworld-rs.  Please refer to the helloworld-rs README.md
 * for instructions on how to build and deploy helloworld-rs.
 */

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * JUnit4 Test class which makes a request to the RESTful helloworld-rs web service.
 *
 * @author bmincey (Blaine Mincey)
 */
public class JaxRsClient {
    /**
     * Request URLs pulled from system properties in pom.xml
     */
	private static String XML_URL;
    private static String XML_ERROR_URL;
    private static String JSON_URL;

    /**
     * Property names used to pull values from system properties in pom.xml
     */
    private static final String XML_PROPERTY = "xmlUrl";
    private static final String XML_ERROR_PROPERTY = "xmlErrorUrl";
    private static final String JSON_PROPERTY = "jsonUrl";

    /**
     * Responses of the RESTful web service
     */
    private static final String XML_RESPONSE = "<xml><result>Hello World!</result></xml>";
    private static final String JSON_RESPONSE = "{\"result\":\"Hello World!\"}";

    private CircuitBreakerRegistry breakerRegistery = CircuitBreakerRegistry.of(
		CircuitBreakerConfig.custom().failureRateThreshold(20).ringBufferSizeInClosedState(10).ringBufferSizeInHalfOpenState(2).build()
	);
    		

    private CircuitBreaker breaker;
    
    public JaxRsClient() {
    	breaker = breakerRegistery.circuitBreaker("default");
	}
    /**
     * Method executes BEFORE the test method. Values are read from system properties that can be modified in the pom.xml.
     */

    public static void main(String[] args) {
        JaxRsClient.XML_URL = System.getProperty(JaxRsClient.XML_PROPERTY);
        JaxRsClient.XML_ERROR_URL = System.getProperty(JaxRsClient.XML_ERROR_PROPERTY);
        JaxRsClient.JSON_URL = System.getProperty(JaxRsClient.JSON_PROPERTY);
        new JaxRsClient().test();
    }

    /**
     * Test method which executes the runRequest method that calls the RESTful helloworld-rs web service.
     */
    void test() {
    	new Thread(()-> {
    		char input;
			try {
				while(true) {
					input = (char)System.in.read();
					if(input==',' || input=='.') {
						String response = null;
						try {
							response = breaker.decorateSupplier(surroundWithCircuitBreaker(input=='.' ? JaxRsClient.XML_URL : JaxRsClient.XML_ERROR_URL, MediaType.APPLICATION_XML_TYPE)).get();
			    	        if (!JaxRsClient.XML_RESPONSE.equals(response)) {
			    	            throw new RuntimeException("Response is wrong:\nXML Response:" + response + "\nshould be: " + XML_RESPONSE);
			    	        }
						} catch(RuntimeException e) {
							e.printStackTrace();
						}
		    		}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}    		
    	}).start();
       /* response = runRequest(JaxRsClient.JSON_URL, MediaType.APPLICATION_JSON_TYPE);

        if (!JaxRsClient.JSON_RESPONSE.equals(response)) {
            throw new RuntimeException("Response is wrong:\nJSON Response:" + response + "\nshould be: " + JSON_RESPONSE);
        }*/

    }

    /**
     * The purpose of this method is to run the external REST request.
     *
     * @param url       The url of the RESTful service
     * @param mediaType The mediatype of the RESTful service
     */
    private String runRequest(String url, MediaType mediaType) {
        String result = null;

        System.out.println("===============================================");
        System.out.println("URL: " + url);
        System.out.println("MediaType: " + mediaType.toString());


        // Using the RESTEasy libraries, initiate a client request
        ResteasyClient client = new ResteasyClientBuilder().build();

        // Set url as target
        ResteasyWebTarget target = client.target(url);

        // Be sure to set the mediatype of the request
        target.request(mediaType);

        // Request has been made, now let's get the response
        Response response = target.request().get();
        result = response.readEntity(String.class);
        response.close();

        // Check the HTTP status of the request
        // HTTP 200 indicates the request is OK
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed request with HTTP status: " + response.getStatus());
        }

        // We have a good response, let's now read it
        System.out.println("\n*** Response from Server ***\n");
        System.out.println(result);
        System.out.println("\n===============================================");

        return result;
    }
    
    public Supplier<String> surroundWithCircuitBreaker(String url, MediaType mediaType) {
    	return () -> {
    		return runRequest(url, mediaType);
    	};
    }
}