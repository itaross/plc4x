/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package org.apache.plc4x.java.opcua.connection;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.*;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.*;
import org.apache.plc4x.java.opcua.protocol.OpcuaField;
import org.apache.plc4x.java.opcua.protocol.OpcuaSubsriptionHandle;
import org.apache.plc4x.java.spi.messages.*;
import org.apache.plc4x.java.spi.messages.utils.ResponseItem;
import org.apache.plc4x.java.spi.model.SubscriptionPlcField;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.eclipse.milo.opcua.stack.core.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;

/**
 * Corresponding implementaion for a TCP-based connection for an OPC UA server.
 * TODO: At the moment are just connections without any security mechanism possible
 * <p>
 */
public class OpcuaTcpPlcConnection extends BaseOpcuaPlcConnection {

    private static final int OPCUA_DEFAULT_TCP_PORT = 4840;

    private static final Logger logger = LoggerFactory.getLogger(OpcuaTcpPlcConnection.class);
    private final AtomicLong clientHandles = new AtomicLong(1L);
    private InetAddress address;
    private int requestTimeout = 5000;
    private int port;
    private String params;
    private OpcUaClient client;
    private boolean isConnected = false;

    private OpcuaTcpPlcConnection(InetAddress address, String params, int requestTimeout) {
        this(address, OPCUA_DEFAULT_TCP_PORT, params, requestTimeout);
        logger.info("Configured OpcuaTcpPlcConnection with: host-name {}", address.getHostAddress());
    }

    private OpcuaTcpPlcConnection(InetAddress address, int port, String params, int requestTimeout) {
        this(params);
        logger.info("Configured OpcuaTcpPlcConnection with: host-name {}", address.getHostAddress());
        this.address = address;
        this.port = port;
        this.params = params;
        this.requestTimeout = requestTimeout;
    }

    private OpcuaTcpPlcConnection(String params) {
        super(getOptionString(params));
    }

    public static OpcuaTcpPlcConnection of(InetAddress address, String params, int requestTimeout) {
        return new OpcuaTcpPlcConnection(address, params, requestTimeout);
    }

    public static OpcuaTcpPlcConnection of(InetAddress address, int port, String params, int requestTimeout) {
        return new OpcuaTcpPlcConnection(address, port, params, requestTimeout);
    }

    public static PlcValue encodePlcValue(DataValue value) {
        NodeId typeNode = value.getValue().getDataType().get();
        Object objValue = value.getValue().getValue();

        if (typeNode.equals(Identifiers.Boolean)) {
            return new PlcBoolean((Boolean) objValue);
        /*} else if (typeNode.equals(Identifiers.ByteString)) {
            byte[] array = ((ByteString) objValue).bytes();
            Byte[] byteArry = new Byte[array.length];
            int counter = 0;
            for (byte bytie : array
            ) {
                byteArry[counter] = bytie;
                counter++;
            }
            return new DefaultByteArrayPlcValue(byteArry);*/
        } else if (typeNode.equals(Identifiers.Integer)) {
            return new PlcInteger((Integer) objValue);
        } else if (typeNode.equals(Identifiers.Int16)) {
            return new PlcInteger((Short) objValue);
        } else if (typeNode.equals(Identifiers.Int32)) {
            return new PlcInteger((Integer) objValue);
        } else if (typeNode.equals(Identifiers.Int64)) {
            return new PlcLong((Long) objValue);
        } else if (typeNode.equals(Identifiers.UInteger)) {
            return new PlcLong((Long) objValue);
        } else if (typeNode.equals(Identifiers.UInt16)) {
            return new PlcInteger(((UShort) objValue).intValue());
        } else if (typeNode.equals(Identifiers.UInt32)) {
            return new PlcLong(((UInteger) objValue).longValue());
        } else if (typeNode.equals(Identifiers.UInt64)) {
            return new PlcBigInteger(new BigInteger(objValue.toString()));
        } else if (typeNode.equals(Identifiers.Byte)) {
            return new PlcInteger(Short.valueOf(objValue.toString()));
        } else if (typeNode.equals(Identifiers.Float)) {
            return new PlcFloat((Float) objValue);
        } else if (typeNode.equals(Identifiers.Double)) {
            return new PlcDouble((Double) objValue);
        } else if (typeNode.equals(Identifiers.SByte)) {
            return new PlcInteger((Byte) objValue);
        } else {
            return new PlcString(objValue.toString());
        }

    }

    public InetAddress getRemoteAddress() {
        return address;
    }

