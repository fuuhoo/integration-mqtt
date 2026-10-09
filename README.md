# sw-intergration-mqtt-starter

基于 Spring Boot 的 MQTT 自动配置 Starter，支持 MQTT v3 和 MQTT v5 协议，简化了 MQTT 消息的发送和接收。

## 适用版本

- **JDK**: 1.8+
- **Spring Boot**: 2.x

## 功能特性

- 支持 MQTT v3 (默认) 和 MQTT v5 双协议
- 支持多通道配置，可同时连接多个 MQTT Broker
- 自动配置，开箱即用
- 支持消费者/生产者独立开关控制
- 支持异步消息发送
- 支持自动重连及自定义重连策略
- 支持遗嘱消息配置
- 支持自定义客户端工厂配置（如 SSL）
- MQTT v5 支持 User Properties 等扩展属性

## Maven 引用

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cn.siwei.fubin</groupId>
    <artifactId>sw-intergration-mqtt-starter</artifactId>
    <version>0.0.7-SNAPSHOT</version>
</dependency>
```

## 配置说明

在 `application.yml` 或 `application.properties` 中进行配置：

### 基础配置示例

```yaml
mqtt:
  config:
    # 通道名称，可配置多个
    channel1:
      # MQTT Broker 地址，支持多个
      url:
        - tcp://127.0.0.1:1883
      # 连接超时时间（秒）
      timeout: 30
      # 心跳间隔（秒）
      kepAliveInterval: 60
      # 订阅的主题列表
      topics:
        - topic/test/1
        - topic/test/2
      # 对应主题的 QoS 级别
      qos:
        - 1
        - 1
      # 用户名
      username: admin
      # 密码
      password: admin
      # 消费者 ClientId
      consumerClientId: consumer-001
      # 生产者 ClientId
      producerClientId: producer-001
      # 是否在 ClientId 后追加本机 IP（避免多实例冲突）
      clientIdAppendIp: true
      # 是否开启消费者，默认 true
      consumerEnable: true
      # 是否开启生产者，默认 true
      producerEnable: true
      # 是否异步发送，默认 false
      async: false
      # 是否自动重连，默认 true
      automaticReconnect: true
      # 是否清除 Session（v3），默认 true
      cleanSession: true
      # MQTT 协议版本：3=v3.1, 4=v3.1.1(默认), 5=v5.0
      mqttVersion: 4
      # 最大传输中消息数
      maxInflight: 1000
```

### MQTT v5 配置示例

```yaml
mqtt:
  config:
    v5channel:
      url:
        - tcp://127.0.0.1:1883
      timeout: 30
      kepAliveInterval: 60
      topics:
        - topic/v5/test
      qos:
        - 1
      username: admin
      password: admin
      consumerClientId: v5-consumer
      producerClientId: v5-producer
      # MQTT v5 版本
      mqttVersion: 5
      # v5 使用 cleanStart 替代 cleanSession
      cleanStart: true
      # 会话过期时间（秒），默认 1800
      sessionExpiryInterval: 1800
      # 最小重连延迟（秒）
      automaticReconnectMinDelay: 0
      # 最大重连延迟（秒）
      automaticReconnectMaxDelay: 300
      # 消息负载类型：空=String, BYTE=字节数组
      payloadType: BYTE
```

### 遗嘱消息配置

```yaml
mqtt:
  config:
    channel1:
      # ... 其他配置
      # 消费者遗嘱
      consumerWill:
        topic: will/topic
        payload: consumer offline
        qos: 1
        retained: false
      # 生产者遗嘱
      producerWill:
        topic: will/topic
        payload: producer offline
        qos: 1
        retained: false
```

### 多通道配置示例

```yaml
mqtt:
  config:
    # 第一个 Broker
    broker1:
      url:
        - tcp://192.168.1.10:1883
      topics:
        - topic/broker1/
      qos:
        - 1
      username: user1
      password: pass1
      consumerClientId: consumer-b1
      producerClientId: producer-b1
    # 第二个 Broker
    broker2:
      url:
        - tcp://192.168.1.11:1883
      topics:
        - topic/broker2/
      qos:
        - 1
      username: user2
      password: pass2
      consumerClientId: consumer-b2
      producerClientId: producer-b2
