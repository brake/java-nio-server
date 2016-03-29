# Example of the Nonblocking NIO Echo Server

Due to a lack of working examples of nonblocking [NIO](https://docs.oracle.com/javase/7/docs/api/java/nio/package-summary.html) Server I decided to put it here.
 
Inspired by ideas from [here](http://tutorials.jenkov.com/java-nio/non-blocking-server.html) and [here](http://stackoverflow.com/a/30430086/5525962)

Server objectives:
 - listen a `5050` port in a blocking mode
 - accept any connection
 - serve each accepted connection in nonblocking mode in separate thread by returning back any bytes received
 - close connection on receiving a "stop word" (here it's `Bye.`).  