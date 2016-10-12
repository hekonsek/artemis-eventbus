package artemis;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import static org.apache.camel.component.amqp.AMQPComponent.amqpComponent;

@SpringBootApplication
public class Broker {

    @Bean
    ArtemisConfigurationCustomizer artemisConfigurationCustomizer() {
        return new ArtemisConfigurationCustomizer(){
            public void customize(Configuration configuration) {
                try {
                    configuration.addAcceptorConfiguration("amqp-acceptor", "tcp://localhost:5672?protocols=AMQP");
//                    configuration.addAddressesSetting("#", new AddressSettings().setAutoCreateJmsQueues(true).setAutoCreateJmsTopics(true));
                    configuration.addQueueConfiguration(new CoreQueueConfiguration().setAddress("foo").setName("foo"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Bean
    RoutesBuilder amqpRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                getContext().addComponent("amqp", amqpComponent("amqp://localhost:5672"));
                from("amqp:foo").log("hello!");
            }
        };
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.artemis.mode", "native");
        ProducerTemplate producer = new SpringApplicationBuilder(Broker.class).run(args).getBean(ProducerTemplate.class);

        Object response = producer.requestBody("amqp:foo", "xxx");
        System.out.println(response);
    }

}
