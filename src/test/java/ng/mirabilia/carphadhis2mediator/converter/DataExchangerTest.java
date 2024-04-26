/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ng.mirabilia.carphadhis2mediator.converter;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;

import java.io.InputStream;

/**
 */
public class DataExchangerTest extends XMLTestCase {

    @Test
    public void testEnrich() throws Exception {
        InputStream dvs1in = getClass().getClassLoader().getResourceAsStream("dataValueSet1.xml");
        String dvs1 = IOUtils.toString(dvs1in);

        DataExchanger enricher = new DataExchanger();

        String result = enricher.dataProcess(dvs1);
        assertXMLEqual("XML input should equal output", dvs1, result);
    }
}