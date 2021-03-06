//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.14 at 10:55:23 AM PDT 
//


package jPCBSim;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}FillColor" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}EdgeColor" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}Primitives" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}Weight" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="f0" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fc" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Number" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Frequency" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Delay" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Excite" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="PropDir" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fillColor",
    "edgeColor",
    "primitives",
    "weight"
})
@XmlRootElement(name = "Excitation")
public class Excitation {

    @XmlElement(name = "FillColor")
    protected List<FillColor> fillColor;
    @XmlElement(name = "EdgeColor")
    protected List<EdgeColor> edgeColor;
    @XmlElement(name = "Primitives")
    protected List<Primitives> primitives;
    @XmlElement(name = "Weight")
    protected List<Weight> weight;
    @XmlAttribute(name = "Type")
    protected String type;
    @XmlAttribute(name = "f0")
    protected String f0;
    @XmlAttribute(name = "fc")
    protected String fc;
    @XmlAttribute(name = "ID")
    protected String id;
    @XmlAttribute(name = "Name")
    protected String name;
    @XmlAttribute(name = "Number")
    protected String number;
    @XmlAttribute(name = "Frequency")
    protected String frequency;
    @XmlAttribute(name = "Delay")
    protected String delay;
    @XmlAttribute(name = "Excite")
    protected String excite;
    @XmlAttribute(name = "PropDir")
    protected String propDir;

    /**
     * Gets the value of the fillColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fillColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFillColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FillColor }
     * 
     * 
     */
    public List<FillColor> getFillColor() {
        if (fillColor == null) {
            fillColor = new ArrayList<FillColor>();
        }
        return this.fillColor;
    }

    /**
     * Gets the value of the edgeColor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the edgeColor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEdgeColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EdgeColor }
     * 
     * 
     */
    public List<EdgeColor> getEdgeColor() {
        if (edgeColor == null) {
            edgeColor = new ArrayList<EdgeColor>();
        }
        return this.edgeColor;
    }

    /**
     * Gets the value of the primitives property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the primitives property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrimitives().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Primitives }
     * 
     * 
     */
    public List<Primitives> getPrimitives() {
        if (primitives == null) {
            primitives = new ArrayList<Primitives>();
        }
        return this.primitives;
    }

    /**
     * Gets the value of the weight property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weight property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Weight }
     * 
     * 
     */
    public List<Weight> getWeight() {
        if (weight == null) {
            weight = new ArrayList<Weight>();
        }
        return this.weight;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the f0 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getF0() {
        return f0;
    }

    /**
     * Sets the value of the f0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setF0(String value) {
        this.f0 = value;
    }

    /**
     * Gets the value of the fc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFc() {
        return fc;
    }

    /**
     * Sets the value of the fc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFc(String value) {
        this.fc = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Gets the value of the frequency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Sets the value of the frequency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrequency(String value) {
        this.frequency = value;
    }

    /**
     * Gets the value of the delay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelay() {
        return delay;
    }

    /**
     * Sets the value of the delay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelay(String value) {
        this.delay = value;
    }

    /**
     * Gets the value of the excite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExcite() {
        return excite;
    }

    /**
     * Sets the value of the excite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExcite(String value) {
        this.excite = value;
    }

    /**
     * Gets the value of the propDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropDir() {
        return propDir;
    }

    /**
     * Sets the value of the propDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropDir(String value) {
        this.propDir = value;
    }

}
