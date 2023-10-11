package org.opentripplanner.ext.emissions.digitransit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.opentripplanner.transit.model._data.TransitModelForTest.id;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentripplanner._support.time.ZoneIds;
import org.opentripplanner.ext.digitransitemissions.DigitransitEmissions;
import org.opentripplanner.ext.digitransitemissions.DigitransitEmissionsService;
import org.opentripplanner.ext.digitransitemissions.EmissionsDataModel;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.plan.ScheduledTransitLeg;
import org.opentripplanner.model.plan.StreetLeg;
import org.opentripplanner.street.search.TraverseMode;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.TransitMode;
import org.opentripplanner.transit.model.framework.Deduplicator;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.model.organization.Agency;
import org.opentripplanner.transit.model.timetable.Trip;
import org.opentripplanner.transit.model.timetable.TripTimes;

class EmissionsServiceTest {

  private DigitransitEmissionsService eService;

  static final ZonedDateTime TIME = OffsetDateTime
    .parse("2023-07-20T17:49:06+03:00")
    .toZonedDateTime();

  private static final Agency subject = Agency
    .of(TransitModelForTest.id("F:1"))
    .withName("Foo_CO")
    .withTimezone("Europe/Helsinki")
    .build();

  @BeforeEach
  void SetUp() {
    Map<FeedScopedId, DigitransitEmissions> digitransitEmissions = new HashMap<>();
    digitransitEmissions.put(
      new FeedScopedId("F", "1"),
      DigitransitEmissions.newDigitransitEmissions(0.12, 12)
    );
    digitransitEmissions.put(
      new FeedScopedId("F", "2"),
      DigitransitEmissions.newDigitransitEmissions(0, 0)
    );
    EmissionsDataModel emissionsDataModel = new EmissionsDataModel();
    emissionsDataModel.setDigitransitEmissions(digitransitEmissions);
    emissionsDataModel.setCarAvgCo2EmissionsPerMeter(0.131);
    this.eService = new DigitransitEmissionsService(emissionsDataModel);
  }

  @Test
  void testGetEmissionsForItinerary() {
    var stopOne = TransitModelForTest.stopForTest("1:stop1", 60, 25);
    var stopTwo = TransitModelForTest.stopForTest("1:stop1", 61, 25);
    var stopThree = TransitModelForTest.stopForTest("1:stop1", 62, 25);
    var stopPattern = TransitModelForTest.stopPattern(stopOne, stopTwo, stopThree);
    var route = TransitModelForTest.route(id("1")).build();
    List<Leg> legs = new ArrayList<>();
    var pattern = TransitModelForTest.tripPattern("1", route).withStopPattern(stopPattern).build();
    var stoptime = new StopTime();
    var stoptimes = new ArrayList<StopTime>();
    stoptimes.add(stoptime);
    var trip = Trip
      .of(FeedScopedId.parse("FOO:BAR"))
      .withMode(TransitMode.BUS)
      .withRoute(route)
      .build();
    var leg = new ScheduledTransitLeg(
      new TripTimes(trip, stoptimes, new Deduplicator()),
      pattern,
      0,
      2,
      TIME,
      TIME.plusMinutes(10),
      TIME.toLocalDate(),
      ZoneIds.BERLIN,
      null,
      null,
      100,
      null
    );
    legs.add(leg);
    Itinerary i = new Itinerary(legs);
    assertEquals(2223.902, eService.getEmissionsForItinerary(i));
  }

  @Test
  void testGetEmissionsForCarRoute() {
    List<Leg> legs = new ArrayList<>();
    var leg = StreetLeg
      .create()
      .withMode(TraverseMode.CAR)
      .withDistanceMeters(214.4)
      .withStartTime(TIME)
      .withEndTime(TIME.plus(1, ChronoUnit.HOURS))
      .build();
    legs.add(leg);
    Itinerary i = new Itinerary(legs);
    assertEquals(28.0864, eService.getEmissionsForItinerary(i));
  }

  @Test
  void testNoEmissionsForFeedWithoutEmissionsConfigured() {
    Map<FeedScopedId, DigitransitEmissions> digitransitEmissions = new HashMap<>();
    digitransitEmissions.put(
      new FeedScopedId("G", "1"),
      DigitransitEmissions.newDigitransitEmissions(0.12, 12)
    );
    EmissionsDataModel emissionsDataModel = new EmissionsDataModel();
    emissionsDataModel.setDigitransitEmissions(digitransitEmissions);
    emissionsDataModel.setCarAvgCo2EmissionsPerMeter(0.131);

    this.eService = new DigitransitEmissionsService(emissionsDataModel);

    var route = TransitModelForTest.route(id("1")).withAgency(subject).build();
    List<Leg> legs = new ArrayList<>();
    var pattern = TransitModelForTest
      .tripPattern("1", route)
      .withStopPattern(TransitModelForTest.stopPattern(3))
      .build();
    var stoptime = new StopTime();
    var stoptimes = new ArrayList<StopTime>();
    stoptimes.add(stoptime);
    var trip = Trip
      .of(FeedScopedId.parse("FOO:BAR"))
      .withMode(TransitMode.BUS)
      .withRoute(route)
      .build();
    var leg = new ScheduledTransitLeg(
      new TripTimes(trip, stoptimes, new Deduplicator()),
      pattern,
      0,
      2,
      TIME,
      TIME.plusMinutes(10),
      TIME.toLocalDate(),
      ZoneIds.BERLIN,
      null,
      null,
      100,
      null
    );
    legs.add(leg);
    Itinerary i = new Itinerary(legs);
    assertNull(eService.getEmissionsForItinerary(i));
  }

  @Test
  void testZeroEmissionsForItineraryWithZeroEmissions() {
    var stopOne = TransitModelForTest.stopForTest("1:stop1", 60, 25);
    var stopTwo = TransitModelForTest.stopForTest("1:stop1", 61, 25);
    var stopThree = TransitModelForTest.stopForTest("1:stop1", 62, 25);
    var stopPattern = TransitModelForTest.stopPattern(stopOne, stopTwo, stopThree);
    var route = TransitModelForTest.route(id("2")).build();
    List<Leg> legs = new ArrayList<>();
    var pattern = TransitModelForTest.tripPattern("1", route).withStopPattern(stopPattern).build();
    var stoptime = new StopTime();
    var stoptimes = new ArrayList<StopTime>();
    stoptimes.add(stoptime);
    var trip = Trip
      .of(FeedScopedId.parse("FOO:BAR"))
      .withMode(TransitMode.BUS)
      .withRoute(route)
      .build();
    var leg = new ScheduledTransitLeg(
      new TripTimes(trip, stoptimes, new Deduplicator()),
      pattern,
      0,
      2,
      TIME,
      TIME.plusMinutes(10),
      TIME.toLocalDate(),
      ZoneIds.BERLIN,
      null,
      null,
      100,
      null
    );
    legs.add(leg);
    Itinerary i = new Itinerary(legs);
    assertEquals(0, eService.getEmissionsForItinerary(i));
  }
}
