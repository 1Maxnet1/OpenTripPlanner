package org.opentripplanner.street.model.vertex;

import javax.annotation.Nonnull;
import org.opentripplanner.framework.i18n.I18NString;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.transit.model.site.StationElement;

public abstract class StationElementVertex extends Vertex {

  protected StationElementVertex(Graph g, double x, double y, I18NString name) {
    super(g, x, y, name);
  }

  /** Get the corresponding StationElement */
  @Nonnull
  public abstract StationElement getStationElement();
}
