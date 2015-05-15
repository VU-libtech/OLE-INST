package org.kuali.ole.docstore.common.document;

import org.apache.log4j.Logger;
import org.kuali.ole.docstore.common.document.factory.JAXBContextFactory;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for holdingsTrees complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="holdingsTrees">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="holdingsTrees" type="{}holdingsTree" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "holdingsTrees", propOrder = {
        "holdingsTrees"
})

@XmlRootElement(name = "holdingsDocsTree")
public class HoldingsTrees {

    private static final Logger LOG = Logger.getLogger(HoldingsTrees.class);
    @XmlElement(name = "holdingsDocTree")
    protected List<HoldingsTree> holdingsTrees;

    /**
     * Gets the value of the holdingsTrees property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the holdingsTrees property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHoldingsTrees().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link org.kuali.ole.docstore.common.document.HoldingsTree }
     *
     *
     */
    public List<HoldingsTree> getHoldingsTrees() {
        if (holdingsTrees == null) {
            holdingsTrees = new ArrayList<HoldingsTree>();
        }
        return this.holdingsTrees;
    }


    public static String serialize(Object object) {
        String result = null;
        HoldingsTrees holdingsTrees = (HoldingsTrees) object;
        try {
            StringWriter sw = new StringWriter();
            Marshaller jaxbMarshaller = JAXBContextFactory.getInstance().getMarshaller(HoldingsTrees.class);
            synchronized (jaxbMarshaller) {
                jaxbMarshaller.marshal(holdingsTrees, sw);
            }
            result = sw.toString();
        } catch (Exception e) {
            LOG.error("Exception ", e);
        }
        return result;
    }

    public static Object deserialize(String holdingsTreesXml) {
        HoldingsTrees holdingsTrees = new HoldingsTrees();
        try {
            ByteArrayInputStream bibTreeInputStream = new ByteArrayInputStream(holdingsTreesXml.getBytes());
            StreamSource streamSource = new StreamSource(bibTreeInputStream);
            XMLStreamReader xmlStreamReader = JAXBContextFactory.getInstance().getXmlInputFactory().createXMLStreamReader(streamSource);
            Unmarshaller unmarshaller = JAXBContextFactory.getInstance().getUnMarshaller(HoldingsTrees.class);
            synchronized (unmarshaller) {
                holdingsTrees = unmarshaller.unmarshal(xmlStreamReader, HoldingsTrees.class).getValue();
            }
        } catch (Exception e) {
            LOG.error("Exception ", e);
        }
        return holdingsTrees;
    }

}
