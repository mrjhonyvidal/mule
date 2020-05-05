package org.mule.runtime.config.internal;

import org.mule.runtime.api.component.execution.ExecutionService;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.config.api.LazyComponentInitializer;

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
    componentInitializer.initializeComponent(location);
    return this.executionService.execute(location, event);
  }

  @Override
  public void initialise() throws InitialisationException {
    this.executionService = executionServiceSupplier.get();
  }
}
