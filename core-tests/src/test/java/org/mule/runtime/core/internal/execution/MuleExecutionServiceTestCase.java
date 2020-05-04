/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.execution;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MuleExecutionServiceTestCase extends AbstractMuleContextTestCase {

  @Rule
  public ExpectedException expectedException = none();

  private final Location executableElementLocation = mock(Location.class);
  private final Location nonExecutableElementLocation = mock(Location.class);
  private final Location missingLocation = mock(Location.class);

  private ExecutionService executionService;

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    ConfigurationComponentLocator configurationComponentLocator = mock(ConfigurationComponentLocator.class);

    ExecutableComponent executableComponent = mock(ExecutableComponent.class);
    when(executableComponent.execute(any(Event.class))).thenAnswer(inv -> completedFuture(inv.getArgument(0)));

    Component component = mock(Component.class);

    when(configurationComponentLocator.find(executableElementLocation)).thenReturn(of(executableComponent));
    when(configurationComponentLocator.find(nonExecutableElementLocation)).thenReturn(of(component));
    when(configurationComponentLocator.find(missingLocation)).thenReturn(empty());
    when(configurationComponentLocator.find(any(ComponentIdentifier.class))).thenReturn(emptyList());

    return singletonMap(REGISTRY_KEY, configurationComponentLocator);
  }

  @Before
  public void setUp() throws Exception {
    executionService = new MuleExecutionService();
    muleContext.getInjector().inject(executionService);
  }

  @Test
  public void componentNotFoundRaisesException() {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("not found");
    executionService.execute(missingLocation, null);
  }

  @Test
  public void nonExecutableComponentRaisesException() {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("Located component is not executable");
    executionService.execute(nonExecutableElementLocation, null);
  }

  @Test
  public void foundComponentIsExecuted() throws Exception {
    final Event event = mock(Event.class);
    CompletableFuture<Event> resultFuture = executionService.execute(executableElementLocation, event);
    assertThat(resultFuture.get(), is(equalTo(event)));
  }


}
