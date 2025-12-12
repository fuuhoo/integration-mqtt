package cn.siwei.fubin.mqtt.starter.listener;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class MqttEventListener {

    @EventListener
    public void handleMqttConnectionFailedEvent(MqttConnectionFailedEvent event) {
        Throwable cause = event.getCause();
        // 这里可以处理连接失败的原因，比如密码错误
        // 你可以检查cause中的信息，例如：
        if (cause instanceof MqttException) {
            MqttException mqttException = (MqttException) cause;
            log.error("MqttException: {}", mqttException.getMessage());
            // 进一步判断原因码等
            // 注意：Paho的MqttException中包含了原因码，但可能需要根据MQTT v5的原因码进行判断
            // 例如，密码错误可能对应0x86原因码
        } else if (cause instanceof org.eclipse.paho.mqttv5.common.MqttException) {
            org.eclipse.paho.mqttv5.common.MqttException mqttException = (org.eclipse.paho.mqttv5.common.MqttException) cause;
            log.error("MqttException: {}", mqttException.getMessage());
        }
        // 发送通知，记录日志等
    }
}