    @Override
    public void connect() throws PlcConnectionException {
        List<EndpointDescription> endpoints = null;
        EndpointDescription endpoint = null;

        try {
            endpoints = DiscoveryClient.getEndpoints(getEndpointUrl(address, port, getSubPathOfParams(params))).get();
            //TODO Exception should be handeled better when the Discovery-API of Milo is stable
        } catch (Exception ex) {
            logger.info("Failed to discover Endpoint with enabled discovery. If the endpoint does not allow a correct discovery disable this option with the nDiscovery=true option. Failed Endpoint: {}", getEndpointUrl(address, port, params));

            // try the explicit discovery endpoint as well
            String discoveryUrl = getEndpointUrl(address, port, getSubPathOfParams(params));

            if (!discoveryUrl.endsWith("/")) {
                discoveryUrl += "/";
            }
            discoveryUrl += "discovery";

            logger.info("Trying explicit discovery URL: {}", discoveryUrl);
            try {
                endpoints = DiscoveryClient.getEndpoints(discoveryUrl).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PlcConnectionException("Unable to discover URL:" + discoveryUrl);
            } catch (ExecutionException e) {
                throw new PlcConnectionException("Unable to discover URL:" + discoveryUrl);
            }

        }
        endpoint = endpoints.stream()
            .filter(e -> e.getSecurityPolicyUri().equals(getSecurityPolicy().getUri()))
            .filter(endpointFilter())
            .findFirst()
            .orElseThrow(() -> new PlcConnectionException("No desired endpoints from"));

        if (this.skipDiscovery) {
            //ApplicationDescription applicationDescription = new ApplicationDescription();
            //endpoint = new EndpointDescription(address.getHostAddress(),applicationDescription , null, MessageSecurityMode.None, SecurityPolicy.None.getUri(), null , TransportProfile.TCP_UASC_UABINARY.getUri(), UByte.valueOf(0));// TODO hier machen wenn fertig
            ApplicationDescription currentAD = endpoint.getServer();
            ApplicationDescription withoutDiscoveryAD = new ApplicationDescription(
                currentAD.getApplicationUri(),
                currentAD.getProductUri(),
                currentAD.getApplicationName(),
                currentAD.getApplicationType(),
                currentAD.getGatewayServerUri(),
                currentAD.getDiscoveryProfileUri(),
                new String[0]);
            //try to replace the overhanded address
            //any error will result in the overhanded address of the client
            String newEndpointUrl = endpoint.getEndpointUrl(), prefix = "", suffix = "";
            String splitterPrefix = "://";
            String splitterSuffix = ":";
            String[] prefixSplit = newEndpointUrl.split(splitterPrefix);
            if (prefixSplit.length > 1) {
                String[] suffixSplit = prefixSplit[1].split(splitterSuffix);
                //reconstruct the uri
                newEndpointUrl = "";
                newEndpointUrl += prefixSplit[0] + splitterPrefix + address.getHostAddress();
                for (int suffixCounter = 1; suffixCounter < suffixSplit.length; suffixCounter++) {
                    newEndpointUrl += splitterSuffix + suffixSplit[suffixCounter];
                }
                // attach surounding prefix match
                for (int prefixCounter = 2; prefixCounter < prefixSplit.length; prefixCounter++) {
                    newEndpointUrl += splitterPrefix + prefixSplit[prefixCounter];
                }
            }

            EndpointDescription noDiscoverEndpoint = new EndpointDescription(
                newEndpointUrl,
                withoutDiscoveryAD,
                endpoint.getServerCertificate(),
                endpoint.getSecurityMode(),
                endpoint.getSecurityPolicyUri(),
                endpoint.getUserIdentityTokens(),
                endpoint.getTransportProfileUri(),
                endpoint.getSecurityLevel());
            endpoint = noDiscoverEndpoint;
        }


        OpcUaClientConfig config = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("eclipse milo opc-ua client of the apache PLC4X:PLC4J project"))
            .setApplicationUri("urn:eclipse:milo:plc4x:client")
            .setEndpoint(endpoint)
            .setIdentityProvider(getIdentityProvider())
            .setRequestTimeout(UInteger.valueOf(requestTimeout))
            .build();

        try {
            this.client = OpcUaClient.create(config);
            this.client.connect().get();
            isConnected = true;
        } catch (UaException e) {
            isConnected = false;
            String message = (config == null) ? "NULL" : config.toString();
            throw new PlcConnectionException("The given input values are a not valid OPC UA connection configuration [CONFIG]: " + message);
        } catch (InterruptedException e) {
            isConnected = false;
            Thread.currentThread().interrupt();
            throw new PlcConnectionException("Error while creation of the connection because of : " + e.getMessage());
        } catch (ExecutionException e) {
            isConnected = false;
            throw new PlcConnectionException("Error while creation of the connection because of : " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && isConnected;
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.disconnect().get();
            isConnected = false;
        }
    }

