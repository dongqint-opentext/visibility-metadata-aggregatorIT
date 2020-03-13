/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opentext.bn.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.NoWrappingJsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.HttpActionBuilder;
import com.consol.citrus.dsl.builder.HttpServerActionBuilder;
import com.consol.citrus.dsl.builder.HttpServerActionBuilder.HttpServerReceiveActionBuilder;
import com.consol.citrus.dsl.builder.HttpServerActionBuilder.HttpServerSendActionBuilder;
import com.consol.citrus.dsl.builder.HttpServerRequestActionBuilder;
import com.consol.citrus.dsl.builder.HttpServerResponseActionBuilder;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.kafka.endpoint.KafkaEndpoint;
import com.consol.citrus.kafka.message.KafkaMessage;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.callback.AbstractValidationCallback;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opentext.bn.content.cmd.models.CmdConstants;
import com.opentext.bn.content.lens.models.Token;
import com.opentext.bn.content.lens.models.Transactions;
import com.opentext.bn.content.lens.models.Payload;
import com.opentext.bn.content.lens.models.Payloads;
import com.opentext.bn.content.lens.models.Process;
import com.opentext.bn.content.lens.models.Processes;
import com.opentext.bn.content.lens.models.Step;
import com.opentext.bn.converters.avro.entity.ContentErrorEvent;
import com.opentext.bn.converters.avro.entity.DeliveryCompletedEvent;
import com.opentext.bn.converters.avro.entity.DeliveryErrorEvent;
import com.opentext.bn.converters.avro.entity.DeliveryReadyForPickupEvent;
import com.opentext.bn.converters.avro.entity.DocumentEvent;
import com.opentext.bn.converters.avro.entity.EnvelopeEvent;
import com.opentext.bn.converters.avro.entity.FileIntrospectedEvent;
import com.opentext.bn.converters.avro.entity.ReceiveCompletedEvent;
import com.opentext.bn.converters.avro.entity.ReceiveErrorEvent;
import com.opentext.bn.converters.avro.entity.TransactionContext;

public class VisibilityMetadataAggregatorIT extends TestNGCitrusTestRunner {

	@Autowired
	@Qualifier("eventInjectorClient")
	private HttpClient eventInjectorClient;

	@Autowired
	@Qualifier("cmdClient")
	private HttpClient cmdClient;

	@Autowired
	@Qualifier("lensServer1")
	private HttpServer lensServer;

	@Autowired
	@Qualifier("citrusKafkaEndpoint")
	private KafkaEndpoint citrusKafkaEndpoint;

	@Autowired
	@Qualifier("receiveCompletedKafkaEndpoint")
	private KafkaEndpoint receiveCompletedKafkaEndpoint;
	
	@Autowired
	@Qualifier("receiveErrorKafkaEndpoint")
	private KafkaEndpoint receiveErrorKafkaEndpoint;

	@Autowired
	@Qualifier("deliverycompletedKafkaEndpoint")
	private KafkaEndpoint deliverycompletedKafkaEndpoint;
	
	@Autowired
	@Qualifier("deliveryErrorKafkaEndpoint")
	private KafkaEndpoint deliveryErrorKafkaEndpoint;
	
	@Autowired
	@Qualifier("deliveryReadyForPickupKafkaEndpoint")
	private KafkaEndpoint deliveryReadyForPickupKafkaEndpoint;

	@Autowired
	@Qualifier("documentKafkaEndpoint")
	private KafkaEndpoint documentKafkaEndpoint;

	@Autowired
	@Qualifier("envelopeKafkaEndpoint")
	private KafkaEndpoint envelopeKafkaEndpoint;

	@Autowired
	@Qualifier("fileKafkaEndpoint")
	private KafkaEndpoint fileKafkaEndpoint;

	@Autowired
	@Qualifier("contentErrorKafkacEndpoint")
	private KafkaEndpoint contentErrorKafkacEndpoint;

	@Autowired
	@Qualifier("cmdResponseAdapter")
	private EndpointAdapter cmdResponseAdapter;

