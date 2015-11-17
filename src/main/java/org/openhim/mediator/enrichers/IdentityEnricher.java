/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator.enrichers;

import org.apache.commons.io.IOUtils;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * An enricher that performs an identity transform on an XML document - input equals output
 *
 * Subclasses can override the processXMLEvent method in order to enrich a document
 */
public class IdentityEnricher {
    protected void processXMLEvent(XMLEventWriter out, XMLEvent event) throws XMLStreamException {
        out.add(event);
    }

    public String enrich(String xml) throws XMLStreamException {
        InputStream in = IOUtils.toInputStream(xml);
        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(in);
        StringWriter output = new StringWriter();
        XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            processXMLEvent(writer, event);
        }

        writer.close();
        return output.toString();
    }
}
