package org.opentripplanner.updater.vehicle_rental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opentripplanner.framework.geometry.GeometryUtils;
import org.opentripplanner.routing.vehicle_rental.GeofencingZone;
import org.opentripplanner.street.model.edge.Edge;
import org.opentripplanner.street.model.edge.StreetEdge;
import org.opentripplanner.street.model.edge.StreetEdgeRentalExtension;
import org.opentripplanner.street.model.edge.StreetEdgeRentalExtension.BusinessAreaBorder;
import org.opentripplanner.street.model.edge.StreetEdgeRentalExtension.GeofencingZoneExtension;

class GeofencingEdgeUpdater {

  private final Function<Envelope, Collection<Edge>> getEdgesForEnvelope;

  public GeofencingEdgeUpdater(Function<Envelope, Collection<Edge>> getEdgesForEnvelope) {
    this.getEdgesForEnvelope = getEdgesForEnvelope;
  }

  /**
   * Applies the restrictions described in the geofencing zones to eges by adding
   * {@link StreetEdgeRentalExtension} to them.
   */
  Set<StreetEdge> applyGeofencingZones(List<GeofencingZone> geofencingZones) {
    var restrictedZones = geofencingZones.stream().filter(GeofencingZone::hasRestriction).toList();

    // these are the edges inside business area where exceptions like "no pass through"
    // or "no drop-off" are added
    var restrictedEdges = addExtensionToIntersectingStreetEdges(
      restrictedZones,
      GeofencingZoneExtension::new
    );

    var updatedEdges = new HashSet<>(restrictedEdges);

    var generalBusinessAreas = geofencingZones
      .stream()
      .filter(GeofencingZone::isBusinessArea)
      .toList();

    if (!generalBusinessAreas.isEmpty()) {
      // if the geofencing zones don't have any restrictions then they describe a general business
      // area which you can traverse freely but are not allowed to leave
      // here we just take the boundary of the geometry since we want to add a "no pass through"
      // restriction to any edge intersecting it

      var network = generalBusinessAreas.get(0).id().getFeedId();
      var polygons = generalBusinessAreas
        .stream()
        .map(GeofencingZone::geometry)
        .toArray(Geometry[]::new);

      var unionOfBusinessAreas = GeometryUtils
        .getGeometryFactory()
        .createGeometryCollection(polygons)
        .union();

      var updated = applyExtension(
        unionOfBusinessAreas.getBoundary(),
        new BusinessAreaBorder(network)
      );

      updatedEdges.addAll(updated);
    }

    return updatedEdges;
  }

  private static Envelope toEnvelope(LineString ls) {
    var env = new Envelope();
    for (var c : ls.getCoordinates()) {
      env.expandToInclude(c);
    }
    return env;
  }

  private Collection<StreetEdge> addExtensionToIntersectingStreetEdges(
    List<GeofencingZone> zones,
    Function<GeofencingZone, StreetEdgeRentalExtension> createExtension
  ) {
    var edgesUpdated = new ArrayList<StreetEdge>();
    for (GeofencingZone zone : zones) {
      var geom = zone.geometry();
      var ext = createExtension.apply(zone);
      edgesUpdated.addAll(applyExtension(geom, ext));
    }
    return edgesUpdated;
  }

  private Collection<StreetEdge> applyExtension(Geometry geom, StreetEdgeRentalExtension ext) {
    var edgesUpdated = new ArrayList<StreetEdge>();
    Set<Edge> candidates;
    if (geom instanceof MultiLineString mls) {
      candidates = getEdgesAlongLineStrings(mls);
    } else {
      candidates = Set.copyOf(getEdgesForEnvelope.apply(geom.getEnvelopeInternal()));
    }
    for (var e : candidates) {
      if (e instanceof StreetEdge streetEdge && streetEdge.getGeometry().intersects(geom)) {
        streetEdge.addRentalExtension(ext);
        edgesUpdated.add(streetEdge);
      }
    }
    return edgesUpdated;
  }

  /**
   * This method optimizes finding all the candidate edges which could cross the business zone
   * border.
   * <p>
   * If you put the entire zone into an envelope you get lots and lots of edges in the middle of it
   * that are nowhere near the border. Since checking if they intersect with the border is an
   * expensive operation we apply the following optimization:
   * <li>we split the line string into segments of equal number of coordinates
   * <li>for each segment we compute the envelope
   * <li>we only get the edges for that envelope and check if they intersect
   * <p>
   * When finding the edges near the business area border in Oslo this speeds up the computation
   * from ~25 seconds to ~3 seconds (on 2021 hardware).
   */
  @Nonnull
  private Set<Edge> getEdgesAlongLineStrings(MultiLineString mls) {
    var lineStrings = GeometryUtils.getLineStrings(mls);

    return lineStrings
      .stream()
      .flatMap(ls -> GeometryUtils.partitionLineString(ls, 10).stream())
      .map(GeofencingEdgeUpdater::toEnvelope)
      .map(getEdgesForEnvelope)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
  }
}