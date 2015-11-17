/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator.enrichers;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class DXFEnricherTest extends XMLTestCase {

    static Map<String, String> dataSetsMap;
    static Map<String, String> dataElementsMap;
    static Map<String, String> orgUnitsMap;
    static Map<String, String> programsMap;

    static {
        dataSetsMap = new HashMap<>();
        dataSetsMap.put("dataValueSet1", "rFwxFuQxqMK");
        dataElementsMap = new HashMap<>();
        dataElementsMap.put("dataElement1", "9f6W1b7Q7y9");
        dataElementsMap.put("dataElement2", "0g0B7m1C3q2");
        dataElementsMap.put("dataElement3", "9n3Y3d1E6i3");
        orgUnitsMap = new HashMap<>();
        orgUnitsMap.put("orgUnit1", "CTs3iHcSTQX");
        programsMap = new HashMap<>();
        programsMap.put("program1", "wIiJOQ7tIlZ");
    }

    @Test
    public void testDataValueSet() throws Exception {
        InputStream dvs1in = getClass().getClassLoader().getResourceAsStream("dataValueSet1.xml");
        String dvs1 = IOUtils.toString(dvs1in);
        InputStream dvs1EnrichedIn = getClass().getClassLoader().getResourceAsStream("dataValueSet1_enriched.xml");
        String dvs1Enriched = IOUtils.toString(dvs1EnrichedIn);

        DXFEnricher enricher = new DXFEnricher(dataSetsMap, dataElementsMap, orgUnitsMap, programsMap);
        String result = enricher.enrich(dvs1);

        assertXMLEqual(dvs1Enriched, result);
    }

    @Test
    public void testTrackerEvent() throws Exception {
        InputStream ev1in = getClass().getClassLoader().getResourceAsStream("event1.xml");
        String ev1 = IOUtils.toString(ev1in);
        InputStream ev1EnrichedIn = getClass().getClassLoader().getResourceAsStream("event1_enriched.xml");
        String ev1Enriched = IOUtils.toString(ev1EnrichedIn);

        DXFEnricher enricher = new DXFEnricher(dataSetsMap, dataElementsMap, orgUnitsMap, programsMap);
        String result = enricher.enrich(ev1);

        assertXMLEqual(ev1Enriched, result);
    }
}