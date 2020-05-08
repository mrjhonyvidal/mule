/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
