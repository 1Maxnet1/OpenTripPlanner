package org.opentripplanner.routing.algorithm.astar.strategies;

import java.time.Duration;
import org.opentripplanner.astar.model.Edge;
import org.opentripplanner.astar.spi.SkipEdgeStrategy;
import org.opentripplanner.routing.core.State;

/**
 * Skips edges when the specified number of seconds has elapsed. This does not guarantee that we get
 * all relevant results up to the specified duration, as the only criterion we optimize on is
 * generalized cost.
 */
public class DurationSkipEdgeStrategy implements SkipEdgeStrategy<State, Edge> {

  private final double durationInSeconds;

  public DurationSkipEdgeStrategy(Duration duration) {
    this.durationInSeconds = duration.toSeconds();
  }

  @Override
  public boolean shouldSkipEdge(State current, Edge edge) {
    return current.getElapsedTimeSeconds() > durationInSeconds;
  }
}