    @Override
    public CompletableFuture<PlcSubscriptionResponse> subscribe(PlcSubscriptionRequest subscriptionRequest) {
        InternalPlcSubscriptionRequest internalPlcSubscriptionRequest = checkInternal(subscriptionRequest, InternalPlcSubscriptionRequest.class);
        CompletableFuture<PlcSubscriptionResponse> future = CompletableFuture.supplyAsync(() -> {
            Map<String, ResponseItem<PlcSubscriptionHandle>> responseItems = internalPlcSubscriptionRequest.getSubscriptionPlcFieldMap().entrySet().stream()
                .map(subscriptionPlcFieldEntry -> {
                    final String plcFieldName = subscriptionPlcFieldEntry.getKey();
                    final SubscriptionPlcField subscriptionPlcField = subscriptionPlcFieldEntry.getValue();
                    final OpcuaField field = (OpcuaField) Objects.requireNonNull(subscriptionPlcField.getPlcField());
                    long cycleTime = subscriptionPlcField.getDuration().orElse(Duration.ofSeconds(1)).toMillis();
                    NodeId idNode = generateNodeId(field);
                    ReadValueId readValueId = new ReadValueId(
                        idNode,
                        AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);
                    UInteger clientHandle = uint(clientHandles.getAndIncrement());

                    MonitoringParameters parameters = new MonitoringParameters(
                        clientHandle,
                        (double) cycleTime,     // sampling interval
                        null,       // filter, null means use default
                        uint(1),   // queue size
                        true        // discard oldest
                    );
                    MonitoringMode monitoringMode;
                    switch (subscriptionPlcField.getPlcSubscriptionType()) {
                        case CYCLIC:
                            monitoringMode = MonitoringMode.Sampling;
                            break;
                        case CHANGE_OF_STATE:
                            monitoringMode = MonitoringMode.Reporting;
                            break;
                        case EVENT:
                            monitoringMode = MonitoringMode.Reporting;
                            break;
                        default:
                            monitoringMode = MonitoringMode.Reporting;
                    }

                    PlcSubscriptionHandle subHandle = null;
                    PlcResponseCode responseCode = PlcResponseCode.ACCESS_DENIED;
                    try {
                        UaSubscription subscription = client.getSubscriptionManager().createSubscription(cycleTime).get();

                        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                            readValueId, monitoringMode, parameters);
                        List<MonitoredItemCreateRequest> requestList = new LinkedList<>();
                        requestList.add(request);
                        OpcuaSubsriptionHandle subsriptionHandle = new OpcuaSubsriptionHandle(plcFieldName, clientHandle);
                        BiConsumer<UaMonitoredItem, Integer> onItemCreated =
                            (item, id) -> item.setValueConsumer(subsriptionHandle::onSubscriptionValue);

                        List<UaMonitoredItem> items = subscription.createMonitoredItems(
                            TimestampsToReturn.Both,
                            requestList,
                            onItemCreated
                        ).get();

                        subHandle = subsriptionHandle;
                        responseCode = PlcResponseCode.OK;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Unable to subscribe Elements because of: {}", e.getMessage());
                    } catch (ExecutionException e) {
                        logger.warn("Unable to subscribe Elements because of: {}", e.getMessage());
                    }


                    return Pair.of(plcFieldName, new ResponseItem(responseCode, subHandle));
                })
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            return new DefaultPlcSubscriptionResponse(internalPlcSubscriptionRequest, responseItems);
        });

