package org.mule.runtime.core.internal.execution;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.execution.ExecutableComponent;
import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleRuntimeException;

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
    if(component instanceof ExecutableComponent) {
      final ExecutableComponent executableComponent = (ExecutableComponent)component;
      return executableComponent.execute(event);
    }
    throw new MuleRuntimeException(createStaticMessage("Located component is not executable"));
  }
}
