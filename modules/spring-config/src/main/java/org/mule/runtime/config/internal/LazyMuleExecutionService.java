package org.mule.runtime.config.internal;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.config.api.LazyComponentInitializer;
import org.mule.runtime.config.internal.dsl.model.NoSuchComponentModelException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class LazyMuleExecutionService implements ExecutionService, Initialisable {

  private final LazyComponentInitializer componentInitializer;
  private final Supplier<ExecutionService> executionServiceSupplier;

  private ExecutionService executionService;

  public LazyMuleExecutionService(LazyComponentInitializer componentInitializer,
                                  Supplier<ExecutionService> metadataServiceSupplier) {
    this.componentInitializer = componentInitializer;
    this.executionServiceSupplier = metadataServiceSupplier;
  }

  @Override
  public CompletableFuture<Event> execute(Location location, Event event) {
    try {
      componentInitializer.initializeComponent(location);
    }catch (NoSuchComponentModelException e) {
      throw new MuleRuntimeException(createStaticMessage("Component with location %s not found", location));
    }
    return this.executionService.execute(location, event);
  }

  @Override
  public void initialise() throws InitialisationException {
    this.executionService = executionServiceSupplier.get();
  }
}
