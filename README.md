# Sample Security Handler calling an external service

By default API Manager APIs consists of handlers that includes Security, Throttling and Analytics. 

This sample is an example handler that does an external service call and add a custom security header 
before sending the request to the backend.

![](https://github.com/malinthaprasan/APIMCustomSecurity/raw/master/Custom%20Handler%20Drawing.jpg)

## Instructions to deploy

1. Build `token-server` using `mvn clean install`. 
2. Deploy the `target/token-service.war` in a tomcat based server. For this demo, copy that into `repository/deployment/server/webapps` folder.
3. Build `CustomAccessTokenHandler` using `mvn clean install`.
4. Copy `CustomAccessTokenHandler/target/com.wso2.carbon.apimgt.custom.handler-1.0.0.jar` into `repository/components/lib` folder.
5. Open `repository/resources/api_templates/velocity_template.xml`. This is used as the template when creating an API synapse configuration. 

Add below configuration in `<handlers xmlns="http://ws.apache.org/ns/synapse">` section. This is the Custom Handler that we just 
added to the lib folder.

`tokenServerUrl` denotes the external service URL. For now, this is pointed to the server we just deployed in Step 2.
`tokenPrefix` denotes the prefix value that will be added before the token. For example, below 'ABC' will generate `Authorization: ABC <token>`

```
      <handler class="com.wso2.carbon.apimgt.custom.handler.CustomAuthHandler">
          <property name="tokenServerUrl" value="https://localhost:9443/token-service/token"/>
          <property name="tokenPrefix" value="ABC"/>
      </handler>
```

Once it is added, the file will look like below:

```
        </resource>
        #set ($resourceNo = $resourceNo + 1)
        #end  ## end of resource iterator
        ## print the handlers
        #if($handlers.size() > 0)
<handlers xmlns="http://ws.apache.org/ns/synapse">
      <handler class="com.wso2.carbon.apimgt.custom.handler.CustomAuthHandler">
          <property name="tokenServerUrl" value="https://localhost:9443/token-service/token"/>
          <property name="tokenPrefix" value="ABC"/>
      </handler>

#foreach($handler in $handlers)
<handler xmlns="http://ws.apache.org/ns/synapse" class="$handler.className">
    #if($handler.hasProperties())
```

Now we are done with configurations of API Manager. 

6. Restart the API Manager server by executing `./wso2server.sh restart` from `/bin` folder. Wait till the server starts.

7. Now create and publish an API. We can use the sample Pizzashack API. (Or if you have an existing API, 
republish it by clicking on the API, then Edit -> Manage -> Save & Publish.)

Now the API is successfully deployed with the custom handler. 

In order to verify that, we can open the synapse configuration of the API and check if the above handler exists. 
Replace <API-Name> and <version> with the name of your API and version. If we use Pizzashack API, the file name
would be `admin--PizzaShackAPI_v1.0.0.xml`

`repository/deployment/server/synapse-configs/default/api/admin--<API-Name>_v<version>.xml` 

In the file under `<handlers>` section below, we should be able to see the handler we added like below.

```
   <handlers>
      <handler class="com.wso2.carbon.apimgt.custom.handler.CustomAuthHandler">    <<==== Custom API Handler
         <property name="tokenServerUrl"
                   value="https://localhost:9443/token-service/token"/>
         <property name="tokenPrefix" value="ABC"/>
      </handler>
      <handler class="org.wso2.carbon.apimgt.gateway.handlers.common.APIMgtLatencyStatsHandler"/>
      <handler class="org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler">
         <property name="apiImplementationType" value="ENDPOINT"/>
      </handler>
      ...
   </handlers>
```
8. Open the server logs using `tail -f repository/logs/wso2carbon.log`

9. Try invoking the API with below payload.

```
{
  "clientId": "abcdefg",
  "clientSecret": "pqrstuv"
}
```

10. You would be able to see below in the server logs.


### Request coming to API Manager

```
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "POST /pizzashack/1.0.0/order HTTP/1.1[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Host: 172.17.0.1:8243[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Connection: keep-alive[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Content-Length: 56[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "accept: application/json[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Origin: https://localhost:9443[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Content-Type: application/json[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Referer: https://localhost:9443/store/apis/info?name=PizzaShackAPI&version=1.0.0&provider=admin&tenant=carbon.super[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Accept-Encoding: gzip, deflate, br[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "Accept-Language: en,en-US;q=0.9[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "[\r][\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "{[\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "  "clientId": "abcdefg",[\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "  "clientSecret": "pqrstuv"[\n]"
[2018-10-26 13:31:53,061] DEBUG - wire HTTPS-Listener I/O dispatcher-6 >> "}"
```

### Request going to backend from API Manager

```
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "POST /am/sample/pizzashack/v1/api/order HTTP/1.1[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Authorization: ABC YWJjZGVmZy0tcHFyc3R1dg[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Origin: https://localhost:9443[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Referer: https://localhost:9443/store/apis/info?name=PizzaShackAPI&version=1.0.0&provider=admin&tenant=carbon.super[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Accept-Encoding: gzip, deflate, br[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Accept-Language: en,en-US;q=0.9[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "accept: application/json[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Content-Type: application/json[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Transfer-Encoding: chunked[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Host: localhost:9443[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Connection: Keep-Alive[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "User-Agent: Synapse-PT-HttpComponents-NIO[\r][\n]"
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "[\r][\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "38[\r][\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "{[\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "  "clientId": "abcdefg",[\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "  "clientSecret": "pqrstuv"[\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "}[\r][\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "0[\r][\n]"
[2018-10-26 13:31:53,362] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "[\r][\n]"
```

You can see from above below `Authorization` header above will be sent to the backend request.

```
[2018-10-26 13:31:53,361] DEBUG - wire HTTPS-Sender I/O dispatcher-3 << "Authorization: ABC YWJjZGVmZy0tcHFyc3R1dg[\r][\n]"
```
