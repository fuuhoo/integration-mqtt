package cn.siwei.fubin.mqtt.starter;



import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import cn.siwei.fubin.mqtt.starter.config.MqttProperties.Config;
import cn.siwei.fubin.mqtt.starter.config.MqttProperties.Will;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
/**
 * <p>
 * 描述: 客户端工厂配置
 * </p>
 * 
 * @author aLiang
 * @date 2022年4月30日 下午10:21:34
 */
@Log4j2
public class MqttPahoClientFactorySetting {

	private MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback;

	public MqttPahoClientFactorySetting(MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback) {
		super();
		this.mqttPahoClientFactorySettingCallback = mqttPahoClientFactorySettingCallback;
	}

	/**
	 * 
	 * <p>
	 * 配置MqttPahoClientFactory
	 * MqttPahoClientFactory:这个很重要，是org.springframework.integration.mqtt自带的，通过工厂类构建client
	 * </P>
	 * @param channelName 通道名
	 * @param config      配置信息
	 * @param isConsumer  是否是消费者
	 * @return
	 */
	public MqttPahoClientFactory mqttClientFactory(String channelName, Config config, boolean isConsumer) {
		return mqttClientFactoryV3(channelName, config, isConsumer);
	}



	public MqttPahoClientFactory mqttClientFactoryV3(String channelName, Config config, boolean isConsumer) {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();

		options.setServerURIs(config.getUrl());
		options.setCleanSession(config.getCleanSession() == null ? true : config.getCleanSession());
		options.setKeepAliveInterval(config.getKepAliveInterval());
		options.setPassword(config.getPassword().toCharArray());
		options.setUserName(config.getUsername());
		options.setConnectionTimeout(config.getTimeout());
		Boolean reconnect = config.getAutomaticReconnect() == null ? true : config.getAutomaticReconnect();
		options.setAutomaticReconnect(reconnect);
		Integer mqttVersion = config.getMqttVersion();

		if (mqttVersion != null) {
			options.setMqttVersion(mqttVersion);
		}

		Integer maxInflight = config.getMaxInflight();
		if (maxInflight != null) {
			options.setMaxInflight(maxInflight);
		}

		Will will = null;
		if (isConsumer && config.getConsumerWill() != null) {
			will = config.getConsumerWill();
		} else if (!isConsumer && config.getProducerWill() != null) {
			will = config.getProducerWill();
		}
		if (will != null) {
			try {
				options.setWill(will.getTopic(), will.getPayload().getBytes("utf-8"), will.getQos(),
						will.getRetained());
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
		}

		if (mqttPahoClientFactorySettingCallback != null) {
			mqttPahoClientFactorySettingCallback.callback(options, channelName, config, isConsumer);
		}

		factory.setConnectionOptions(options);
		return factory;

	}


	public MqttConnectionOptions mqttV5Connection(String channelName,Config config,boolean isConsumer){
		MqttConnectionOptions options=new MqttConnectionOptions();
		options.setServerURIs(config.getUrl());
		options.setUserName(config.getUsername());
		options.setPassword(config.getPassword().getBytes());
		options.setAutomaticReconnect(true);
		options.setCleanStart(false);
//		// 配置 最大传输中数，默认值10，qos!=0 时生效
//		//表示允许同时在传输中的最大消息数量。
//		// MQTT 协议规定，在未收到 ACK 确认之前，客户端只能同时传输一定数量的消息。
//		// MaxInflight 指标用来控制该数量，以避免网络拥塞
//		options.setReceiveMaximum(config.getMaxInflight()==null?1000:config.getMaxInflight());
//		options.setAutomaticReconnectDelay(config.getAutomaticReconnectMinDelay(),config.getAutomaticReconnectMaxDelay());
//		options.setKeepAliveInterval(config.getKepAliveInterval());
//		Boolean reconnect = config.getAutomaticReconnect() == null ? true : config.getAutomaticReconnect();
//		options.setAutomaticReconnect(reconnect);
//		options.setCleanStart(config.getCleanStart() == null ? true : config.getCleanStart());
//
		options.setSessionExpiryInterval(config.getSessionExpiryInterval());
//		options.setConnectionTimeout(config.getTimeout());
//
//		Will will = null;
//		if (isConsumer && config.getConsumerWill() != null) {
//			will = config.getConsumerWill();
//		} else if (!isConsumer && config.getProducerWill() != null) {
//			will = config.getProducerWill();
//		}
//
//		if (will != null) {
//			try {
//				MqttMessage mqttMessage = new MqttMessage(will.getPayload().getBytes("utf-8"), will.getQos(), will.getRetained(), null);
//				options.setWill(will.getTopic(), mqttMessage);
//			} catch (UnsupportedEncodingException e) {
//				log.error(e.getMessage(), e);
//			}
//		}

		return options;
	}
	public MqttPahoClientFactorySettingCallback getMqttPahoClientFactorySettingCallback() {
		return mqttPahoClientFactorySettingCallback;
	}

	public void setMqttPahoClientFactorySettingCallback(
			MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback) {
		this.mqttPahoClientFactorySettingCallback = mqttPahoClientFactorySettingCallback;
	}

}
