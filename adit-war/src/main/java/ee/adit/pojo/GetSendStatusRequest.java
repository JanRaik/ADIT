//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.07 at 10:40:05 AM EEST 
//

package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ee.adit.pojo.ArrayOfFileType;
import ee.adit.pojo.GetSendStatusRequestDocument;

/**
 * <p>
 * Java class for GetDocumentRequest complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GetDocumentRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="document_id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="include_file_contents" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="file_types" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}ArrayOfFileType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSendStatusRequest", propOrder = {"dokumendid" })
public class GetSendStatusRequest {
    
    @XmlElement(name = "documendid")
    private GetSendStatusRequestDocument document;


	public GetSendStatusRequestDocument getDocument() {
		return document;
	}

	public void setDocument(GetSendStatusRequestDocument document) {
		this.document = document;
	}
    
    
}