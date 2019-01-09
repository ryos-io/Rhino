/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk;

import java.util.List;

/**
 * Scanner, used to search for annotated benchmark entities within the package provided. The with
 * {@link com.adobe.rhino.sdk.annotations.Simulation} annotated entities will be packaged along
 * with the SDK into a JAR file, so the scanner searches for entities in the JAR artifact.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @see com.adobe.rhino.sdk.annotations.Simulation
 * @see Simulation
 * @since 1.0
 */
public interface SimulationJobsScanner {

    /**
     * Scanner method which takes a list of paths to be scanned for benchmark entities and
     * returns a list of {@link Simulation} instances.
     *
     * @param inPackages The path to scan for entities.
     * @param forSimulation Simulation name.
     * @return A list of benchmark job instances.
     */
    List<Simulation> scan(String forSimulation, String... inPackages);

    /**
     * Factory method to create new {@link SimulationJobsScanner} instances.
     *
     * @return An instance of the scanner.
     */
    static SimulationJobsScanner create() {
        return new SimulationJobsScannerImpl();
    }
}
