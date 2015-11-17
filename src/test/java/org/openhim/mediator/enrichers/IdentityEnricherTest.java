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

/**
 */
public class IdentityEnricherTest extends XMLTestCase {

    @Test
    public void testEnrich() throws Exception {
        InputStream dvs1in = getClass().getClassLoader().getResourceAsStream("dataValueSet1.xml");
        String dvs1 = IOUtils.toString(dvs1in);

        IdentityEnricher enricher = new IdentityEnricher();

        String result = enricher.enrich(dvs1);
        assertXMLEqual("XML input should equal output", dvs1, result);
    }
}