package org.mule.test.functional;

import static org.mule.runtime.config.api.SpringXmlConfigurationBuilderFactory.createConfigurationBuilder;
import org.mule.runtime.core.api.config.ConfigurationBuilder;

public class LazyMuleExecutionServiceFunctionalTestCase extends MuleExecutionServiceFunctionalTestCase {

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    final ConfigurationBuilder configurationBuilder = createConfigurationBuilder(getConfigFile(), true);
    configureSpringXmlConfigurationBuilder(configurationBuilder);
    return configurationBuilder;
  }

  @Override
  public boolean enableLazyInit() {
    return true;
  }

}
