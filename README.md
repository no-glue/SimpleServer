SimpleServer
============

Simple web server using the actor design pattern (scala, JDK7) and
asynchronous channels.

 * Serves HTML files from ~/www
 * By Default, logs are written /tmp

The first parameter is the port number, which defaults to 8181


**Design**

SimpleServer
    -> AcceptHandler
        -> ReadActor   <---\
            -> ParseActor -/    // message ReadActor if more bytes to read
                -> HandleActor
                    -> ResponseActor

HttpRequest object tracks state and chain of actors
