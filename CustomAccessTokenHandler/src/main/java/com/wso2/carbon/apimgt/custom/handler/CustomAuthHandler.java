package com.wso2.carbon.apimgt.custom.handler;

import com.wso2.carbon.apimgt.custom.handler.exception.CustomHandlerException;
import com.wso2.carbon.apimgt.custom.handler.http.HttpClientFactory;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class CustomAuthHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(CustomAuthHandler.class);
    
    private String tokenServerUrl;
    private String tokenPrefix;

    public boolean handleRequest(MessageContext messageContext) {

        String method = (String) (((Axis2MessageContext)messageContext).getAxis2MessageContext().getProperty(
                Constants.Configuration.HTTP_METHOD));
        try {
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext(), false);

            String jsonPayloadToString = JsonUtil
                    .jsonPayloadToString(((Axis2MessageContext) messageContext)
                            .getAxis2MessageContext());

            log.debug("body: " + jsonPayloadToString);

            JSONParser parser = new JSONParser();
            JSONObject body = (JSONObject) parser.parse(jsonPayloadToString);

            String clientId = (String) body.get("clientId");
            String clientSecret = (String) body.get("clientSecret");

            Map headers = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            headers.put("Authorization", getAuthorizationHeader(clientId, clientSecret));
            return true;
        } catch (Exception e) {
            log.error("Error while handling json input", e);
        }
        return false;
    }

    private String getAuthorizationHeader(String clientId, String clientSecret) throws CustomHandlerException {
        CloseableHttpClient apacheClient = null;
        try {
            apacheClient = HttpClientFactory.getHttpsClient();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new CustomHandlerException("Error while getting https client", e);
        }

        HttpGet httpGet = new HttpGet(tokenServerUrl + "?clientId=" + clientId + "&clientSecret=" + clientSecret);
        RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000)
                .build();
        httpGet.setConfig(defaultRequestConfig);
        try (CloseableHttpResponse response = apacheClient.execute(httpGet)){
            String responseString = IOUtils.toString(response.getEntity().getContent());
            JSONParser parser = new JSONParser();
            JSONObject body = (JSONObject) parser.parse(responseString);

            return tokenPrefix + " " + body.get("token");
        } catch (IOException | ParseException e) {
            throw new CustomHandlerException("Error while getting or parsing response for get token request", e);
        }
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public void setTokenServerUrl(String tokenServerUrl) {
        this.tokenServerUrl = tokenServerUrl;
    }

    public String getTokenServerUrl() {
        return tokenServerUrl;
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        
    }

    @Override
    public void destroy() {

    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
}


