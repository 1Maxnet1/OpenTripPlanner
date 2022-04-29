package org.opentripplanner.ext.transmodelapi.model.timetable;

import static org.opentripplanner.ext.transmodelapi.model.EnumTypes.SERVICE_ALTERATION;

import graphql.Scalars;
import graphql.schema.*;
import org.opentripplanner.ext.transmodelapi.support.GqlUtil;
import org.opentripplanner.model.TripOnServiceDate;

/**
 * A DatedServiceJourney GraphQL Type for use in endpoints fetching DatedServiceJourney data
 */
public class DatedServiceJourneyType {

    private static final String NAME = "DatedServiceJourney";
    public static final GraphQLTypeReference REF = new GraphQLTypeReference(NAME);

    public static GraphQLObjectType create(GraphQLOutputType serviceJourneyType) {
        return GraphQLObjectType.newObject()
                .name(NAME)
                .description("A planned journey on a specific day")
                .field(GqlUtil.newTransitIdField())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("OperatingDayDate")
                        .description("The date this service runs. The date used is based on the service date as opposed to calendar date.")
                        .type(Scalars.GraphQLString)
                        .dataFetcher(environment -> (
                                tripOnServiceDate(environment).getServiceDate().toString()
                        ))
                )
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("ServiceJourney")
                        .description("The service journey this Dated Service Journey is based on")
                        .type(new GraphQLNonNull(serviceJourneyType))
                        .dataFetcher(environment -> (
                                        tripOnServiceDate(environment).getTrip()
                                )
                        ))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("TripAlteration")
                        .description("Alterations specified on the Trip in the planned data")
                        .type(SERVICE_ALTERATION)
                        .dataFetcher(environment -> tripOnServiceDate(environment).getTripAlteration())
                ).field(GraphQLFieldDefinition
                        .newFieldDefinition()
                        .name("replacementFor")
                        .description("List of the dated service journeys this dated service journeys replaces")
                        .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(REF))))
                        .dataFetcher(environment -> tripOnServiceDate(environment).getReplacementFor())
                )
                .build();
    }

    private static TripOnServiceDate tripOnServiceDate(DataFetchingEnvironment environment) {
        return environment.getSource();
    }
}