package org.opentripplanner.ext.sorlandsbanen;

import java.util.Collection;
import java.util.function.Function;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.model.WgsCoordinate;
import org.opentripplanner.model.modes.TransitMainMode;
import org.opentripplanner.routing.algorithm.raptor.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptor.transit.TripSchedule;
import org.opentripplanner.routing.algorithm.raptor.transit.cost.FactorStrategy;
import org.opentripplanner.routing.algorithm.raptor.transit.cost.IndexBasedFactorStrategy;
import org.opentripplanner.routing.algorithm.raptor.transit.request.RaptorRoutingRequestTransitData;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.transit.raptor.api.request.RaptorRequest;
import org.opentripplanner.transit.raptor.api.request.SearchParams;
import org.opentripplanner.transit.raptor.api.transit.RaptorTransfer;
import org.opentripplanner.transit.raptor.api.transit.RaptorTransitDataProvider;
import org.opentripplanner.transit.raptor.api.transit.RaptorTripSchedule;
import org.opentripplanner.transit.raptor.api.view.Heuristics;
import org.opentripplanner.transit.raptor.api.view.Worker;
import org.opentripplanner.transit.raptor.rangeraptor.configure.RaptorConfig;

public class EnturHackSorlandsBanen {
    private static final double SOUTH_BOARDER_LIMIT = 59.1;
    private static final int MIN_DISTANCE_LIMIT = 120_000;

    public static <T extends RaptorTripSchedule> boolean match(RaptorRequest<T> mcRequest) {
        return mcRequest.extraSearchCoachReluctance > 0.1;
    }

    public static <T extends RaptorTripSchedule> Worker<T> worker(
            RaptorConfig<T> config,
            RaptorTransitDataProvider<T> transitData,
            RaptorRequest<T> mcRequest,
            Heuristics destinationHeuristics
    ) {
        //noinspection unchecked
        RaptorTransitDataProvider<T> altTransitData =
                (RaptorTransitDataProvider<T>) ((RaptorRoutingRequestTransitData)transitData)
                        .enturHackSorlandsbanen(mapFactors(mcRequest.extraSearchCoachReluctance));

        return new ConcurrentCompositeWorker<>(
                config.createMcWorker(transitData, mcRequest, destinationHeuristics),
                config.createMcWorker(altTransitData, mcRequest, destinationHeuristics)
        );
    }

    public static RaptorRequest<TripSchedule> enableHack(
            RaptorRequest<TripSchedule> raptorRequest,
            RoutingRequest request,
            TransitLayer transitLayer
    ) {
        if(request.extraSearchCoachReluctance < 0.1) { return raptorRequest; }

        SearchParams params = raptorRequest.searchParams();

        WgsCoordinate from = findStopCoordinate(request.from, params.accessPaths(), transitLayer);
        WgsCoordinate to = findStopCoordinate(request.to, params.egressPaths(), transitLayer);

        if(from.latitude() > SOUTH_BOARDER_LIMIT && to.latitude() > SOUTH_BOARDER_LIMIT) {
            return raptorRequest;
        }

        double distanceMeters = SphericalDistanceLibrary.distance(
                from.latitude(), from.longitude(), to.latitude(), to.longitude()
        );

        if(distanceMeters < MIN_DISTANCE_LIMIT) { return raptorRequest; }

        raptorRequest.extraSearchCoachReluctance = request.extraSearchCoachReluctance;
        return raptorRequest;
    }

    /* private methods */


    private static Function<FactorStrategy, FactorStrategy> mapFactors(
            final double extraSearchCoachReluctance
    ) {
        return (FactorStrategy originalFactors) -> {
            int[] modeReluctance = new int[TransitMainMode.values().length];
            for (TransitMainMode mode : TransitMainMode.values()) {
                int index = mode.ordinal();
                int originalFactor = originalFactors.factor(index);
                modeReluctance[index] = mode == TransitMainMode.COACH
                        ? (int)(extraSearchCoachReluctance * originalFactor + 0.5)
                        : originalFactor;
            }
            return new IndexBasedFactorStrategy(modeReluctance);
        };
    }


    /**
     * Find a coordinate matching the given location, in order:
     *  - First return the coordinate of the location if it exists.
     *  - Then loop through the access/egress stops and try to find the
     *    stop or station given by the location id, return the stop/station coordinate.
     *  - Return the fist stop in the access/egress list coordinate.
     */
    @SuppressWarnings("ConstantConditions")
    private static WgsCoordinate findStopCoordinate(
            GenericLocation location,
            Collection<RaptorTransfer> accessEgress,
            TransitLayer transitLayer
    ) {
        if(location.lat != null) {
            return new WgsCoordinate(location.lat, location.lng);
        }

        StopLocation firstStop = null;
        for (RaptorTransfer it : accessEgress) {
            StopLocation stop = transitLayer.getStopByIndex(it.stop());
            if(stop.getId().equals(location.stopId)) {
                return stop.getCoordinate();
            }
            if(idIsParentStation(stop, location.stopId)) {
                return stop.getParentStation().getCoordinate();
            }
            if(firstStop == null) { firstStop = stop; }
        }
        return firstStop.getCoordinate();
    }

    private static boolean idIsParentStation(StopLocation stop, FeedScopedId pId) {
        return stop.getParentStation() != null && stop.getParentStation().getId().equals(pId);
    }
}
