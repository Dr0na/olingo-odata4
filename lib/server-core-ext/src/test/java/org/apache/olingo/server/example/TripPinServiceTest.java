/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.commons.core.Encoder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Please note that NONE of the system query options are developed in the sample
 * service like $filter, $orderby etc. So using those options will be ignored
 * right now. These tests designed to test the framework, all options are responsibilities
 * of service developer.
 */
public class TripPinServiceTest {
  private static Tomcat tomcat = new Tomcat();
  private static String baseURL;
  private static DefaultHttpClient http = new DefaultHttpClient();
  private static final int TOMCAT_PORT = 9900;

  @BeforeClass
  public static void beforeTest() throws Exception {
    tomcat.setPort(TOMCAT_PORT);
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    tomcat.setBaseDir(baseDir.getAbsolutePath());
    tomcat.getHost().setAppBase(baseDir.getAbsolutePath());
    Context cxt = tomcat.addContext("/trippin", baseDir.getAbsolutePath());
    Tomcat.addServlet(cxt, "trippin", new TripPinServlet());
    cxt.addServletMapping("/*", "trippin");
    baseURL = "http://" + tomcat.getHost().getName() + ":"+ TOMCAT_PORT+"/trippin";
    tomcat.start();
  }

  @AfterClass
  public static void afterTest() throws Exception {
    tomcat.stop();
  }

  private HttpHost getLocalhost() {
    return new HttpHost(tomcat.getHost().getName(), TOMCAT_PORT);
  }
  
  private HttpResponse httpGET(String url, int expectedStatus) throws Exception{
    HttpRequest request = new HttpGet(url);
	  return httpSend(request, expectedStatus);
  }
  
  private HttpResponse httpSend(HttpRequest request, int expectedStatus) throws Exception{
    HttpResponse response = http.execute(getLocalhost(), request);
    assertEquals(expectedStatus, response.getStatusLine().getStatusCode());
    return response;
  }

