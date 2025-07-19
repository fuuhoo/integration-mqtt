package cn.siwei.fubin.mqtt.starter.adapter;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;

public class MyMqttv5PahoMessageDrivenChannelAdapter extends Mqttv5PahoMessageDrivenChannelAdapter {
    public MyMqttv5PahoMessageDrivenChannelAdapter (String url, String clientId, String... topic) {
        super(url, clientId, topic);
    }

    public MyMqttv5PahoMessageDrivenChannelAdapter (MqttConnectionOptions connectionOptions, String clientId, String... topic) {
        super(connectionOptions, clientId, topic);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            reconnect = false;
        }
        super.connectComplete(reconnect, serverURI);
    }

}
