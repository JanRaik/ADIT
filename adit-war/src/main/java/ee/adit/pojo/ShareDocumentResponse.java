//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.08 at 02:57:06 PM EEST 
//

package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ShareDocumentResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ShareDocumentResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="messages" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}ArrayOfMessage"/>
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
@XmlType(name = "ShareDocumentResponse", propOrder = {"success", "messages", "recipientList" })
public class ShareDocumentResponse {

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
     * Gets the value of the messages property.
     * 
     * @return possible object is {@link ArrayOfMessage }
     * 
     */
    public ArrayOfMessage getMessages() {
        return messages;
    }

    /**
     * Sets the value of the messages property.
     * 
     * @param value
     *            allowed object is {@link ArrayOfMessage }
     * 
     */
    public void setMessages(ArrayOfMessage value) {
        this.messages = value;
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

}
