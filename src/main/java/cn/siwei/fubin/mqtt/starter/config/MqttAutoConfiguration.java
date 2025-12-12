package cn.siwei.fubin.mqtt.starter.config;


import cn.siwei.fubin.mqtt.starter.MqttPahoClientFactorySetting;
import cn.siwei.fubin.mqtt.starter.MqttPahoClientFactorySettingCallback;
import cn.siwei.fubin.mqtt.starter.handler.MyMqttHeaderMapper;
import cn.siwei.fubin.mqtt.starter.handler.MyMqttv5PahoMessageHandler;
import cn.siwei.fubin.mqtt.starter.utils.MqttUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import cn.siwei.fubin.mqtt.starter.config.MqttProperties.Config;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 描述:mqtt自动配置
 * </p>
 * 
 * @author xingyl
 * @date 2020-03-27 17:08:36
 */
@Log4j2
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration implements ApplicationContextAware, BeanPostProcessor {

	private ConfigurableApplicationContext applicationContext;
	
	@Autowired
	private MqttProperties mqttProperties;
	
	@Autowired(required = false)
	private MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback;

	@ConditionalOnMissingBean(MqttPahoClientFactorySetting.class)
	@Bean
	public MqttPahoClientFactorySetting mqttPahoClientFactorySetting() {
		return new MqttPahoClientFactorySetting(mqttPahoClientFactorySettingCallback);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		//循环初始化mqtt config
		mqttProperties.getConfig().forEach((chnnelName, config) -> init(chnnelName, config));
	}

	/**
	 * 初始化,根据配置，循环此函数
	 * 
	 * @param channelName
	 * @param config
	 */
	private void init(String channelName, Config config) {
		Integer mqttVersion = config.getMqttVersion();
		if(mqttVersion!=5) {
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
			// 默认开启consumer，消费者
			if (!Boolean.FALSE.equals(config.getConsumerEnable())) {
				// 注册通道信息
				beanFactory.registerBeanDefinition(channelName, mqttChannel());
				log.info("mqtt============>初始化mqtt, channel {}, 配置 {} ", channelName, config);
				MessageChannel mqttChannel = beanFactory.getBean(channelName, MessageChannel.class);
				beanFactory.registerBeanDefinition(channelName + "MqttChannelAdapter", channelAdapter(channelName, config, mqttChannel));

				MqttPahoMessageDrivenChannelAdapter bean = beanFactory.getBean(channelName + "MqttChannelAdapter", MqttPahoMessageDrivenChannelAdapter.class);
				MqttUtils.putComsumer(channelName, bean);
				log.info("初始化mqtt Channel Adapter");
			}

			// 生产者
			if (!Boolean.FALSE.equals(config.getProducerEnable())) {
				String handlerBeanName = channelName + MqttUtils.CHANNEL_NAME_SUFFIX;
				//这里注册到mqttOutbound函数
				//注册producer
				beanFactory.registerBeanDefinition(handlerBeanName, mqttOutbound(channelName, config));
				log.info("初始化mqtt MqttPahoMessageHandler");
				//这里用到了MqttUtils,初始化的时候放到HANDLER_MAP中，以便发送的时候用
				MqttPahoMessageHandler bean = beanFactory.getBean(handlerBeanName, MqttPahoMessageHandler.class);
				MqttUtils.put(channelName, bean);
			}
		}else{
			//v5版本
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
			//消费者
			if (!Boolean.FALSE.equals(config.getConsumerEnable())) {
				// 注册通道信息
				beanFactory.registerBeanDefinition(channelName, mqttChannel());
				log.info("mqtt============>初始化mqttV5, channel {}, 配置 {} ", channelName, config);
				MessageChannel mqttChannel = beanFactory.getBean(channelName, MessageChannel.class);
				//Adapter
				beanFactory.registerBeanDefinition(channelName + "MqttChannelAdapter", channelAdapterV5(channelName, config, mqttChannel));
				log.info("初始化mqttV5 Channel Adapter");
				Mqttv5PahoMessageDrivenChannelAdapter bean = beanFactory.getBean(channelName + "MqttChannelAdapter", Mqttv5PahoMessageDrivenChannelAdapter.class);
				MqttUtils.putv5Comsumer(channelName, bean);
			}

			//生产者
			if (!Boolean.FALSE.equals(config.getProducerEnable())) {
				String handlerBeanName = channelName + MqttUtils.CHANNEL_NAME_SUFFIX;
				beanFactory.registerBeanDefinition(handlerBeanName, mqttOutboundV5(channelName, config));
				MyMqttv5PahoMessageHandler bean = beanFactory.getBean(handlerBeanName, MyMqttv5PahoMessageHandler.class);

				// 创建并设置 HeaderMapper
				MyMqttHeaderMapper headerMapper = new MyMqttHeaderMapper();
				headerMapper.setOutboundHeaderNames(
						"mqtt_userProperty_*"
				);
				headerMapper.setInboundHeaderNames("*");
				bean.setHeaderMapper(headerMapper);
				MqttUtils.putv5(channelName, bean);
			}
		}
	}


	/**
	 * 初始化通道
	 * 
	 * @return
	 */
	private AbstractBeanDefinition mqttChannel() {
		BeanDefinitionBuilder messageChannelBuilder = BeanDefinitionBuilder.genericBeanDefinition(DirectChannel.class);
		messageChannelBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
		return messageChannelBuilder.getBeanDefinition();
	}

	/**
	 * mqtt消息驱动转换器
	 * 
	 * @param channelName
	 * @param config
	 * @param mqttChannel
	 * @return
	 */
	private AbstractBeanDefinition channelAdapter(String channelName, Config config, MessageChannel mqttChannel) {

		BeanDefinitionBuilder messageProducerBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MqttPahoMessageDrivenChannelAdapter.class);
		messageProducerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
		//三个参数。clientId,factory,topics
		messageProducerBuilder.addConstructorArgValue(config.getConsumerClientId());
		//生成消费者的bean
		messageProducerBuilder
				.addConstructorArgValue(mqttPahoClientFactorySetting().mqttClientFactory(channelName, config, true));

		messageProducerBuilder.addConstructorArgValue(config.getTopics());

		//这段不能注释，要不拿不到payload的类型
		String payloadType = config.getPayloadType();
		if(ObjectUtils.isEmpty(payloadType)){
			messageProducerBuilder.addPropertyValue("converter", new DefaultPahoMessageConverter());
		}else if("BYTE".equals(payloadType)){
			DefaultPahoMessageConverter defaultPahoMessageConverter = new DefaultPahoMessageConverter();
			defaultPahoMessageConverter.setPayloadAsBytes(true);
			messageProducerBuilder.addPropertyValue("converter", defaultPahoMessageConverter);
		}

		messageProducerBuilder.addPropertyValue("qos", config.getQos());
		messageProducerBuilder.addPropertyValue("outputChannel", mqttChannel);


		return messageProducerBuilder.getBeanDefinition();

	}


	/**
	 * mqtt消息驱动转换器
	 *
	 * @param channelName
	 * @param config
	 * @param mqttChannel
	 * @return
	 */
	private AbstractBeanDefinition channelAdapterV5(String channelName, Config config, MessageChannel mqttChannel) {

		BeanDefinitionBuilder messageProducerBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Mqttv5PahoMessageDrivenChannelAdapter.class);

		messageProducerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);

		//生成消费者的bean
		//三个参数。connecttion,clientId,topics
		messageProducerBuilder
				.addConstructorArgValue(mqttPahoClientFactorySetting().mqttV5Connection(channelName,config,true));

		messageProducerBuilder.addConstructorArgValue(config.getConsumerClientId());

		messageProducerBuilder.addConstructorArgValue(config.getTopics());
		String payloadType = config.getPayloadType();

		if(ObjectUtils.isEmpty(payloadType)){
			messageProducerBuilder.addPropertyValue("converter", new StringMessageConverter());
		}else if("BYTE".equals(payloadType)){
			ByteArrayMessageConverter defaultPahoMessageConverter = new ByteArrayMessageConverter();
//			defaultPahoMessageConverter.setPayloadAsBytes(true);
			messageProducerBuilder.addPropertyValue("messageConverter", defaultPahoMessageConverter);
		}


		messageProducerBuilder.addPropertyValue("qos", config.getQos());
		messageProducerBuilder.addPropertyValue("outputChannel", mqttChannel);
		return messageProducerBuilder.getBeanDefinition();

	}

	/**
	 * 消息发送客户端
	 * 
	 * @param channelName
	 * @param config
	 * @return
	 */
	private AbstractBeanDefinition mqttOutbound(String channelName, Config config) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MqttPahoMessageHandler.class);
		builder.addConstructorArgValue(config.getProducerClientId());
		//这里调用了mqttClientFactory工厂类
		builder.addConstructorArgValue(mqttPahoClientFactorySetting().mqttClientFactory(channelName, config, false));
		builder.addPropertyValue("async", config.getAsync());
		builder.addPropertyValue("asyncEvents", config.getAsync());
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		return beanDefinition;
	}


	private AbstractBeanDefinition mqttOutboundV5(String channelName, Config config) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyMqttv5PahoMessageHandler.class);
		//添加参数
		builder.addConstructorArgValue(mqttPahoClientFactorySetting().mqttV5Connection(channelName,config,false));
		builder.addConstructorArgValue(config.getProducerClientId());

		builder.addPropertyValue("async", config.getAsync());
		builder.addPropertyValue("asyncEvents", config.getAsync());
//		// 创建DefaultMqttHeaderMapper的Bean定义
//		BeanDefinitionBuilder headerMapperBuilder = BeanDefinitionBuilder
//				.genericBeanDefinition(DefaultMqttHeaderMapper.class)
//				.addPropertyValue("outboundHeaderNames", new String[]{
//						MqttHeaders.TOPIC,
//						MqttHeaders.QOS,
//						MqttHeaders.RETAINED,
//						"mqtt_userProperties", // 必须包含这个
//						"mqtt_messageExpiryInterval",
//						"mqtt_responseTopic",
//						"mqtt_correlationData",
//						"mqtt_contentType",
//						"mqtt_userProperty_*" // 如果你也想支持前缀方式
//				});
//		// 将HeaderMapper的Bean定义注册到当前handler的Bean定义中
//		builder.addPropertyValue("headerMapper", headerMapperBuilder.getBeanDefinition());

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		return beanDefinition;
	}
}