	@Test
	@CitrusTest
	public void process_ReceiveCompletedIT() {

		try {
			String controlFile = "src/test/resources/testfiles/controlFile2.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String receiveCompletedJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			ReceiveCompletedEvent receiveCompleteEvent = mapper.readValue(receiveCompletedJsonString,
					ReceiveCompletedEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint)
				.message(new KafkaMessage(receiveCompleteEvent).topic("visibility.platform.receivecompleted")
						.messageKey(receiveCompleteEvent.getTransactionContext().getTransactionId()));

			});

			//sleep(30000);

			httpLensServerTokenAction();
			//httpLensServerTokenAction();
			//httpLensServerTokenAction();
			String processFile = "src/test/resources/testfiles/lensProcesses.txt";
			httpLensServerProcessesAction(processFile);
			
			String fileName = "src/test/resources/testfiles/lensTransactions.txt";
			httpLensServerTransactionsAction(fileName);
			httpLensServerPayloadsAction();
			
			//httpLensServerAction();

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(receiveCompletedKafkaEndpoint).validationCallback(
						new JsonMappingValidationCallback<ReceiveCompletedEvent>(ReceiveCompletedEvent.class, mapper) {

							@Override
							public void validate(ReceiveCompletedEvent payload, Map<String, Object> headers,
									TestContext context) {
								String receiveCompletedResponse = null;
								receiveCompletedResponse = convertToJson(payload);
								echo("Payload received - receiveCompletedResponse : " + receiveCompletedResponse);

								try {
									JSONAssert.assertEquals(receiveCompletedJsonString, receiveCompletedResponse,
											JSONCompareMode.LENIENT);
								} catch (JSONException e) {
									e.printStackTrace();
									throw new ValidationException("this test failed : {} ", e);
								}

								Assert.assertNotNull(payload);
								// Assert.assertNotNull("missing transaction Id in the payload: ",
								// payload.getTransactionId());
								Assert.assertNotNull("missing citrus_kafka_messageKey: ",
										String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")),
										transactionId, "transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(receiveCompleteEvent, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"ReceiveCompletedEvent properies not matched: " + resultStr);
							}

						});

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}
	
	@Test
	@CitrusTest
	public void process_ReceiveErrorIT() {

		try {
			String controlFile = "src/test/resources/testfiles/receiveError.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String receiveErrorJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			ReceiveErrorEvent receiveErrorEvent = mapper.readValue(receiveErrorJsonString, ReceiveErrorEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint)
				.message(new KafkaMessage(receiveErrorEvent).topic("visibility.platform.receiveerror")
						.messageKey(transactionId));

			});

			//sleep(30000);
			
			httpLensServerTokenAction();
			//httpLensServerTokenAction();
			//httpLensServerTokenAction();
			String processFile = "src/test/resources/testfiles/lensProcesses_for_receive_error.txt";
			httpLensServerProcessesAction(processFile);
			String fileName = "src/test/resources/testfiles/lensTransaction_for_receive_error.txt";
			httpLensServerTransactionsAction(fileName);
			httpLensServerPayloadsAction();

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(receiveErrorKafkaEndpoint).validationCallback(
						new JsonMappingValidationCallback<ReceiveErrorEvent>(ReceiveErrorEvent.class, mapper) {

							@Override
							public void validate(ReceiveErrorEvent payload, Map<String, Object> headers, TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing citrus_kafka_messageKey: ", String.valueOf(headers.get("citrus_kafka_messageKey")));
								//Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")), transactionId, "transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(receiveErrorEvent, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"ReceiveErrorEvent properies not matched: " + resultStr);
							}

						});

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}
	
	@Test
	@CitrusTest
	public void process_DeliveryCompletedIT() {

		try {
			String controlFile = "src/test/resources/testfiles/deliveryCompleted.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String deliveryCompletedJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			DeliveryCompletedEvent deliveryCompletedEvent = mapper.readValue(deliveryCompletedJsonString, DeliveryCompletedEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint)
				.message(new KafkaMessage(deliveryCompletedEvent).topic("visibility.platform.deliverycompleted")
						.messageKey(deliveryCompletedEvent.getTransactionContext().getTransactionId()));

			});

			//sleep(30000);

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(deliverycompletedKafkaEndpoint).validationCallback(
						new JsonMappingValidationCallback<DeliveryCompletedEvent>(DeliveryCompletedEvent.class, mapper) {

							@Override
							public void validate(DeliveryCompletedEvent payload, Map<String, Object> headers, TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing citrus_kafka_messageKey: ", String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")), transactionId, "transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(deliveryCompletedEvent, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"DeliveryCompletedEvent properies not matched: " + resultStr);
							}

						});

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}
	
	
	@Test
	@CitrusTest
	public void process_DeliveryErrorIT() {

		try {
			String controlFile = "src/test/resources/testfiles/deliveryError.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String deliveryErrorJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			DeliveryErrorEvent deliveryErrorEvent = mapper.readValue(deliveryErrorJsonString, DeliveryErrorEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint)
				.message(new KafkaMessage(deliveryErrorEvent).topic("visibility.platform.deliveryerror")
						.messageKey(deliveryErrorEvent.getTransactionContext().getTransactionId()));

			});

			//sleep(30000);

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(deliveryErrorKafkaEndpoint).validationCallback(
						new JsonMappingValidationCallback<DeliveryErrorEvent>(DeliveryErrorEvent.class, mapper) {

							@Override
							public void validate(DeliveryErrorEvent payload, Map<String, Object> headers, TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing citrus_kafka_messageKey: ", String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")), transactionId, "transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(deliveryErrorEvent, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"DeliveryErrorEvent properies not matched: " + resultStr);
							}

						});

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}
	
	@Test
	@CitrusTest
	public void process_DeliveryReadyForPickupIT() {

		try {
			String controlFile = "src/test/resources/testfiles/deliveryError.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String deliveryReadyForPickupJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			DeliveryReadyForPickupEvent deliveryErrorEvent = mapper.readValue(deliveryReadyForPickupJsonString, DeliveryReadyForPickupEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint)
				.message(new KafkaMessage(deliveryErrorEvent).topic("visibility.platform.deliveryreadyforpickup")
						.messageKey(deliveryErrorEvent.getTransactionContext().getTransactionId()));

			});

			//sleep(30000);

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(deliveryReadyForPickupKafkaEndpoint).validationCallback(
						new JsonMappingValidationCallback<DeliveryReadyForPickupEvent>(DeliveryReadyForPickupEvent.class, mapper) {

							@Override
							public void validate(DeliveryReadyForPickupEvent payload, Map<String, Object> headers, TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing citrus_kafka_messageKey: ", String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")), transactionId, "transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(deliveryErrorEvent, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"DeliveryReadyForPickupEvent properies not matched: " + resultStr);
							}

						});

			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}
	
	
	
	
	
	private void httpLensServerTokenAction() {
		echo("Start Receive Lens Server for Token");
		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer) 
				.receive()
				.post()
				.contentType("application/json;charset=UTF-8") 
				.accept("application/json")
				.selector(Collections.singletonMap(HttpMessageHeaders.HTTP_REQUEST_URI, "/api/1/token"))
				.extractFromHeader("citrus_http_request_uri", "receive_citrus_http_request_uri")
				);

		echo("Token_receive_citrus_http_request_uri - ${receive_citrus_http_request_uri}");
		echo("Stop Receive Lens Server for Token");
		echo("Start Send Lens Server for Token");
		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer)
				.send()		
				.response(HttpStatus.OK) 
				.payload(TestHelper.getLensAccessToken())
				.contentType("application/json;charset=UTF-8")	
				//.extractFromHeader("citrus_http_request_uri", "send_citrus_http_request_uri")
				);
		echo("Stop Send Lens Server for Token");
		//echo("Token_send_citrus_http_request_uri - ${send_citrus_http_request_uri}");
	}
	
	private void httpLensServerProcessesAction(String fileName) {

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer) 
				.receive()
				.post()
				.contentType("application/json;charset=UTF-8") 
				.accept("application/json")
				.selector(Collections.singletonMap(HttpMessageHeaders.HTTP_REQUEST_URI, "/api/1/processes"))
				.extractFromHeader("citrus_http_request_uri", "receive_citrus_http_request_uri")
				.validationCallback(processesValidationCallback(fileName))
				);

		echo("Processes_receive_citrus_http_request_uri - ${receive_citrus_http_request_uri}");

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer)
				.send()		
				.response(HttpStatus.OK) 
				.contentType("application/json;charset=UTF-8")	
				);		
	}
	
	private AbstractValidationCallback<String> processesValidationCallback(String fileName) {
        return new AbstractValidationCallback<String>() {
            @Override
            public void validate(final String payload, final Map<String, Object> headers, final TestContext context) {
            	Assert.assertNotNull(payload);

            	String authorizationFromHeader = (String)headers.get("Authorization");
            	String authorizationExpected = TestHelper.getLensAuthorizationFromToken();
            	System.out.println("processesValidationCallback - payload: " + payload);
            	System.out.println("processesValidationCallback - Authorization: " + authorizationFromHeader + ", " + authorizationExpected);
            	            	
            	Processes processes = null;				
            	try {
					ObjectMapper mapper = new ObjectMapper();
		           	processes = mapper.readValue(payload, Processes.class);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i = 0; i < processes.getProcesses().size(); i++) {
					Process processPayload = processes.getProcesses().get(i);
					Process processExpected = TestHelper.getLensProcess(fileName, i);
					
	            	Assert.assertNotNull(processPayload);
	             	Assert.assertNotNull(processExpected);
	            	Assert.assertEquals(String.valueOf(headers.get("Authorization")), TestHelper.getLensAuthorizationFromToken(), "Authorization is not matching with Token response");
	               	                
	                // Check not null or empty for Process
	                Assert.assertNotNull(processPayload.getTransactionId());
	            	Assert.assertNotNull(processPayload.getDate());
	            	Assert.assertNotNull(processPayload.getExpires());
	            	Assert.assertNotNull(processPayload.getVersion());
	            	//Assert.assertNotNull(processPayload.getSteps());
	                
	            	// Compare all properties for Process except ...
	            	List<String> efProcess = new ArrayList<String>(); 
					efProcess.add("transactionId"); 
					efProcess.add("date");
					efProcess.add("expires"); 
					efProcess.add("version"); 
					efProcess.add("steps"); 
					String resultStrProcess = TestHelper.haveSamePropertyValuesExcludeFields(processPayload, processExpected, efProcess);

					echo("Validation Result - Lens Process: " + resultStrProcess.toString()); 
					Boolean resultProcess =  resultStrProcess.isEmpty()? true: false;
					Assert.assertEquals(Boolean.TRUE.toString(), resultProcess.toString(), "Lens UI Process properies not matched: " + resultStrProcess);
					
					for(int j = 0; j < processPayload.getSteps().size(); j++) {
						Step stepPayload = processPayload.getSteps().get(j);
						Step stepExpected = processExpected.getSteps().get(j);
						
						Assert.assertNotNull(stepPayload.getDate());	
						Assert.assertNotNull(stepPayload.getId());	
						Assert.assertNotNull(stepPayload.getMetadata());	
						// Compare all properties for Steps except ...
		            	List<String> efStep = new ArrayList<String>(); 
						efStep.add("date");
						efStep.add("id"); 
						efStep.add("metadata"); 
						String resultStrStep = TestHelper.haveSamePropertyValuesExcludeFields(stepPayload, stepExpected, efStep);

						echo("Validation Result - Lens Process Step: " + resultStrStep.toString()); 
						Boolean resultStep =  resultStrStep.isEmpty()? true: false;
						Assert.assertEquals(Boolean.TRUE.toString(), resultStep.toString(), "Lens UI Process properies not matched: " + resultStrStep);
						
						// Compare MetaData
		            	Assert.assertEquals(processPayload.getSteps().get(0).getMetadata().get("protocol"), processExpected.getSteps().get(0).getMetadata().get("protocol"), "Step metadat protocol is not matching");
		            	Assert.assertEquals(processPayload.getSteps().get(0).getMetadata().get("deliveryReceipt"), processExpected.getSteps().get(0).getMetadata().get("deliveryReceipt"), "Step metadat deliveryReceipt is not matching");
		            	Assert.assertEquals(processPayload.getSteps().get(0).getMetadata().get("remoteHost"), processExpected.getSteps().get(0).getMetadata().get("remoteHost"), "Step metadat remoteHost is not matching");
		            	Assert.assertEquals(processPayload.getSteps().get(0).getMetadata().get("serviceName"), processExpected.getSteps().get(0).getMetadata().get("serviceName"), "Step metadat serviceName is not matching");

					}
				}
             }
        };
    }
	
	private void httpLensServerTransactionsAction(String fileName) {

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer) 
				.receive()
				.post()
				.contentType("application/json;charset=UTF-8") 
				.accept("application/json")
				.selector(Collections.singletonMap(HttpMessageHeaders.HTTP_REQUEST_URI, "/api/1/transactions"))
				.extractFromHeader("citrus_http_request_uri", "receive_citrus_http_request_uri")
				.validationCallback(transactionsValidationCallback(fileName))
				);

		echo("Transactions_receive_citrus_http_request_uri - ${receive_citrus_http_request_uri}");

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer)
				.send()		
				.response(HttpStatus.OK) 
				.contentType("application/json;charset=UTF-8")
				);
	}
	
	private AbstractValidationCallback<String> transactionsValidationCallback(String fileName) {
        return new AbstractValidationCallback<String>() {
            @Override
            public void validate(final String payload, final Map<String, Object> headers, final TestContext context) {
            	Assert.assertNotNull(payload);

            	String authorizationFromHeader = (String)headers.get("Authorization");
            	String authorizationExpected = TestHelper.getLensAuthorizationFromToken();
            	System.out.println("transactionsValidationCallback - payload: " + payload);
            	System.out.println("transactionsValidationCallback - Authorization: " + authorizationFromHeader + ", " + authorizationExpected);
            	            	
            	Transactions transactions = null;				
            	try {
					ObjectMapper mapper = new ObjectMapper();
					transactions = mapper.readValue(payload, Transactions.class);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i = 0; i < transactions.getMessages().size(); i++) {
					com.opentext.bn.content.lens.models.Message transactionPayload = transactions.getMessages().get(i);
					com.opentext.bn.content.lens.models.Message transactionExpected = TestHelper.getLensTransactionMessage(fileName, i);
					
	            	Assert.assertNotNull(transactionPayload);
	             	Assert.assertNotNull(transactionExpected);
	            	Assert.assertEquals(String.valueOf(headers.get("Authorization")), TestHelper.getLensAuthorizationFromToken(), "Authorization is not matching with Token response");
	            	
	            	// Check not null or empty for Transaction
	                Assert.assertNotNull(transactionPayload.getId());
	            	Assert.assertNotNull(transactionPayload.getPublishedtime());
	            	Assert.assertNotNull(transactionPayload.getVersion());
	            	Assert.assertNotNull(transactionPayload.getSendermsgid());
	            	Assert.assertNotNull(transactionPayload.getAdditionalinformation());
	            	Assert.assertNotNull(transactionPayload.getRelatedmsg());
	                
	            	// Compare all properties for Process except ...
	            	List<String> efTransaction = new ArrayList<String>(); 
					efTransaction.add("id"); 
					efTransaction.add("publishedtime");
					efTransaction.add("arrivaltime"); 
					efTransaction.add("version"); 
					efTransaction.add("sendermsgid");
					efTransaction.add("statusdate");
					efTransaction.add("relatedmsg");
					efTransaction.add("additionalinformation");
					String resultStrTransaction = TestHelper.haveSamePropertyValuesExcludeFields(transactionPayload, transactionExpected, efTransaction);

					echo("Validation Result - Lens Transaction: " + resultStrTransaction.toString()); 
					Boolean resultProcess =  resultStrTransaction.isEmpty()? true: false;
					Assert.assertEquals(Boolean.TRUE.toString(), resultProcess.toString(), "Lens UI Transaction properies not matched: " + resultStrTransaction);
					
				}
             }
        };
    }
	
	private void httpLensServerPayloadsAction() {

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer) 
				.receive()
				.post()
				.contentType("application/json;charset=UTF-8") 
				.accept("application/json")
				.selector(Collections.singletonMap(HttpMessageHeaders.HTTP_REQUEST_URI, "/api/1/payloads"))
				.extractFromHeader("citrus_http_request_uri", "receive_citrus_http_request_uri")
				.validationCallback(payloadsValidationCallback())
				);

		echo("Payloads_receive_citrus_http_request_uri - ${receive_citrus_http_request_uri}");

		http(httpActionBuilder -> httpActionBuilder 
				.server(lensServer)
				.send()		
				.response(HttpStatus.OK) 
				.contentType("application/json;charset=UTF-8")	
				);
	}

	
	private AbstractValidationCallback<String> payloadsValidationCallback() {
        return new AbstractValidationCallback<String>() {
            @Override
            public void validate(final String payload, final Map<String, Object> headers, final TestContext context) {
            	Assert.assertNotNull(payload);

            	String authorizationFromHeader = (String)headers.get("Authorization");
            	String authorizationExpected = TestHelper.getLensAuthorizationFromToken();
            	System.out.println("payloadsValidationCallback - payload: " + payload);
            	System.out.println("payloadsValidationCallback - Authorization: " + authorizationFromHeader + ", " + authorizationExpected);
            	            	
            	Payloads payloads = null;				
            	try {
					ObjectMapper mapper = new ObjectMapper();
		           	payloads = mapper.readValue(payload, Payloads.class);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i = 0; i < payloads.getPayloads().size(); i++) {
					Payload processPayload = payloads.getPayloads().get(i);
					Payload processExpected = TestHelper.getLensPayload(i);
					
	            	Assert.assertNotNull(processPayload);
	             	Assert.assertNotNull(processExpected);
	            	Assert.assertEquals(String.valueOf(headers.get("Authorization")), TestHelper.getLensAuthorizationFromToken(), "Authorization is not matching with Token response");
	            		            	
	            	// Check not null or empty for Payload
	                Assert.assertNotNull(processPayload.getTransactionId());
	            	Assert.assertNotNull(processPayload.getSize());
	            	Assert.assertNotNull(processPayload.getDate());
	            	Assert.assertNotNull(processPayload.getExpireDate());
	            	Assert.assertNotNull(processPayload.getCallbackURI());
	                
	            	// Compare all properties for Process except ...
	            	List<String> efTransaction = new ArrayList<String>(); 
					efTransaction.add("transactionId"); 
					efTransaction.add("size");
					efTransaction.add("callbackURI"); 
					efTransaction.add("date"); 
					efTransaction.add("expireDate");
					String resultStrTransaction = TestHelper.haveSamePropertyValuesExcludeFields(processPayload, processExpected, efTransaction);

					echo("Validation Result - Lens Payload: " + resultStrTransaction.toString()); 
					Boolean resultProcess =  resultStrTransaction.isEmpty()? true: false;
					Assert.assertEquals(Boolean.TRUE.toString(), resultProcess.toString(), "Lens UI Payload properies not matched: " + resultStrTransaction);
	            	Assert.assertEquals(processPayload.getName(), processExpected.getName(), "Lens Payload name is not matching");
				}
             }
        };
    }

	@Test
	@CitrusTest
	public void process_CMDRestCallIT() {

		try {
			String controlFile = "src/test/resources/testfiles/controlFile2.txt";
			String eventJsonString = new String(Files.readAllBytes(Paths.get(controlFile)));

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);
			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			String receiveCompletedJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			ObjectMapper mapper = new ObjectMapper();
			ReceiveCompletedEvent receiveCompleteEvent = mapper.readValue(receiveCompletedJsonString,
					ReceiveCompletedEvent.class);

			TransactionContext transactionContext = receiveCompleteEvent.getTransactionContext();
			String receiverInfo = transactionContext.getReceiverAddress();
			String senderInfo = transactionContext.getSenderAddress();
			int receiverIndex = receiverInfo.indexOf(":");
			int senderIndex = senderInfo.indexOf(":");
			String senderQualifier = senderInfo.substring(0, senderIndex);
			String senderAddress = senderInfo.substring(senderIndex + 1);
			String receiverQualifier = receiverInfo.substring(0, receiverIndex);
			String receiverAddress = receiverInfo.substring(receiverIndex + 1);
			echo("receiverAddress: " + receiverAddress + ", receiverQualifier: " + receiverQualifier
					+ ", senderAddress: " + senderAddress + ", senderQualifier: " + senderQualifier);

			// CMD REST Call 1 - buid Look Up By Addr
			// http://qtotcra.qa.gxsonline.net:8080/communitymasterdata/rest/v1/resolver/rootParentsByAddresses?senderAddress=ADHUBMDCS&senderQualifier=MS&receiverAddress=ADPARTMDCS&receiverQualifier=MS
			echo("Start CMD Call 1 - buid Look Up By Addr");

			String buidLookUpByAddr = "/resolver/rootParentsByAddresses?senderAddress=" + senderAddress
					+ "&senderQualifier=" + senderQualifier + "&receiverAddress=" + receiverAddress
					+ "&receiverQualifier=" + receiverQualifier;
			String senderAddressBuIdExpected = "GC22698817YV";
			String receiverAddressBuIdExpected = "GC28464997QM";

			http(httpActionBuilder -> httpActionBuilder.client(cmdClient).send().get(buidLookUpByAddr)
					.header(CmdConstants.IM_PRINCIPAL_TYPE, CmdConstants.CMD_PRINCIPAL_TYPE)
					.header(CmdConstants.IM_COMMUNITY_ID, CmdConstants.CMD_COMMUNITY_ID)
					.header(CmdConstants.IM_SERVICE_INSTANCE_ID, CmdConstants.CMD_INSTANCE_ID)
					.accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json"));

			http(httpActionBuilder -> httpActionBuilder.client(cmdClient).receive().response(HttpStatus.OK)
					.messageType(MessageType.JSON)
					.extractFromPayload("$.senderAddressBuId", "senderAddressBuIdReceived")
					.extractFromPayload("$.receiverAddressBuId", "receiverAddressBuIdReceived")
					.validate("$.senderAddressBuId", senderAddressBuIdExpected)
					.validate("$.receiverAddressBuId", receiverAddressBuIdExpected));

			echo("senderAddressBuIdReceived - ${senderAddressBuIdReceived};   receiverAddressBuIdReceived - ${receiverAddressBuIdReceived} ");
			echo("Finish CMD Call 1 - buid Look Up By Addr");

			// CMD REST Call 2 - Company Name Lookup By Sender BUID
			// http://qtotcra.qa.gxsonline.net:8080/communitymasterdata/rest/v1/businessUnits/GC22698817YV
			echo("Start CMD Call 2 - Company Name Lookup By Sender BUID");

			String companyNameLookup = "/businessUnits/" + senderAddressBuIdExpected;
			String companyNameExpected = "AD-TGMSHUBCOMP";

			http(httpActionBuilder -> httpActionBuilder.client(cmdClient).send().get(companyNameLookup)
					.header(CmdConstants.IM_PRINCIPAL_TYPE, CmdConstants.CMD_PRINCIPAL_TYPE)
					.header(CmdConstants.IM_COMMUNITY_ID, CmdConstants.CMD_COMMUNITY_ID)
					.header(CmdConstants.IM_SERVICE_INSTANCE_ID, CmdConstants.CMD_INSTANCE_ID)
					.accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json"));

			http(httpActionBuilder -> httpActionBuilder.client(cmdClient).receive().response(HttpStatus.OK)
					.messageType(MessageType.JSON).extractFromPayload("$.name", "companyNameReceived")
					.validate("$.name", companyNameExpected));

			echo("companyNameReceived - ${companyNameReceived}");

			echo("Finish CMD Call 2 - Company Name Lookup By BUID");

			// CMD REST Call 3 - Company Name Lookup By Receiver BUID
			// http://qtotcra.qa.gxsonline.net:8080/communitymasterdata/rest/v1/businessUnits/GC22698817YV
			echo("Start CMD REST Call 3 - Company Name Lookup By Receiver BUID");

			String companyNameLookup2 = "/businessUnits/" + receiverAddressBuIdExpected;
			String companyNameExpected2 = "AD-TGMSPARTCOMP1";

			http(httpActionBuilder -> httpActionBuilder
					.client(cmdClient)
					.send()
					.get(companyNameLookup2)
					.header(CmdConstants.IM_PRINCIPAL_TYPE, CmdConstants.CMD_PRINCIPAL_TYPE)
					.header(CmdConstants.IM_COMMUNITY_ID, CmdConstants.CMD_COMMUNITY_ID)
					.header(CmdConstants.IM_SERVICE_INSTANCE_ID, CmdConstants.CMD_INSTANCE_ID)
					.accept(MediaType.APPLICATION_JSON_VALUE).contentType("application/json")
					);

			http(httpActionBuilder -> httpActionBuilder
					.client(cmdClient)
					.receive()
					.response(HttpStatus.OK)
					.messageType(MessageType.JSON)
					.extractFromPayload("$.name", "companyNameReceived2")
					.validate("$.name", companyNameExpected2)
					);

			echo("companyNameReceived2 - ${companyNameReceived2}");

			echo("Finish CMD REST Call 3 - Company Name Lookup By Receiver BUID");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ValidationException("this test failed : {} ", e);

		}

	}

	@Test
	@CitrusTest
	public void process_FileIntrospectedEventIT() {

		try {
			String testFilePath = "src/test/resources/testfiles/fileIntrospected.txt";

			String eventJsonString = new String(Files.readAllBytes(Paths.get(testFilePath)));
			System.out.println("fileIntrospectedEventJsonString 1: " + eventJsonString);

			String transactionId = UUID.randomUUID().toString();

			echo("transactionId: " + transactionId);

			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			eventJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			System.out.println("fileIntrospectedEventJsonString 2: " + eventJsonString);
			ObjectMapper mapper = new ObjectMapper();
			FileIntrospectedEvent envelope = mapper.readValue(eventJsonString, FileIntrospectedEvent.class);
			System.out.println("Envelope Send:");
			System.out.println(TestHelper.toString(envelope));
			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint).message(
						new KafkaMessage(envelope).topic("visibility.introspection.file").messageKey(transactionId));

			});
			
			httpLensServerTokenAction();
			String fileName = "src/test/resources/testfiles/lensTransaction_for_File.txt";
			httpLensServerTransactionsAction(fileName);

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(fileKafkaEndpoint).messageType(MessageType.BINARY)
				.message(new KafkaMessage(envelope).messageKey(transactionId)).validationCallback(
						new JsonMappingValidationCallback<FileIntrospectedEvent>(FileIntrospectedEvent.class,
								mapper) {

							@Override
							public void validate(FileIntrospectedEvent payload, Map<String, Object> headers,
									TestContext context) {
								System.out.println("Payload Receive:");
								System.out.println(TestHelper.toString(payload));
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing transaction Id in the payload: ",
										payload.getTransactionId());
								Assert.assertNotNull("missing citrus_kafka_messageKey: ",
										String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")),
										payload.getTransactionId(),
										"transactionid is not matching with kafka key");
								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(envelope, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"FileIntrospectedEvent properies not matched: " + resultStr);
							}
						});
			});

		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	@CitrusTest
	public void process_EnvelopeIntrospectedEventIT() {

		try {

			String testFilePath = "src/test/resources/testfiles/envelopeIntrospected.txt";

			String eventJsonString = new String(Files.readAllBytes(Paths.get(testFilePath)));
			System.out.println("envelopeIntrospectedEventJsonString 1: " + eventJsonString);

			String transactionId = UUID.randomUUID().toString();
			echo("transactionId: " + transactionId);

			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			eventJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			System.out.println("envelopeIntrospectedEventJsonString 2: " + eventJsonString);

			ObjectMapper mapper = new ObjectMapper();
			EnvelopeEvent envelope = mapper.readValue(eventJsonString, EnvelopeEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint).message(new KafkaMessage(envelope)
						.topic("visibility.introspection.envelope").messageKey(transactionId));

			});
			
			httpLensServerTokenAction();
			String fileName = "src/test/resources/testfiles/lensTransaction_for_File.txt";
			httpLensServerTransactionsAction(fileName);

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(envelopeKafkaEndpoint).messageType(MessageType.BINARY)
				.message(new KafkaMessage(envelope).messageKey(transactionId)).validationCallback(
						new JsonMappingValidationCallback<EnvelopeEvent>(EnvelopeEvent.class, mapper) {

							@Override
							public void validate(EnvelopeEvent payload, Map<String, Object> headers,
									TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing transaction Id in the payload: ",
										payload.getTransactionId());
								Assert.assertNotNull("missing citrus_kafka_messageKey: ",
										String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")),
										payload.getTransactionId(),
										"transactionid is not matching with kafka key");
								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(envelope, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"FileIntrospectedEvent properies not matched: " + resultStr);
							}
						});
			});

		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	@CitrusTest
	public void process_DocumentIntrospectedEventIT() {

		try {

			String testFilePath = "src/test/resources/testfiles/documentIntrospected.txt";

			String eventJsonString = new String(Files.readAllBytes(Paths.get(testFilePath)));
			System.out.println("documentIntrospectedEventJsonString 1: " + eventJsonString);

			String transactionId = UUID.randomUUID().toString();

			echo("transactionId: " + transactionId);

			String payloadId = "Q14E-201912000000000"
					+ UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
			echo("payloadId: " + payloadId);

			eventJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, payloadId);

			System.out.println("documentIntrospectedEventJsonString 2: " + eventJsonString);

			ObjectMapper mapper = new ObjectMapper();
			DocumentEvent envelope = mapper.readValue(eventJsonString, DocumentEvent.class);

			send(sendMessageBuilder -> {

				sendMessageBuilder.endpoint(citrusKafkaEndpoint).message(new KafkaMessage(envelope)
						.topic("visibility.introspection.document").messageKey(transactionId));

			});

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(documentKafkaEndpoint).messageType(MessageType.BINARY)
				.message(new KafkaMessage(envelope).messageKey(transactionId)).validationCallback(
						new JsonMappingValidationCallback<DocumentEvent>(DocumentEvent.class, mapper) {

							@Override
							public void validate(DocumentEvent payload, Map<String, Object> headers,
									TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing transaction Id in the payload: ",
										payload.getTransactionId());
								Assert.assertNotNull("missing citrus_kafka_messageKey: ",
										String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")),
										payload.getTransactionId(),
										"transactionid is not matching with kafka key");
								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(envelope, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"FileIntrospectedEvent properies not matched: " + resultStr);
							}
						});
			});
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	@CitrusTest
	public void process_ContentErrorEventIT() {

		try {

			String testFilePath = "src/test/resources/testfiles/contentError.txt";

			String eventJsonString = new String(Files.readAllBytes(Paths.get(testFilePath)));
			System.out.println("contentErrorEventJsonString 1: " + eventJsonString);

			String transactionId = UUID.randomUUID().toString();

			echo("transactionId: " + transactionId);

			eventJsonString = TestHelper.setVariableValues(eventJsonString, transactionId, "");

			System.out.println("contentErrorEventJsonString 2: " + eventJsonString);

			ObjectMapper mapper = new ObjectMapper();
			ContentErrorEvent envelope = mapper.readValue(eventJsonString, ContentErrorEvent.class);

			send(sendMessageBuilder -> {
				sendMessageBuilder.endpoint(citrusKafkaEndpoint).message(new KafkaMessage(envelope)
						.topic("visibility.introspection.contenterror").messageKey(transactionId));

			});

			receive(receiveMessageBuilder -> {
				receiveMessageBuilder.endpoint(contentErrorKafkacEndpoint).messageType(MessageType.BINARY)
				.message(new KafkaMessage(envelope).messageKey(transactionId)).validationCallback(
						new JsonMappingValidationCallback<ContentErrorEvent>(ContentErrorEvent.class, mapper) {

							@Override
							public void validate(ContentErrorEvent payload, Map<String, Object> headers,
									TestContext context) {
								Assert.assertNotNull(payload);
								Assert.assertNotNull("missing transaction Id in the payload: ",
										payload.getTransactionId());
								Assert.assertNotNull("missing citrus_kafka_messageKey: ",
										String.valueOf(headers.get("citrus_kafka_messageKey")));
								Assert.assertEquals(String.valueOf(headers.get("citrus_kafka_messageKey")),
										payload.getTransactionId(),
										"transactionid is not matching with kafka key");

								// Compare all properties
								String resultStr = TestHelper.haveSamePropertyValues(envelope, payload);
								echo("Validation Result: " + resultStr.toString());
								Boolean result = resultStr.isEmpty() ? true : false;
								Assert.assertEquals(Boolean.TRUE.toString(), result.toString(),
										"FileIntrospectedEvent properies not matched: " + resultStr);
							}
						});
			});

		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String writeJson(ReceiveCompletedEvent receiveEvent) throws IOException {

		abstract class IgnoreSchemaProperty {
			// You have to use the correct package for JsonIgnore,
			// fasterxml or codehaus
			@JsonIgnore
			abstract void getSchema();
		}

		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		om.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		om.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
		om.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		om.addMixIn(ReceiveCompletedEvent.class, IgnoreSchemaProperty.class);

		String controlFileString = om.writer().writeValueAsString(receiveEvent);

		return controlFileString;
	}

	public String convertToJson(ReceiveCompletedEvent record) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			NoWrappingJsonEncoder jsonEncoder = new NoWrappingJsonEncoder(record.getSchema(), outputStream);
			DatumWriter<GenericRecord> writer = record instanceof SpecificRecord
					? new SpecificDatumWriter<>(record.getSchema())
							: new GenericDatumWriter<>(record.getSchema());
					writer.write(record, jsonEncoder);
					jsonEncoder.flush();
					return outputStream.toString();
		} catch (IOException e) {
			throw new ValidationException("Failed to convert to JSON.", e);
		}
	}
}
