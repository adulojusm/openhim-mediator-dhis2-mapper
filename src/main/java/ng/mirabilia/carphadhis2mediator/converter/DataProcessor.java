/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ng.mirabilia.carphadhis2mediator.converter;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;


public class DataProcessor extends DataExchanger {
    private Map<String, String> dataSetsMap;
    private Map<String, String> dataElementsMap;
    private Map<String, String> orgUnitsMap;
    private Map<String, String> programsMap;
    private XMLEventFactory eventFactory = XMLEventFactory.newFactory();

    public DataProcessor(Map<String, String> dataSetsMap, Map<String, String> dataElementsMap, Map<String, String> orgUnitsMap, Map<String, String> programsMap) {
        this.dataSetsMap = dataSetsMap != null ? dataSetsMap : Collections.<String, String>emptyMap();
        this.dataElementsMap = dataElementsMap != null ? dataElementsMap : Collections.<String, String>emptyMap();
        this.orgUnitsMap = orgUnitsMap != null ? orgUnitsMap : Collections.<String, String>emptyMap();
        this.programsMap = programsMap != null ? programsMap : Collections.<String, String>emptyMap();
    }

    @Override
    protected void processXMLEvent(XMLEventWriter out, XMLEvent event) throws XMLStreamException {
        if (event.getEventType() == XMLEvent.START_ELEMENT) {
            StartElement elem = event.asStartElement();
            List<Attribute> attributes = new LinkedList<>();

            Iterator<Attribute> iter = elem.getAttributes();
            while (iter.hasNext()) {
                Attribute attr = iter.next();

                if ("dataSet".equals(attr.getName().getLocalPart()) && dataSetsMap.containsKey(attr.getValue())) {
                    attributes.add(eventFactory.createAttribute("dataSet", dataSetsMap.get(attr.getValue())));
                } else if ("dataElement".equals(attr.getName().getLocalPart()) && dataElementsMap.containsKey(attr.getValue())) {
                    attributes.add(eventFactory.createAttribute("dataElement", dataElementsMap.get(attr.getValue())));
                } else if ("orgUnit".equals(attr.getName().getLocalPart()) && orgUnitsMap.containsKey(attr.getValue())) {
                    attributes.add(eventFactory.createAttribute("orgUnit", orgUnitsMap.get(attr.getValue())));
                } else if ("program".equals(attr.getName().getLocalPart()) && programsMap.containsKey(attr.getValue())) {
                    attributes.add(eventFactory.createAttribute("program", programsMap.get(attr.getValue())));
                } else {
                    attributes.add(attr);
                }
            }

            event = eventFactory.createStartElement(elem.getName(), attributes.iterator(), elem.getNamespaces());
        }

        super.processXMLEvent(out, event);
    }
}
