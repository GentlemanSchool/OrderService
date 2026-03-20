package ru.gentleman.order.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerListenerInvocationErrorHandler("order-group",
                conf -> PropagatingErrorHandler.instance());
    }

//    @Autowired
//    public void registerCustomerCommandInterceptor(ApplicationContext context, CommandGateway commandGateway) {
//        commandGateway.registerDispatchInterceptor(context.getBean(QuizAggregateInterceptor.class));
//        commandGateway.registerDispatchInterceptor(context.getBean(QuizAttemptAggregateInterceptor.class));
//    }
}
