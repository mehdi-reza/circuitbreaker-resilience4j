# circuitbreaker-resilience4j

It has two maven projects which should be build seperately. The first project **helloworld-rs** is a JAX-RS service which reports success response and an error (Internal server) based on the paramter supplied (error=1)

The other project **resteasy-jaxrs-client** is a client which runs in console mode. It waits for a keystoke, comma(,) dot(.) and slash(/)  followed by ENTER to send a call to the above service. When keystroke is a dot(.) then it sends call to the service without any error parameter and service returns a success response. In the case of comma(,) it send a call with error paramter (error=1) and service simply throws an exception. To see metrics of circuit breaker use slash(/) followed by enter will show you the state whether open, half open or closed including failure rate.

The client decorates the service call using resilience4j circuit breaker. The reported exceptions are shown in the console.

Before executing the client, make sure you build the helloworld-rs project and deploy the generated war into a JAX-RS and CDI complaint server. The URLs to make the service call are declared in resteasy-jaxrs-client/pom.xml.

Configured circuit breaker values are:

`CircuitBreakerConfig.custom().failureRateThreshold(20).ringBufferSizeInClosedState(10).ringBufferSizeInHalfOpenState(2).build()`

#### Execute client

`mvn clean package exec:java`
