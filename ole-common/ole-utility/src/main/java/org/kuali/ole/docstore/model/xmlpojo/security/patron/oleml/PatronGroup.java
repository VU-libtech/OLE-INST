////
//// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
//// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
//// Any modifications to this file will be lost upon recompilation of the source schema.
//// Generated on: 2012.03.15 at 02:03:46 PM IST
////
//
//
//package org.kuali.ole.docstore.model.xmlpojo.security.patron.oleml;
//
//import com.thoughtworks.xstream.annotations.XStreamAlias;
//import com.thoughtworks.xstream.annotations.XStreamImplicit;
//
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlType;
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * A sequence of patrons wrapped in a patronGroup. Also valid to have a
// *                 single patron.
// *
// * <p>Java class for patronGroup complex type.
// *
// * <p>The following schema fragment specifies the expected content contained within this class.
// *
// * <pre>
// * &lt;complexType name="patronGroup">
// *   &lt;complexContent>
// *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
// *       &lt;sequence>
// *         &lt;element name="patron" type="{http://ole.kuali.org/standards/ole-patron}patron" maxOccurs="unbounded"/>
// *       &lt;/sequence>
// *     &lt;/restriction>
// *   &lt;/complexContent>
// * &lt;/complexType>
// * </pre>
// *
// *
// * @author Rajesh Chowdary K
// * @created Mar 15, 2012
// */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "patronGroup", propOrder = {
//    "patron"
//})
//@XStreamAlias("patronGroup")
//public class PatronGroup {
//
//
//    @XmlElement(required = true)
//    @XStreamImplicit
//    protected List<Patron> patron = new ArrayList<Patron>();
//
//
//    public List<Patron> getPatron() {
//        System.out.println("get in patron group");
//        if (patron == null) {
//            patron = new ArrayList<Patron>();
//        }
//        return patron;
//    }
//
//     public void setPatron(List<Patron> patron) {
//         System.out.println("set in patron group");
//        this.patron = patron;
//    }
//
//}