        return future;
    }

    @Override
    public CompletableFuture<PlcUnsubscriptionResponse> unsubscribe(PlcUnsubscriptionRequest unsubscriptionRequest) {
        InternalPlcUnsubscriptionRequest internalPlcUnsubscriptionRequest = checkInternal(unsubscriptionRequest, InternalPlcUnsubscriptionRequest.class);
        internalPlcUnsubscriptionRequest.getInternalPlcSubscriptionHandles().forEach(o -> {
            OpcuaSubsriptionHandle opcSubHandle = (OpcuaSubsriptionHandle) o;
            try {
                client.getSubscriptionManager().deleteSubscription(opcSubHandle.getClientHandle()).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Unable to unsubscribe Elements because of: {}", e.getMessage());
            } catch (ExecutionException e) {
                logger.warn("Unable to unsubscribe Elements because of: {}", e.getMessage());
            }
        });

        return null;
    }

    @Override
    public PlcConsumerRegistration register(Consumer<PlcSubscriptionEvent> consumer, Collection<PlcSubscriptionHandle> handles) {
        List<PlcConsumerRegistration> unregisters = new LinkedList<>();
        handles.forEach(plcSubscriptionHandle -> unregisters.add(plcSubscriptionHandle.register(consumer)));

        return () -> unregisters.forEach(PlcConsumerRegistration::unregister);
    }

    @Override
    public void unregister(PlcConsumerRegistration registration) {
        registration.unregister();
    }

    @Override
    public CompletableFuture<PlcReadResponse> read(PlcReadRequest readRequest) {
        CompletableFuture<PlcReadResponse> future = CompletableFuture.supplyAsync(() -> {
            readRequest.getFields();
            Map<String, ResponseItem<PlcValue>> fields = new HashMap<>();
            List<NodeId> readValueIds = new LinkedList<>();
            List<PlcField> readPLCValues = readRequest.getFields();
            for (PlcField field : readPLCValues) {
                NodeId idNode = generateNodeId((OpcuaField) field);
                readValueIds.add(idNode);
            }

            CompletableFuture<List<DataValue>> dataValueCompletableFuture = client.readValues(0.0, TimestampsToReturn.Both, readValueIds);
            List<DataValue> readValues = null;
            try {
                readValues = dataValueCompletableFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Unable to read Elements because of: {}", e.getMessage());
            } catch (ExecutionException e) {
                logger.warn("Unable to read Elements because of: {}", e.getMessage());
            }
            for (int counter = 0; counter < readValueIds.size(); counter++) {
                PlcResponseCode resultCode = PlcResponseCode.OK;
                PlcValue stringItem = null;
                if (readValues == null || readValues.size() <= counter ||
                    !readValues.get(counter).getStatusCode().equals(StatusCode.GOOD)) {
                    resultCode = PlcResponseCode.NOT_FOUND;
                } else {
                    stringItem = encodePlcValue(readValues.get(counter));

                }
                ResponseItem<PlcValue> newPair = new ResponseItem<>(resultCode, stringItem);
                fields.put((String) readRequest.getFieldNames().toArray()[counter], newPair);


            }
            InternalPlcReadRequest internalPlcReadRequest = checkInternal(readRequest, InternalPlcReadRequest.class);
            return new DefaultPlcReadResponse(internalPlcReadRequest, fields);
        });

        return future;
    }

    @Override
    public CompletableFuture<PlcWriteResponse> write(PlcWriteRequest writeRequest) {
        CompletableFuture<PlcWriteResponse> future;
        future = CompletableFuture.supplyAsync(() -> {

            InternalPlcWriteRequest internalPlcWriteRequest = (InternalPlcWriteRequest) writeRequest;

            LinkedList<DataValue> values = new LinkedList<>();
            LinkedList<NodeId> ids = new LinkedList<>();
            LinkedList<String> names = new LinkedList<>();
            Map<String, PlcResponseCode> fieldResponse = new HashMap<>();

            for (String fieldName : writeRequest.getFieldNames()) {
                OpcuaField uaField = (OpcuaField) writeRequest.getField(fieldName);
                NodeId idNode = generateNodeId(uaField);
                Object valueObject = internalPlcWriteRequest.getPlcValue(fieldName).getObject();

                // Added small work around for handling BigIntegers as input type for UInt64
                if (valueObject instanceof BigInteger)
                    valueObject = ulong((BigInteger) valueObject);

                // Another workaround for the PlcList incompatible with Eclipse Milo
                if (valueObject instanceof List<?>) {
                    valueObject = getPlainArrayFromCollection(internalPlcWriteRequest.getPlcValue(fieldName).getList());
                }
                
                Variant var = new Variant(valueObject);
                DataValue value = new DataValue(var, null, null, null);
                ids.add(idNode);
                names.add(fieldName);
                values.add(value);
            }

            CompletableFuture<List<StatusCode>> opcRequest =
                client.writeValues(ids, values);
            List<StatusCode> statusCodes = null;

            try {
                statusCodes = opcRequest.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                statusCodes = new LinkedList<>();
                for (int counter = 0; counter < ids.size(); counter++) {
                    ((LinkedList<StatusCode>) statusCodes).push(StatusCode.BAD);
                }
            } catch (ExecutionException e) {
                statusCodes = new LinkedList<>();
                for (int counter = 0; counter < ids.size(); counter++) {
                    ((LinkedList<StatusCode>) statusCodes).push(StatusCode.BAD);
                }
            }

            for (int counter = 0; counter < names.size(); counter++) {
                PlcResponseCode resultCode;
                if (statusCodes != null && statusCodes.size() > counter) {
                    if (statusCodes.get(counter).isGood()) {
                        resultCode = PlcResponseCode.OK;
                    } else if (statusCodes.get(counter).isUncertain()) {
                        resultCode = PlcResponseCode.NOT_FOUND;
                    } else if (statusCodes.get(counter).isBad() && statusCodes.get(counter).getValue() == 2155085824L) {
                        resultCode = PlcResponseCode.INVALID_DATATYPE;
                    } else {
                        resultCode = PlcResponseCode.ACCESS_DENIED;
                    }
                } else {
                    resultCode = PlcResponseCode.ACCESS_DENIED;
                }
                fieldResponse.put(names.get(counter), resultCode);
            }

            InternalPlcWriteRequest internalPlcReadRequest = checkInternal(writeRequest, InternalPlcWriteRequest.class);
            PlcWriteResponse response = new DefaultPlcWriteResponse(internalPlcReadRequest, fieldResponse);
            return response;
        });


        return future;
    }

    private <T> T[] convertPlcListToArray(Class<T> clazz, List<? extends PlcValue> list) {
        if (!list.isEmpty()) {
            T[] ret = (T[]) Array.newInstance(clazz, list.size());

            for (int i = 0; i < ret.length; i++) {
                ret[i] = (T) list.get(i).getObject();
            }
            return ret;
        } else {
            return null;
        }
    }

    private Object[] getPlainArrayFromCollection(List<? extends PlcValue> list) {
        if (!list.isEmpty()) {
            Object firstElem = list.get(0);

            if (firstElem instanceof PlcBoolean) {
                return convertPlcListToArray(Boolean.class, list);
            } else if (firstElem instanceof PlcByte) {
                return convertPlcListToArray(Byte.class, list);
            } else if (firstElem instanceof PlcShort) {
                return convertPlcListToArray(Short.class, list);
            } else if (firstElem instanceof PlcInteger) {
                return convertPlcListToArray(Integer.class, list);
            } else if (firstElem instanceof PlcLong) {
                return convertPlcListToArray(Long.class, list);
            } else if (firstElem instanceof PlcFloat) {
                return convertPlcListToArray(Float.class, list);
            } else if (firstElem instanceof PlcDouble) {
                return convertPlcListToArray(Double.class, list);
            } else if (firstElem instanceof PlcString) {
                return convertPlcListToArray(String.class, list);
            } else {
                return list.toArray(new Object[list.size()]);
            }
        } else {
            return new Object[0];
        }
    }

    private NodeId generateNodeId(OpcuaField uaField) {
        NodeId idNode = null;
        switch (uaField.getIdentifierType()) {
            case STRING_IDENTIFIER:
                idNode = new NodeId(uaField.getNamespace(), uaField.getIdentifier());
                break;
            case NUMBER_IDENTIFIER:
                idNode = new NodeId(uaField.getNamespace(), UInteger.valueOf(uaField.getIdentifier()));
                break;
            case GUID_IDENTIFIER:
                idNode = new NodeId(uaField.getNamespace(), UUID.fromString(uaField.getIdentifier()));
                break;
            case BINARY_IDENTIFIER:
                idNode = new NodeId(uaField.getNamespace(), new ByteString(uaField.getIdentifier().getBytes()));
                break;

            default:
                idNode = new NodeId(uaField.getNamespace(), uaField.getIdentifier());
        }

        return idNode;
    }

    private String getEndpointUrl(InetAddress address, Integer port, String params) {
        return "opc.tcp://" + address.getHostAddress() + ":" + port + "/" + params;
    }

    private Predicate<EndpointDescription> endpointFilter() {
        return e -> true;
    }

    private SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.None;
    }

    private IdentityProvider getIdentityProvider() {
        return new AnonymousProvider();
    }
    
    private static String getSubPathOfParams(String params) {
        if (params.contains("=")) {
            if (params.contains("?")) {
                return params.split("\\?")[0];
            } else {
                return "";
            }

        } else {
            return params;
        }
    }

    private static String getOptionString(String params) {
        if (params != null && params.contains("=")) {
            if (params.contains("?")) {
                return params.split("\\?")[1];
            } else {
                return params;
            }

        } else {
            return "";
        }
    }

}
