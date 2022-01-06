package org.opentripplanner.model;

import java.util.EnumSet;

/**
 * Equivalent to GTFS route_type or to NeTEx TransportMode.
 */
public enum TransitMode {
  RAIL,
  COACH,
  SUBWAY,
  BUS,
  TRAM,
  FERRY,
  AIRPLANE,
  CABLE_CAR,
  GONDOLA,
  FUNICULAR,
  TROLLEYBUS,
  MONORAIL;

  private static final EnumSet<TransitMode> ON_STREET_MODES = EnumSet.of(
          COACH, BUS, TROLLEYBUS
  );


  public boolean onStreet() {
    return ON_STREET_MODES.contains(this);
  }
}
