package cn.siwei.fubin.mqtt.starter.utils;

import cn.siwei.fubin.mqtt.starter.handler.MyMqttv5PahoMessageHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.AbstractMqttMessageHandler;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.ObjectUtils;

import java.util.*;


@Log4j2
public class MqttUtils {

    /**
     * qos 0
     */
    public static final int QOS_0 = 0;
    /**
     * qos 1
     */
    public static final int QOS_1 = 1;
    /**
     * qos 2
     */
    public static final int QOS_2 = 2;
    private final static Map<String, MqttPahoMessageHandler> HANDLER_MAP = new HashMap<>(16);
    private final static Map<String, MyMqttv5PahoMessageHandler> MQTTV5_HANDLER_MAP = new HashMap<>(16);

    private final static Map<String, MqttPahoMessageDrivenChannelAdapter> ADAPTER_MAP_COMSUMER = new HashMap<>(16);
    private final static Map<String, Mqttv5PahoMessageDrivenChannelAdapter> MQTTV5_ADAPTER_MAP_COMSUMER = new HashMap<>(16);

    private static MqttPahoMessageHandler defaultMqttPahoMessageHandler;
    private static MyMqttv5PahoMessageHandler defaultMqttv5PahoMessageHandler;

    private static MqttPahoMessageDrivenChannelAdapter defaultAdapter;
    private static Mqttv5PahoMessageDrivenChannelAdapter defaultV5Adapter;

    public final static String CHANNEL_NAME_SUFFIX = "MqttPahoMessageHandler";
    public final static String CHANNEL_NAME_COMSUMER_SUFFIX = "ConsumerMqttPahoMessageHandler";

    /**
     * 存放handler
     *
     * @param channelName
     * @param handler
     */
    public static void put(String channelName, MqttPahoMessageHandler handler) {
        if (ObjectUtils.isEmpty(defaultMqttPahoMessageHandler)) {
            defaultMqttPahoMessageHandler = handler;
        }
        HANDLER_MAP.put(channelName + CHANNEL_NAME_SUFFIX, handler);
    }


    /**
     * 存放comsumerhandler
     *
     * @param channelName
     */
    public static void putComsumer(String channelName, MqttPahoMessageDrivenChannelAdapter adapter) {
        if (ObjectUtils.isEmpty(defaultAdapter)) {
            defaultAdapter = adapter;
        }
        ADAPTER_MAP_COMSUMER.put(channelName + CHANNEL_NAME_COMSUMER_SUFFIX, adapter);
    }


    public static void putv5(String channelName, MyMqttv5PahoMessageHandler handler) {
        if (ObjectUtils.isEmpty(defaultMqttv5PahoMessageHandler)) {
            defaultMqttv5PahoMessageHandler = handler;
        }
        MQTTV5_HANDLER_MAP.put(channelName + CHANNEL_NAME_SUFFIX, handler);
    }

    public static void putv5Comsumer(String channelName, Mqttv5PahoMessageDrivenChannelAdapter adapter) {
        if (ObjectUtils.isEmpty(defaultV5Adapter)) {
            defaultV5Adapter = adapter;
        }
        MQTTV5_ADAPTER_MAP_COMSUMER.put(channelName + CHANNEL_NAME_COMSUMER_SUFFIX, adapter);
    }

