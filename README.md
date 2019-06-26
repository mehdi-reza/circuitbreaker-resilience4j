# circuitbreaker-resilience4j
It has two maven projects which should be build seperately. The first project helloworld-rs is a JAX-RS service which reports success response and an error (Internal server) based on the paramter supplied (error=1)

The other project is a JAX-RS client which runs in console mode. It waits for a keystoke, comma(,) or dot(.) followed by ENTER to send a call to the above service. When keystroke is a dot(.) then it sends call to the servie with no error parameter and service returns a success response and in case of comma(,) it send a call with error paramter (error=1) and service simple throws an exception.

The client decorates the service call using resilience4j circuit breaker. The reported exceptions are shown in the console.

Before executing the client, make sure you build the helloworld-rs project and deploy the generated war into a JAX-RS and CDI complaint server.
