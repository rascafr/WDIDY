/**
 * @file BusRequest.java
 * @brief Describes an object for transporting a query on the event bus
 *
 * @author François LEPAROUX
 *
 */
package com.wdidy.app.bus;

import android.os.Bundle;

public class BusRequest {

    /**
     * Request identifier
     */
    private BusType requestType;

    /**
     * Bundle data to transport
     */
    private Bundle data;

    /**
     * Main constructor
     * @param requestType the request identifier
     */
    public BusRequest(BusType requestType) {
        this.requestType = requestType;
        this.data = new Bundle();
    }

    /**
     * Returns the request type (identifier)
     * @return the request id
     */
    public BusType getRequestType() {
        return requestType;
    }

    /**
     * Returns the request data
     * @return the bundled data
     */
    public Bundle getData() {
        return data;
    }
}
