/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;
import static org.mule.runtime.api.component.execution.ExecutionService.EXECUTION_SERVICE_KEY;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.SystemProperty;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MuleExecutionServiceFunctionalTestCase extends MuleArtifactFunctionalTestCase {

  private static final String SET_PAYLOAD_FLOW_NAME = "set-payload-flow";
  private static final String FILE_FLOW_NAME = "file-flow";
  private static final String FILE_CONFIG_NAME = "fileConfig";

  @Rule
  public SystemProperty workingDir = new SystemProperty("workingDir",
                                                        this.getClass().getClassLoader().getResource(".").getPath());

  @Rule
  public ExpectedException expectedException = none();

  @Inject
  @Named(EXECUTION_SERVICE_KEY)
  private ExecutionService executionService;

  @Override
  protected String getConfigFile() {
    return "execution-service-config.xml";
  }

  @Test
  public void executeMissingLocation() throws Exception{
    final Location location = Location.builder().globalName("missing").build();
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("not found");
    executionService.execute(location, testEvent()).get();
  }

  @Test
  public void executeNonExecutable() throws Exception{
    final Location location = Location.builder().globalName(FILE_CONFIG_NAME).build();
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("Located component is not executable");
    executionService.execute(location, testEvent()).get();
  }

  @Test
  public void executeSetPayload() throws Exception {
    final Location location = Location.builder().globalName(SET_PAYLOAD_FLOW_NAME).addProcessorsPart().addIndexPart(0).build();
    final Event result = executionService.execute(location, testEvent()).get();
    assertThat(result.getMessage().getPayload().getValue(), is(equalTo("SUCCESS")));
  }

  @Test
  public void executeFlow() throws Exception {
    final Location location = Location.builder().globalName(SET_PAYLOAD_FLOW_NAME).build();
    final Event result = executionService.execute(location, testEvent()).get();
    assertThat(result.getMessage().getPayload().getValue(), is(equalTo("SUCCESS")));
  }

  @Test
  public void executeStandalonePluginOperation() throws Exception {
    final Location location = Location.builder().globalName(FILE_FLOW_NAME).addProcessorsPart().addIndexPart(0).build();
    final Event result = executionService.execute(location, testEvent()).get();
    assertThat(IOUtils.toString((CursorStreamProvider) result.getMessage().getPayload().getValue()), is(equalTo("SUCCESS")));
  }

  @Test
  public void executeStandaloneConfigAwarePluginOperation() throws Exception {
    final Location location = Location.builder().globalName(FILE_FLOW_NAME).addProcessorsPart().addIndexPart(1).build();
    final Event result = executionService.execute(location, testEvent()).get();
    assertThat(IOUtils.toString((CursorStreamProvider) result.getMessage().getPayload().getValue()), is(equalTo("SUCCESS")));
  }

}