    /**
     * 发送消息
     *
     * @param topic       要发送的主题
     * @param message     消息内容
     * @param qos         qos级别
     * @param channelName 发送到指定的通道
     */
    public static void sendMessage(String topic, String message, int qos, String channelName) {
        Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos).build();
        dispatchSendMessage(mqttMessage, channelName);
    }

    public static void sendMessage(String topic, byte[] message, int qos, String channelName) {
        Message<byte[]> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos).build();
        dispatchSendMessage(mqttMessage, channelName);
    }

    /**
     * 发送消息,默认qos级别为1
     *
     * @param topic       要发送的主题
     * @param message     消息内容
     * @param channelName 发送到指定的通道
     */
    public static void sendMessage(String topic, String message, String channelName) {
        Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, QOS_1).build();
        dispatchSendMessage(mqttMessage, channelName);
    }

    public static void sendMessage(String topic, byte[] message, String channelName) {
        Message<byte[]> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, QOS_1).build();
        dispatchSendMessage(mqttMessage, channelName);
    }



    /**
     * 发送消息
     *
     * @param mqttMessage 消息
     * @param channelName 发送到指定的通道
     */
    public static void sendMessage(Message<?> mqttMessage, String channelName) {
        dispatchSendMessage(mqttMessage, channelName);
    }



    /**
     * 如果只有一个通道将使用该通道发送消息
     *
     * @param topic
     * @param message
     * @param qos
     */
    public static void sendMessage(String topic, byte[] message, int qos) {
        Message<byte[]> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos).build();
        dispatchSendMessage(mqttMessage);
    }

    public static void sendMessage(String topic, String message, int qos) {
        Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos).build();
        dispatchSendMessage(mqttMessage);
    }

    /**
     * 如果只有一个通道将使用该通道发送消息，默认qos级别为1
     *
     * @param topic
     * @param message
     */
    public static void sendMessage(String topic, String message) {
        Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, QOS_1).build();
        dispatchSendMessage(mqttMessage);
    }

    public static void sendMessage(String topic, byte[] message) {
        Message<byte[]> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, QOS_1).build();
        dispatchSendMessage(mqttMessage);
    }

    /**
     * 如果只有一个通道将使用该通道发送消息，默认qos级别为1
     *
     * @param mqttMessage 消息信息
     */
    public static void sendMessage(Message<?> mqttMessage) {
        dispatchSendMessage(mqttMessage);
    }




    public static void dispatchSendMessage(Message<?> mqttMessage, String channelName) {
        if (ObjectUtils.isEmpty(channelName)) {
            AbstractMqttMessageHandler handler = getDefaultHandler();
            if (!ObjectUtils.isEmpty(handler)) {
                handler.handleMessage(mqttMessage);
            } else {
                MyMqttv5PahoMessageHandler v5Handler = getDefaultV5Handler();
                if (!ObjectUtils.isEmpty(v5Handler)) {
                    v5Handler.handleMessage(mqttMessage);
                } else {
                    log.error("未找到有效的mqtt客户端");
                }
            }
        } else {
            MqttPahoMessageHandler handler = getHandler(channelName);
            if (!ObjectUtils.isEmpty(handler)) {
                handler.handleMessage(mqttMessage);
            } else {
                MyMqttv5PahoMessageHandler v5Handler = getV5Handler(channelName);
                if(!ObjectUtils.isEmpty(v5Handler)) {
                    v5Handler.handleMessage(mqttMessage);
                }else {
                    log.error("未找到有效的mqtt客户端");
                }
            }
        }

    }

    public static void dispatchSendMessage(Message<?> mqttMessage) {
        dispatchSendMessage(mqttMessage, null);
    }

    /**
     * 获取默认的handler
     *
     * @return
     */
    private static AbstractMqttMessageHandler getDefaultHandler() {
        return defaultMqttPahoMessageHandler != null ? defaultMqttPahoMessageHandler : defaultMqttv5PahoMessageHandler;
    }

    /**
     * 获取默认的handler
     *
     * @return
     */
    private static MyMqttv5PahoMessageHandler getDefaultV5Handler() {




        return defaultMqttv5PahoMessageHandler;
    }

    /**
     * 根据通道获取handler
     *
     * @param channelName
     * @return
     */

    public static MqttPahoMessageHandler getHandler(String channelName) {
        MqttPahoMessageHandler handler = HANDLER_MAP.get(channelName + CHANNEL_NAME_SUFFIX);


        if (handler == null) {
            log.error("未查询到相应通道{}的handler，存在的通道名称{}", channelName, HANDLER_MAP.keySet());
            throw new IllegalArgumentException("未查询到相应通道" + channelName + "的handler");
        }
        return handler;
    }


    public static MyMqttv5PahoMessageHandler getV5Handler(String channelName) {
        MyMqttv5PahoMessageHandler handler = MQTTV5_HANDLER_MAP.get(channelName + CHANNEL_NAME_SUFFIX);
        if (handler == null) {
            log.error("未查询到相应通道{}的handler，存在的通道名称{}", channelName, HANDLER_MAP.keySet());
            throw new IllegalArgumentException("未查询到相应通道" + channelName + "的handler");
        }
        return handler;
    }


    public static MqttPahoMessageDrivenChannelAdapter getComsumerMqttAdapter(String channelName) {
        MqttPahoMessageDrivenChannelAdapter adapter = ADAPTER_MAP_COMSUMER.get(channelName + CHANNEL_NAME_COMSUMER_SUFFIX);
        return adapter;
    }

    public static MqttPahoMessageDrivenChannelAdapter getComsumerMqttAdapter() {

        return defaultAdapter;
    }

    public static Mqttv5PahoMessageDrivenChannelAdapter getMqttV5Adapter(String channelName) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter = MQTTV5_ADAPTER_MAP_COMSUMER.get(channelName + CHANNEL_NAME_COMSUMER_SUFFIX);
        return adapter;
    }

    public static Mqttv5PahoMessageDrivenChannelAdapter getMqttV5Adapter() {
        return defaultV5Adapter;
    }

}