```

## 配置项详解

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `url` | String[] | - | MQTT Broker 地址，如 `tcp://ip:port` |
| `timeout` | int | - | 连接超时时间（秒） |
| `kepAliveInterval` | int | - | 心跳间隔（秒） |
| `topics` | String[] | - | 订阅/发送的主题列表 |
| `qos` | int[] | - | 对应主题的 QoS 级别（0/1/2） |
| `username` | String | - | 连接用户名 |
| `password` | String | - | 连接密码 |
| `consumerClientId` | String | - | 消费者客户端 ID |
| `producerClientId` | String | - | 生产者客户端 ID |
| `clientIdAppendIp` | Boolean | false | 是否在 ClientId 后追加本机 IP |
| `consumerEnable` | Boolean | true | 是否开启消费者 |
| `producerEnable` | Boolean | true | 是否开启生产者 |
| `async` | Boolean | false | 是否异步发送消息 |
| `automaticReconnect` | Boolean | true | 是否自动重连 |
| `cleanSession` | Boolean | true | 是否清除 Session（v3） |
| `cleanStart` | Boolean | true | 是否清除 Start（v5） |
| `maxInflight` | Integer | - | 最大传输中消息数 |
| `mqttVersion` | Integer | 4 | MQTT 协议版本：3/4/5 |
| `sessionExpiryInterval` | Long | 1800 | 会话过期时间（秒，v5） |
| `automaticReconnectMinDelay` | int | 0 | 最小重连延迟（秒） |
| `automaticReconnectMaxDelay` | int | 300 | 最大重连延迟（秒） |
| `payloadType` | String | - | 负载类型：空=String, BYTE=字节数组 |
| `consumerWill` | Object | - | 消费者遗嘱配置 |
| `producerWill` | Object | - | 生产者遗嘱配置 |

## 使用方式

### 1. 发送消息

```java
import cn.siwei.fubin.mqtt.starter.utils.MqttUtils;

@Service
public class MqttService {

    public void send() {
        // 发送 String 消息到指定主题（默认 QoS=1）
        MqttUtils.sendMessage("topic/test", "hello mqtt");

        // 发送 String 消息，指定 QoS
        MqttUtils.sendMessage("topic/test", "hello mqtt", 2);

        // 发送字节数组消息
        MqttUtils.sendMessage("topic/test", "hello".getBytes(), 1);

        // 发送到指定通道（多通道时使用）
        MqttUtils.sendMessage("topic/test", "hello", "channel1");
        MqttUtils.sendMessage("topic/test", "hello", 2, "channel1");
    }
}
```

### 2. MQTT v5 发送消息（支持 User Properties）

```java
import cn.siwei.fubin.mqtt.starter.utils.MqttUtils;
import java.util.HashMap;
import java.util.Map;

@Service
public class MqttV5Service {

    public void sendV5() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("key1", "value1");
        userProperties.put("key2", "value2");

        // 发送 v5 消息到默认通道
        MqttUtils.sendV5Message("topic/v5", "hello v5", 1, userProperties);

        // 发送 v5 消息到指定通道
        MqttUtils.sendV5Message("topic/v5", "hello v5", 1, "v5channel", userProperties);

        // 发送字节数组 v5 消息
        MqttUtils.sendV5Message("topic/v5", "hello".getBytes(), 1, userProperties);
    }
}
```

### 3. 接收消息（消费者）

消费者通过 Spring Integration 的 `@ServiceActivator` 注解接收消息：

```java
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MqttConsumer {

    /**
     * 接收指定通道的消息
     * inputChannel 值为配置中的通道名称
     */
    @ServiceActivator(inputChannel = "channel1")
    public void handleMessage(Message<?> message) {
        System.out.println("收到消息: " + message.getPayload());
        System.out.println("消息头: " + message.getHeaders());
    }
}
```

### 4. 自定义客户端工厂配置

如需自定义连接配置（如 SSL），实现 `MqttPahoClientFactorySettingCallback` 接口：

```java
import cn.siwei.fubin.mqtt.starter.MqttPahoClientFactorySettingCallback;
import cn.siwei.fubin.mqtt.starter.config.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.stereotype.Component;

@Component
public class MyMqttCallback implements MqttPahoClientFactorySettingCallback {

    @Override
    public void callback(MqttConnectOptions options, String channelName,
                         MqttProperties.Config config, boolean isConsumer) {
        // 自定义配置，如 SSL、自定义属性等
        // options.setSocketFactory(...);
        // options.setSSLProperties(...);
    }
}
```

### 5. 监听连接事件

```java
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttEventHandler {

    @EventListener
    public void handleConnectionFailed(MqttConnectionFailedEvent event) {
        Throwable cause = event.getCause();
        if (cause instanceof MqttException) {
            MqttException mqttException = (MqttException) cause;
            log.error("MQTT 连接失败: {}", mqttException.getMessage());
        }
    }
}
```

## 注意事项

1. **ClientId 唯一性**：同一 Broker 下 ClientId 必须唯一，建议开启 `clientIdAppendIp` 避免多实例冲突
2. **QoS 与 topics 对应**：`qos` 数组长度应与 `topics` 数组长度一致，按索引一一对应
3. **MQTT v5 特性**：使用 v5 时需确保 Broker 支持 MQTT v5 协议
4. **自动重连**：建议保持 `automaticReconnect: true`，确保网络恢复后自动连接
5. **通道名称**：配置中的通道名称（如 `channel1`）即为 Spring Integration 的通道 Bean 名称，消费者需保持一致

## 依赖说明

| 依赖 | 版本 | 说明 |
|------|------|------|
| spring-boot-starter | 2.x | Spring Boot 基础 |
| spring-integration-mqtt | 5.5.20 | Spring Integration MQTT 支持 |
| org.eclipse.paho.mqttv5.client | 1.2.5 | Paho MQTT v5 客户端 |
| lombok | 1.18.26 | 代码简化 |
