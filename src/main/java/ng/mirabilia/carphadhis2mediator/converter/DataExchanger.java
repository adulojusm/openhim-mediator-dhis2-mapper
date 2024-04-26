/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ng.mirabilia.carphadhis2mediator.converter;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.StringWriter;


public class DataExchanger {

    protected void processXMLEvent(XMLEventWriter out, XMLEvent event) throws XMLStreamException {
        out.add(event);
    }

    public String dataProcess(String json) throws XMLStreamException {

        String xmlString = null;


        try {
            // Parse JSON string
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            // Extract data from JSON
            JSONObject data = (JSONObject) jsonObject.get("data");
            String location = (String) data.get("location");
            String dateOfEntry = (String) data.get("dateofentry");
            JSONObject values = (JSONObject) data.get("values");
            int bcgGiven = ((Long) values.get("bcgGiven")).intValue();
            boolean yrVaccinated = (boolean) values.get("YrVaccinated");

            // Generate XML
            xmlString = "<dataValueSet xmlns=\"http://dhis2.org/schema/dxf/2.0\" dataSet=\"pBOMPrpg1QX\" completeDate=\"" + dateOfEntry + "\" period=\"201401\" orgUnit=\"DiszpKrYNg8\">\n";
            xmlString += "  <dataValue dataElement=\"f7n9E0hX8qk\" value=\"" + bcgGiven + "\"/>\n";
            xmlString += "  <dataValue dataElement=\"Ix2HsbDMLea\" value=\"" + (yrVaccinated ? "true" : "false") + "\"/>\n";
            xmlString += "</dataValueSet>";

            // Output XML
            System.out.println(xmlString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        InputStream in = IOUtils.toInputStream(xmlString);
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
