/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.execution;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.internal.event.EventQuickCopy.quickCopy;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

/**
 * Default implementation of {@link ExecutionService} that works on deployed Mule Applications.
 *
 * @since 4.4.0
 */
public class MuleExecutionService implements ExecutionService {

  @Inject
  private ConfigurationComponentLocator componentLocator;

  @Override
  public CompletableFuture<Event> execute(Location location, Event event) {
    return componentLocator
        .find(location)
        .map(c -> execute(c, event))
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Component with location %s not found", location)));
  }

  private CompletableFuture<Event> execute(Component component, Event event) {
    if (component instanceof ExecutableComponent) {
      final ExecutableComponent executableComponent = (ExecutableComponent) component;
      return executableComponent.execute(event);
    } else if (component instanceof Processor) {
      final Processor processor = (Processor) component;
      CoreEvent coreEvent;
      if (event instanceof CoreEvent) {
        coreEvent = (CoreEvent) event;
      } else {
        coreEvent = CoreEvent.builder(event.getContext())
            .message(event.getMessage())
            .error(event.getError().orElse(null))
            .variables(event.getVariables())
            .build();
      }
      try {
        return completedFuture(processor.process(coreEvent));
      } catch (MuleException e) {
        throw new MuleRuntimeException(e);
      }

    }
    throw new MuleRuntimeException(createStaticMessage("Located component is not executable"));
  }
}
