package org.qifu.fm.flowable;

import java.util.ArrayList;

import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FmProcessCompletionConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processCompletionConfigurer(
            ObjectProvider<IFmProcessInstanceService> processInstances) {
        return configuration -> {
            var listeners = new ArrayList<FlowableEventListener>();
            if (configuration.getEventListeners() != null) {
                listeners.addAll(configuration.getEventListeners());
            }
            listeners.add(new FmProcessCompletionListener(processInstances));
            configuration.setEventListeners(listeners);
        };
    }
}
