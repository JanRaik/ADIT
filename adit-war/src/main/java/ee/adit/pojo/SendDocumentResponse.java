//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.20 at 11:37:41 AM EEST 
//

package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SendDocumentResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SendDocumentResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="recipient_list" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}ArrayOfRecipientStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendDocumentResponse", propOrder = {"success", "recipientList" })
public class SendDocumentResponse {

    protected boolean success;
    @XmlElement(required = true)
    protected ArrayOfMessage messages;
    @XmlElement(name = "recipient_list", required = true)
    protected ArrayOfRecipientStatus recipientList;

    /**
     * Gets the value of the success property.
     * 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Gets the value of the recipientList property.
     * 
     * @return possible object is {@link ArrayOfRecipientStatus }
     * 
     */
    public ArrayOfRecipientStatus getRecipientList() {
        return recipientList;
    }

    /**
     * Sets the value of the recipientList property.
     * 
     * @param value
     *            allowed object is {@link ArrayOfRecipientStatus }
     * 
     */
    public void setRecipientList(ArrayOfRecipientStatus value) {
        this.recipientList = value;
    }

    public ArrayOfMessage getMessages() {
        return messages;
    }

    public void setMessages(ArrayOfMessage messages) {
        this.messages = messages;
    }

}
