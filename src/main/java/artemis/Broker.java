package artemis;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Broker {

    @Bean
    ArtemisConfigurationCustomizer artemisConfigurationCustomizer() {
        return new ArtemisConfigurationCustomizer(){
            public void customize(Configuration configuration) {
                try {
                    configuration.addAcceptorConfiguration("amqp-acceptor", "tcp://localhost:5672?protocols=AMQP");
                    configuration.addQueueConfiguration(new CoreQueueConfiguration().setAddress("foo").setName("foo"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.artemis.mode", "native");
        new SpringApplicationBuilder(Broker.class).run(args);

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addComponent("amqp", AMQPComponent.amqpComponent("amqp://localhost:5672"));
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("amqp:foo").log("hello!");
            }
        });
        camelContext.start();

        Object response = camelContext.createProducerTemplate().requestBody("amqp:foo", "xxx");
        System.out.println(response);
    }

}
