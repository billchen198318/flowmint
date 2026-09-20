package org.qifu.fm.flowable;

import java.util.ArrayList;

import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.logic.IFmGroovyCancellationLogicService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "flowmint.groovy.recovery.execution-deletion-enabled", havingValue = "true")
public class FmGroovyExecutionDeletionConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> groovyExecutionDeletionConfigurer(
            ObjectProvider<IFmGroovyCancellationLogicService> cancellation,
            ObjectProvider<FmGroovyFlowableTransactionGuard> guard) {
        return configuration -> {
            var listeners = new ArrayList<FlowableEventListener>();
            if (configuration.getEventListeners() != null) {
                listeners.addAll(configuration.getEventListeners());
            }
            listeners.add(new FmGroovyExecutionDeletionListener(cancellation, guard));
            configuration.setEventListeners(listeners);
        };
    }
}