  private JsonNode getJSONNode(HttpResponse response) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode node = objectMapper.readTree(response.getEntity().getContent());
    return node;
  }
  
  private String getHeader(HttpResponse response, String header) {
    Header[] headers = response.getAllHeaders();
    for (Header h : headers) {
      if (h.getName().equalsIgnoreCase(header)) {
        return h.getValue();
      }
    }
    return null;
  }

  @Test
  public void testEntitySet() throws Exception {
    HttpRequest req = new HttpGet(baseURL+"/People");
    req.setHeader("Content-Type", "application/json;odata.metadata=minimal");

    HttpResponse response = httpSend(req, 200);
    JsonNode node = getJSONNode(response);

    assertEquals("$metadata#People", node.get("@odata.context").asText());
    assertEquals(baseURL+"/People?$skiptoken=8", node.get("@odata.nextLink").asText());

    JsonNode person = ((ArrayNode)node.get("value")).get(0);
    assertEquals("russellwhyte", person.get("UserName").asText());
  }

  @Test
  public void testReadEntitySetWithPaging() throws Exception {
    String url = baseURL+"/People";
    HttpRequest request = new HttpGet(url);
    request.setHeader("Prefer", "odata.maxpagesize=10");
    HttpResponse response = httpSend(request, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People", node.get("@odata.context").asText());
    assertEquals(baseURL+"/People?$skiptoken=10", node.get("@odata.nextLink").asText());

    JsonNode person = ((ArrayNode)node.get("value")).get(0);
    assertEquals("russellwhyte", person.get("UserName").asText());
    assertEquals("odata.maxpagesize=10", getHeader(response, "Preference-Applied"));
  }

  @Test
  public void testReadEntityWithKey() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines('AA')", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#Airlines/$entity", node.get("@odata.context").asText());
    assertEquals("American Airlines", node.get("Name").asText());
    //assertEquals("/Airlines('AA')/Picture", node.get("Picture@odata.mediaReadLink").asText());
  }

  @Test
  public void testReadEntityWithNonExistingKey() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines('OO')", 404);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testRead$Count() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines/$count", 200);
    assertEquals("15", IOUtils.toString(response.getEntity().getContent()));
  }

  @Test
  public void testReadPrimitiveProperty() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines('AA')/Name", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#Airlines('AA')/Name", node.get("@odata.context").asText());
    assertEquals("American Airlines", node.get("value").asText());
  }

  @Test
  public void testReadNonExistentProperty() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines('AA')/Unknown", 404);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadPrimitiveArrayProperty() throws Exception {
    HttpResponse response = httpGET(baseURL + "/People('russellwhyte')/Emails", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Emails", node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("Russell@example.com", ((ArrayNode)node.get("value")).get(0).asText());
    assertEquals("Russell@contoso.com", ((ArrayNode)node.get("value")).get(1).asText());
  }

  @Test
  public void testReadPrimitivePropertyValue() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Airlines('AA')/Name/$value", 200);
    assertEquals("American Airlines", IOUtils.toString(response.getEntity().getContent()));
  }

  @Test @Ignore
  // TODO: Support geometry types to make this run
  public void testReadComplexProperty() throws Exception {
    //HttpResponse response = httpGET(baseURL + "/Airports('KSFO')/Location");
    //fail("support geometry type");
  }

  @Test
  public void testReadComplexArrayProperty() throws Exception {
    HttpResponse response = httpGET(baseURL + "/People('russellwhyte')/AddressInfo", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/AddressInfo", node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("187 Suffolk Ln.", ((ArrayNode)node.get("value")).get(0).get("Address").asText());
  }

  @Test
  public void testReadMedia() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Photos(1)/$value", 200);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testCreateMedia() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Photos(1)/$value";
    HttpPut request = new HttpPut(editUrl);
    request.setEntity(new ByteArrayEntity("bytecontents".getBytes(), ContentType.APPLICATION_OCTET_STREAM));
    HttpResponse response = httpSend(request, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testDeleteMedia() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Photos(1)/$value";
    HttpDelete request = new HttpDelete(editUrl);
    HttpResponse response = httpSend(request, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testCreateStream() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Airlines('AA')/Picture";
    HttpPost request = new HttpPost(editUrl);
    request.setEntity(new ByteArrayEntity("bytecontents".getBytes(), ContentType.APPLICATION_OCTET_STREAM));
    // method not allowed
    HttpResponse response = httpSend(request, 405);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testCreateStream2() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Airlines('AA')/Picture";
    HttpPut request = new HttpPut(editUrl);
    request.setEntity(new ByteArrayEntity("bytecontents".getBytes(), ContentType.APPLICATION_OCTET_STREAM));
    HttpResponse response = httpSend(request, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testDeleteStream() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Airlines('AA')/Picture";
    HttpDelete request = new HttpDelete(editUrl);
    HttpResponse response = httpSend(request, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadStream() throws Exception {
    // treating update and create as same for now, as there is details about
    // how entity payload and media payload can be sent at same time in request's body
    String editUrl = baseURL + "/Airlines('AA')/Picture";
    HttpResponse response = httpGET(editUrl, 200);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testLambdaAny() throws Exception {
    // this is just testing to see the lamda expressions are going through the
    // framework, none of the system options are not implemented in example service
    String query = "Friends/any(d:d/UserName eq 'foo')";
    HttpResponse response = httpGET(baseURL + "/People?$filter="+Encoder.encode(query), 200);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testSingleton() throws Exception {
    HttpResponse response = httpGET(baseURL + "/Me", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#Me", node.get("@odata.context").asText());
    assertEquals("russellwhyte", node.get("UserName").asText());
  }

  @Test
  public void testSelectOption() throws Exception {
    HttpResponse response = httpGET(baseURL + "/People('russellwhyte')?$select=FirstName,LastName", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People(FirstName,LastName)/$entity", node.get("@odata.context").asText());
    assertEquals("Russell", node.get("FirstName").asText());
  }

  @Test
  public void testActionImportWithNoResponse() throws Exception {
    HttpPost request = new HttpPost(baseURL + "/ResetDataSource");
    HttpResponse response = httpSend(request, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test @Ignore
  public void testFunctionImport() throws Exception {
    //TODO: fails because of lack of geometery support
    HttpResponse response = httpGET(baseURL + "/GetNearestAirport(lat=23.0,lon=34.0)", 200);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testBadReferences() throws Exception {
    HttpResponse response = httpGET(baseURL + "/People('russelwhyte')/$ref", 405);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadReferences() throws Exception {
    HttpResponse response = httpGET(baseURL + "/People('russellwhyte')/Friends/$ref", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#Collection($ref)", node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("/People('scottketchum')", ((ArrayNode)node.get("value")).get(0).get("@odata.id").asText());
  }

  @Test
  public void testAddCollectionReferences() throws Exception {
    //GET
    HttpResponse response = httpGET(baseURL + "/People('kristakemp')/Friends/$ref", 200);
    JsonNode node = getJSONNode(response);

    assertTrue(node.get("value").isArray());
    assertEquals("/People('genevievereeves')", ((ArrayNode)node.get("value")).get(0).get("@odata.id").asText());
    assertNull(((ArrayNode)node.get("value")).get(1));

    //ADD
    String payload = "{\n" +
        "\"@odata.id\": \"/People('scottketchum')\"\n" +
        "}";
    
    HttpPost postRequest = new HttpPost(baseURL + "/People('kristakemp')/Friends/$ref");
    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    response = httpSend(postRequest, 204);
    
    //GET
    response = httpGET(baseURL + "/People('kristakemp')/Friends/$ref", 200);
    node = getJSONNode(response);

    assertTrue(node.get("value").isArray());
    assertEquals("/People('genevievereeves')", ((ArrayNode)node.get("value")).get(0).get("@odata.id").asText());
    assertEquals("/People('scottketchum')", ((ArrayNode)node.get("value")).get(1).get("@odata.id").asText());
  }


  @Test
  public void testEntityId() throws Exception {
    HttpResponse response = httpGET(baseURL+"/$entity?$id="+baseURL + "/People('kristakemp')", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People/$entity", node.get("@odata.context").asText());
    assertEquals("kristakemp", node.get("UserName").asText());

    // using relative URL
    response = httpGET(baseURL+"/$entity?$id="+"People('kristakemp')", 200);
    node = getJSONNode(response);
    assertEquals("$metadata#People/$entity", node.get("@odata.context").asText());
    assertEquals("kristakemp", node.get("UserName").asText());
  }

  @Test
  public void testCreateReadDeleteEntity() throws Exception {
    String payload = "{\n" +
        "         \"UserName\":\"olingodude\",\n" +
        "         \"FirstName\":\"Olingo\",\n" +
        "         \"LastName\":\"Apache\",\n" +
        "         \"Emails\":[\n" +
        "            \"olingo@apache.org\"\n" +
        "         ],\n" +
        "         \"AddressInfo\":[\n" +
        "            {\n" +
        "               \"Address\":\"100 apache Ln.\",\n" +
        "               \"City\":{\n" +
        "                  \"CountryRegion\":\"United States\",\n" +
        "                  \"Name\":\"Boise\",\n" +
        "                  \"Region\":\"ID\"\n" +
        "               }\n" +
        "            }\n" +
        "         ],\n" +
        "         \"Gender\":\"0\",\n" +
        "         \"Concurrency\":635585295719432047\n" +
        "}";
    HttpPost postRequest = new HttpPost(baseURL + "/People");
    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    postRequest.addHeader("Prefer", "return=minimal");

    HttpResponse response = httpSend(postRequest, 204);
    // the below would be 204, if minimal was not supplied
    assertEquals(baseURL +"/People('olingodude')", getHeader(response, "Location"));
    assertEquals("return=minimal", getHeader(response, "Preference-Applied"));

    String location = getHeader(response, "Location");
    response = httpGET(location, 200);
    EntityUtils.consumeQuietly(response.getEntity());

    HttpDelete deleteRequest = new HttpDelete(location);
    response = httpSend(deleteRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());

    response = httpGET(location, 404);
    EntityUtils.consumeQuietly(response.getEntity());
  }


  @Test
  public void testCreateEntityWithLinkToRelatedEntities() throws Exception {
    String payload = "{\n" +
        "         \"UserName\":\"olingo\",\n" +
        "         \"FirstName\":\"Olingo\",\n" +
        "         \"LastName\":\"Apache\",\n" +
        "         \"Emails\":[\n" +
        "            \"olingo@apache.org\"\n" +
        "         ],\n" +
        "         \"AddressInfo\":[\n" +
        "            {\n" +
        "               \"Address\":\"100 apache Ln.\",\n" +
        "               \"City\":{\n" +
        "                  \"CountryRegion\":\"United States\",\n" +
        "                  \"Name\":\"Boise\",\n" +
        "                  \"Region\":\"ID\"\n" +
        "               }\n" +
        "            }\n" +
        "         ],\n" +
        "         \"Gender\":\"0\",\n" +
        "         \"Concurrency\":635585295719432047,\n" +
        "\"Friends@odata.bind\":[\"" +
         baseURL+"/People('russellwhyte')\",\""+
         baseURL+"/People('scottketchum')\""+
        "]"+
        "}";
    HttpPost postRequest = new HttpPost(baseURL + "/People");
    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    postRequest.setHeader("Prefer", "return=minimal");
    HttpResponse response = httpSend(postRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());
    
    response = httpGET(baseURL+"/People('olingo')/Friends", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People", node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("scottketchum", ((ArrayNode)node.get("value")).get(1).get("UserName").asText());
  }

  @Test
  public void testUpdatePrimitiveProperty() throws Exception {
    String payload = "{"
        + " \"value\":\"Pilar Ackerman\""
        + "}";

    String editUrl = baseURL + "/People('russellwhyte')/FirstName";
    HttpPut postRequest = new HttpPut(editUrl);
    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    HttpResponse response = httpSend(postRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());
    
    response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/FirstName", node.get("@odata.context").asText());
    assertEquals("Pilar Ackerman", node.get("value").asText());
  }

  @Test
  public void testUpdatePrimitiveArrayProperty() throws Exception {
    String payload = "{"
        + " \"value\": [\n" +
        "       \"olingo@apache.com\"\n" +
        "    ]"
        + "}";

    String editUrl = baseURL + "/People('russellwhyte')/Emails";
    HttpPut postRequest = new HttpPut(editUrl);
    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    HttpResponse response = httpSend(postRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());

    response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Emails", node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("olingo@apache.com", ((ArrayNode)node.get("value")).get(0).asText());
  }

  @Test
  public void testDeleteProperty() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/FirstName";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("Russell", node.get("value").asText());

    HttpDelete deleteRequest = new HttpDelete(editUrl);
    response = httpSend(deleteRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());
    
    response = httpGET(editUrl, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadNavigationPropertyEntityCollection() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Friends";
    HttpResponse response = httpGET(editUrl, 200);

    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People", node.get("@odata.context").asText());

    JsonNode person = ((ArrayNode)node.get("value")).get(0);
    assertEquals("scottketchum", person.get("UserName").asText());
  }

  @Test
  public void testReadNavigationPropertyNoContainsTarget() throws Exception {
    String editUrl = baseURL + "/People('scottketchum')/Photo";
    HttpResponse response = httpGET(editUrl, 200);

    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#Photos/$entity", node.get("@odata.context").asText());
  }
  
  @Test
  public void testReadNavigationPropertyNonExistingNavigation() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Foobar";
    httpGET(editUrl, 404);
  }  
  
  @Test
  public void testReadNavigationPropertyEntityCollection2() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Friends('scottketchum')/Trips";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Friends('scottketchum')/Trips",
        node.get("@odata.context").asText());
    assertTrue(node.get("value").isArray());
    assertEquals("1001", ((ArrayNode)node.get("value")).get(0).get("TripId").asText());
  }

  @Test
  public void testReadNavigationPropertyEntity() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Trips(1003)";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Trips/$entity",
        node.get("@odata.context").asText());
    assertEquals("f94e9116-8bdd-4dac-ab61-08438d0d9a71", node.get("ShareId").asText());
  }

  @Test
  public void testReadNavigationPropertyEntityNotExisting() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Trips(9999)";
    HttpResponse response = httpGET(editUrl, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadNavigationPropertyEntitySetNotExisting() throws Exception {
    String editUrl = baseURL + "/People('jhondoe')/Trips";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('jhondoe')/Trips",
        node.get("@odata.context").asText());
    assertEquals(0, ((ArrayNode)node.get("value")).size());
  }

  @Test
  public void testBadNavigationProperty() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Unknown";
    HttpResponse response = httpGET(editUrl, 404);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testReadNavigationPropertyEntityProperty() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Trips(1003)/PlanItems(5)/ConfirmationCode";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Trips(1003)/PlanItems(5)/ConfirmationCode",
        node.get("@odata.context").asText());
    assertEquals("JH58494", node.get("value").asText());
  }

  @Test
  public void testReadNavigationPropertyEntityMultipleDerivedTypes() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Trips(1003)/PlanItems";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Trips(1003)/PlanItems",
        node.get("@odata.context").asText());
    assertEquals("#Microsoft.OData.SampleService.Models.TripPin.Flight",
        ((ArrayNode) node.get("value")).get(0).get("@odata.type").asText());
  }

  @Test
  public void testReadNavigationPropertyEntityCoolectionDerivedFilter() throws Exception {
    String editUrl = baseURL
        + "/People('russellwhyte')/Trips(1003)/PlanItems/Microsoft.OData.SampleService.Models.TripPin.Event";
    HttpResponse response = httpGET(editUrl, 200);

    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Trips(1003)/PlanItems/"
        + "Microsoft.OData.SampleService.Models.TripPin.Event",
        node.get("@odata.context").asText());

    assertEquals("#Microsoft.OData.SampleService.Models.TripPin.Event",
        ((ArrayNode) node.get("value")).get(0).get("@odata.type").asText());
  }

  @Test
  public void testReadNavigationPropertyEntityDerivedFilter() throws Exception {
    String editUrl = baseURL+ "/People('russellwhyte')/Trips(1003)/PlanItems(56)/"
        + "Microsoft.OData.SampleService.Models.TripPin.Event";
    HttpResponse response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("$metadata#People('russellwhyte')/Trips(1003)/PlanItems/"
        + "Microsoft.OData.SampleService.Models.TripPin.Event/$entity",
        node.get("@odata.context").asText());
    assertEquals("#Microsoft.OData.SampleService.Models.TripPin.Event", node.get("@odata.type").asText());
    assertEquals("56", node.get("PlanItemId").asText());
  }

  @Test
  public void testUpdateReference() throws Exception {
    HttpResponse response = httpGET(baseURL+"/People('ronaldmundy')/Photo/$ref", 200);
    JsonNode node = getJSONNode(response);
    assertEquals("/Photos(12)", node.get("@odata.id").asText());

    String msg = "{\n" +
        "\"@odata.id\": \"/Photos(11)\"\n" +
        "}";
    String editUrl = baseURL + "/People('ronaldmundy')/Photo/$ref";
    HttpPut putRequest = new HttpPut(editUrl);
    putRequest.setEntity(new StringEntity(msg, ContentType.APPLICATION_JSON));
    putRequest.setHeader("Content-Type", "application/json;odata.metadata=minimal");
    response = httpSend(putRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());

    response = httpGET(baseURL+"/People('ronaldmundy')/Photo/$ref", 200);
    node = getJSONNode(response);
    assertEquals("/Photos(11)", node.get("@odata.id").asText());
  }

  @Test
  public void testAddDelete2ReferenceCollection() throws Exception {
    // add
    String msg = "{\n" +
        "\"@odata.id\": \"/People('russellwhyte')\"\n" +
        "}";
    String editUrl = baseURL + "/People('vincentcalabrese')/Friends/$ref";
    HttpPost postRequest = new HttpPost(editUrl);
    postRequest.setEntity(new StringEntity(msg, ContentType.APPLICATION_JSON));
    postRequest.addHeader("Content-Type", "application/json;odata.metadata=minimal");
    HttpResponse response = httpSend(postRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());
    
    // get
    response = httpGET(editUrl, 200);
    JsonNode node = getJSONNode(response);
    assertEquals("/People('russellwhyte')",
        ((ArrayNode) node.get("value")).get(2).get("@odata.id").asText());

    //delete
    HttpDelete deleteRequest = new HttpDelete(editUrl+"?$id="+baseURL+"/People('russellwhyte')");
    deleteRequest.addHeader("Content-Type", "application/json;odata.metadata=minimal");
    response = httpSend(deleteRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());

    // get
    response = httpGET(editUrl, 200);
    node = getJSONNode(response);
    assertNull("/People('russellwhyte')", ((ArrayNode) node.get("value")).get(2));
  }

  @Test
  public void testDeleteReference() throws Exception {
    String editUrl = baseURL + "/People('russellwhyte')/Photo/$ref";
    HttpResponse response = httpGET(editUrl, 200);
    EntityUtils.consumeQuietly(response.getEntity());
    
    HttpDelete deleteRequest = new HttpDelete(editUrl);
    response = httpSend(deleteRequest, 204);
    EntityUtils.consumeQuietly(response.getEntity());
    
    response = httpGET(editUrl, 204);
    EntityUtils.consumeQuietly(response.getEntity());
  }

  @Test
  public void testCrossJoin() throws Exception {
    String editUrl = baseURL + "/$crossjoin(People,Airlines)";
    HttpResponse response = httpGET(editUrl, 200);
    EntityUtils.consumeQuietly(response.getEntity());
  }
}
