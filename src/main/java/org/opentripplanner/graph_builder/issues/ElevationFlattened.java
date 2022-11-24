package org.opentripplanner.graph_builder.issues;

import org.opentripplanner.street.model.edge.Edge;
import org.opentripplanner.graph_builder.DataImportIssue;

public class ElevationFlattened implements DataImportIssue {

  public static final String FMT = "Edge %s was steeper than Baldwin Street and was flattened.";

  final Edge edge;

  public ElevationFlattened(Edge edge) {
    this.edge = edge;
  }

  @Override
  public String getMessage() {
    return String.format(FMT, edge);
  }
}
