/*
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentripplanner.model;

import java.io.Serializable;

public final class Transfer implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MISSING_VALUE = -999;

    private Stop fromStop;

    private Route fromRoute;

    private Trip fromTrip;

    private Stop toStop;

    private Route toRoute;

    private Trip toTrip;

    private int transferType;

    private int minTransferTime = MISSING_VALUE;

    private boolean staySeated = false;

    private boolean planned = true;

    private boolean guaranteed = false;

    private boolean advertised = false;

    //TODO maximumWaitTime

    public Transfer() {
    }

    public Transfer(Transfer obj) {
        this.fromStop = obj.fromStop;
        this.fromRoute = obj.fromRoute;
        this.fromTrip = obj.fromTrip;
        this.toStop = obj.toStop;
        this.toRoute = obj.toRoute;
        this.toTrip = obj.toTrip;
        this.transferType = obj.transferType;
        this.minTransferTime = obj.minTransferTime;
        this.staySeated = obj.staySeated;
        this.planned = obj.planned;
        this.guaranteed = obj.guaranteed;
        this.advertised = obj.advertised;
    }

    public Stop getFromStop() {
        return fromStop;
    }

    public void setFromStop(Stop fromStop) {
        this.fromStop = fromStop;
    }

    public Route getFromRoute() {
        return fromRoute;
    }

    public void setFromRoute(Route fromRoute) {
        this.fromRoute = fromRoute;
    }

    public Trip getFromTrip() {
        return fromTrip;
    }

    public void setFromTrip(Trip fromTrip) {
        this.fromTrip = fromTrip;
    }

    public Stop getToStop() {
        return toStop;
    }

    public void setToStop(Stop toStop) {
        this.toStop = toStop;
    }

    public Route getToRoute() {
        return toRoute;
    }

    public void setToRoute(Route toRoute) {
        this.toRoute = toRoute;
    }

    public Trip getToTrip() {
        return toTrip;
    }

    public void setToTrip(Trip toTrip) {
        this.toTrip = toTrip;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public boolean isMinTransferTimeSet() {
        return minTransferTime != MISSING_VALUE;
    }

    public int getMinTransferTime() {
        return minTransferTime;
    }

    public void setMinTransferTime(int minTransferTime) {
        this.minTransferTime = minTransferTime;
    }

    public void clearMinTransferTime() {
        this.minTransferTime = MISSING_VALUE;
    }

    public boolean isStaySeated() { return staySeated; }

    public void setStaySeated(boolean staySeated) { this.staySeated = staySeated; }

    public boolean isPlanned() { return planned; }

    public void setPlanned(boolean planned) { this.planned = planned; }

    public boolean isGuaranteed() { return guaranteed; }

    public void setGuaranteed(boolean guaranteed) { this.guaranteed = guaranteed; }

    public boolean isAdvertised() { return advertised; }

    public void setAdvertised(boolean advertised) { this.advertised = advertised; }

    public String toString() {
        return "<Transfer"
                + toStrOpt(" stop=", fromStop, toStop)
                + toStrOpt(" route=", fromRoute, toRoute)
                + toStrOpt(" trip=", fromTrip, toTrip)
                + ">";
    }

    private static String toStrOpt(String lbl, IdentityBean arg1, IdentityBean arg2) {
        return  (arg1 == null ? "" : (lbl + arg1.getId() + ".." + arg2.getId()));
    }
}
