package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendCohortSizeRequest;
import org.highmed.dsf.bpe.message.SendCohortSizeResultToMedic;
import org.highmed.dsf.bpe.message.SendCohortSizeResultToResearcher;
import org.highmed.dsf.bpe.message.SendErrorMessage;
import org.highmed.dsf.bpe.plugin.SimpleFeasibilityPlugin;
import org.highmed.dsf.bpe.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleFeasibilityConfig
{

    @Bean
    public ProcessEnginePlugin feasibilityPlugin()
    {
        return new SimpleFeasibilityPlugin();
    }

    @Bean
    public CheckCohortSizeRequest checkCohortSizeRequest()
    {
        return new CheckCohortSizeRequest();
    }

    @Bean
    public SelectTargetMedics selectTargetMedics()
    {
        return new SelectTargetMedics();
    }

    @Bean
    public ExecuteCohortSizeQuery executeCohortSizeQuery() {
        return new ExecuteCohortSizeQuery();
    }

    @Bean
    public CalculateMultiMedicCohortSize calculateMultiMedicCohortSize()
    {
        return new CalculateMultiMedicCohortSize();
    }

    @Bean
    public CheckCohortSizeResult checkResult() {
        return new CheckCohortSizeResult();
    }

    @Bean
    public SendCohortSizeRequest sendCohortSizeRequest() {
        return new SendCohortSizeRequest();
    }

    @Bean
    public SendCohortSizeResultToMedic sendCohortSizeResultToMedic() {
        return new SendCohortSizeResultToMedic();
    }

    @Bean
    public SendCohortSizeResultToResearcher sendCohortSizeResultToResearcher() {
        return new SendCohortSizeResultToResearcher();
    }

    @Bean
    public SendErrorMessage sendErrorMessage() {
        return new SendErrorMessage();
    }
}
