package com.cmu.miis.capstone.rr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.net.HttpHeaders;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


@Path("/")
public class RestApi {

    private static final ObjectMapper nodeFactory = new ObjectMapper();
    private static final int MAX_REQUEST_ATTEMPT = 3;
    private final CloseableHttpClient client;
    private static final String DOMAIN = "gateway.watsonplatform.net/retrieve-and-rank/api/v1/solr_clusters/";

    public RestApi(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .build();

        this.client = HttpClients.custom()
                .disableCookieManagement()
                .disableContentCompression()
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(20)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @POST
    @Path("v1/rr/createcluster/users/{username}/pwd/{passwd}")
    public Response createCluster(@PathParam("username") String userName, @PathParam("passwd") String passwd) throws IOException {
        HttpRequestBase request = buildPostRequest(userName,passwd,DOMAIN);
        HttpResponse response = requestWithRetry(request);
        String jsonStr = EntityUtils.toString(response.getEntity());
        JSONObject jsObj = new JSONObject(jsonStr);
        return Response.status(200).entity(jsObj).build();
    }

    @GET
    @Path("/v1/rr/standard/{id}")
    public Response testGet(@PathParam("id") String id) throws IOException {
        ObjectNode response = buildResponse(id);
        return Response.status(200).entity(response.toString()).build();
    }

    private ObjectNode buildResponse( String id ) throws IOException{
        ObjectNode response = nodeFactory.createObjectNode();
        response.put("clusterId",id);
        return response;
    }

    private HttpRequestBase buildPostRequest(String username,String passwd,String domain) throws UnsupportedEncodingException {
        String url = "https://" + username + ":" + passwd +"@"+domain;
        HttpPost request = new HttpPost(url);
//        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.setEntity(new StringEntity(""));
        return request;
    }

    protected HttpResponse requestWithRetry(HttpRequestBase request){
        CloseableHttpResponse response = null;
        for(int i = 0;i < MAX_REQUEST_ATTEMPT;i++){
            try{
                response = client.execute(request);
                return response;
            }catch (IOException e){
            }
        }
        return response;
    }

}
