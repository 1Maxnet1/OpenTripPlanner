package org.opentripplanner.routing.algorithm.raptor.transit.mappers;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import org.opentripplanner.ext.sorlandsbanen.EnturHackSorlandsBanen;
import org.opentripplanner.routing.algorithm.raptor.transit.SlackProvider;
import org.opentripplanner.routing.algorithm.raptor.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptor.transit.TripSchedule;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.transit.raptor.api.request.Optimization;
import org.opentripplanner.transit.raptor.api.request.RaptorProfile;
import org.opentripplanner.transit.raptor.api.request.RaptorRequest;
import org.opentripplanner.transit.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.transit.raptor.api.transit.RaptorTransfer;
import org.opentripplanner.util.OTPFeature;

public class RaptorRequestMapper {
    private final RoutingRequest request;
    private final Collection<? extends RaptorTransfer> accessPaths;
    private final Collection<? extends RaptorTransfer> egressPaths;
    private final TransitLayer transitLayer;
    private final long transitSearchTimeZeroEpocSecond;

    private RaptorRequestMapper(
            RoutingRequest request,
            Collection<? extends RaptorTransfer> accessPaths,
            Collection<? extends RaptorTransfer> egressPaths,
            TransitLayer transitLayer,
            long transitSearchTimeZeroEpocSecond
    ) {
        this.request = request;
        this.accessPaths = accessPaths;
        this.egressPaths = egressPaths;
        this.transitLayer = transitLayer;
        this.transitSearchTimeZeroEpocSecond = transitSearchTimeZeroEpocSecond;
    }

    public static RaptorRequest<TripSchedule> mapRequest(
            RoutingRequest request,
            ZonedDateTime transitSearchTimeZero,
            Collection<? extends RaptorTransfer> accessPaths,
            Collection<? extends RaptorTransfer> egressPaths,
            TransitLayer transitLayer
    ) {
        return new RaptorRequestMapper(
                request,
                accessPaths,
                egressPaths,
                transitLayer,
                transitSearchTimeZero.toEpochSecond()
        ).doMap();
    }

    private RaptorRequest<TripSchedule> doMap(
    ) {
        var builder = new RaptorRequestBuilder<TripSchedule>();
        var searchParams = builder.searchParams();

        if(request.pageCursor ==  null) {
            int time = relativeTime(request.getDateTime());
            if (request.arriveBy) {
                searchParams.latestArrivalTime(time);
            }
            else {
                searchParams.earliestDepartureTime(time);
            }
            searchParams.searchWindow(request.searchWindow);
        }
        else {
            var c = request.pageCursor;

            if (c.earliestDepartureTime != null) {
                searchParams.earliestDepartureTime(relativeTime(c.earliestDepartureTime));
            }
            if (c.latestArrivalTime != null) {
                searchParams.latestArrivalTime(relativeTime(c.latestArrivalTime));
            }
            searchParams.searchWindow(c.searchWindow);
        }

        if(request.maxTransfers != null) {
            searchParams.maxNumberOfTransfers(request.maxTransfers);
        }

        builder
                .profile(RaptorProfile.MULTI_CRITERIA)
                .enableOptimization(Optimization.PARETO_CHECK_AGAINST_DESTINATION)
                .slackProvider(new SlackProvider(
                        request.transferSlack,
                        request.boardSlack,
                        request.boardSlackForMode,
                        request.alightSlack,
                        request.alightSlackForMode
                ));

        builder
                .searchParams()
                .timetableEnabled(request.timetableView)
                .constrainedTransfersEnabled(OTPFeature.TransferConstraints.isOn())
                .addAccessPaths(accessPaths)
                .addEgressPaths(egressPaths);

        if(!request.timetableView && request.arriveBy) {
            builder.searchParams().preferLateArrival(true);
        }

        return EnturHackSorlandsBanen.enableHack(builder.build(), request, transitLayer);
    }

    private int relativeTime(Instant time) {
        return (int)(time.getEpochSecond() - transitSearchTimeZeroEpocSecond);
    }
}
