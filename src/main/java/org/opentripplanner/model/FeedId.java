/*
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google Inc.
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

public class FeedId implements Serializable, Comparable<FeedId> {

    public static final char ID_SEPARATOR = '_';

    private static final long serialVersionUID = 1L;

    private String agencyId;

    private String id;

    public FeedId() {

    }

    public FeedId(String agencyId, String id) {
        this.agencyId = agencyId;
        this.id = id;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasValues() {
        return this.agencyId != null && this.id != null;
    }

    public int compareTo(FeedId o) {
        int c = this.agencyId.compareTo(o.agencyId);
        if (c == 0)
            c = this.id.compareTo(o.id);
        return c;
    }

    /**
     * Given an id of the form "agencyId_entityId", parses into a
     * {@link FeedId} id object.
     *
     * @param value id of the form "agencyId_entityId"
     * @return an id object
     */
    public static FeedId convertFromString(String value, char separator) {
        int index = value.indexOf(separator);
        if (index == -1) {
            throw new IllegalStateException("invalid agency-and-id: " + value);
        } else {
            return new FeedId(value.substring(0, index), value.substring(index + 1));
        }
    }

    @Override
    public int hashCode() {
        return agencyId.hashCode() ^ id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FeedId))
            return false;
        FeedId other = (FeedId) obj;
        if (!agencyId.equals(other.agencyId))
            return false;
        if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return convertToString(this);
    }

    /****
     *
     ****/

    /**
     * Given an id of the form "agencyId_entityId", parses into a
     * {@link FeedId} id object.
     *
     * @param value id of the form "agencyId_entityId"
     * @return an id object
     * @throws IllegalArgumentException if the id cannot be parsed
     */
    public static FeedId convertFromString(String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty())
            return null;
        int index = value.indexOf(ID_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("invalid agency-and-id: " + value);
        } else {
            return new FeedId(value.substring(0, index), value.substring(index + 1));
        }
    }

    /**
     * Given an {@link FeedId} object, creates a string representation of the
     * form "agencyId_entityId"
     *
     * @param aid an id object
     * @return a string representation of the form "agencyId_entityId"
     */
    public static String convertToString(FeedId aid) {
        if (aid == null)
            return null;
        return aid.getAgencyId() + ID_SEPARATOR + aid.getId();
    }
}
