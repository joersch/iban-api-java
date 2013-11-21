/*
 * Copyright (c) 2013 AVENTUM SOLUTIONS GmbH (http://www.aventum-solutions.de)
 * 
 * Free usage, copy and fusion is granted to any person obtaining a copy of this software included associated documentation 
 * files for own use (even commercial). Any changes and enhancements to the software itself, have to be published under 
 * the same license as free software.
 * 
 * In case of using the software within a product, which obtains income from rent, lease or sale, the use of this software 
 * is prohibited. For using the software within commercial products, a separate license can be purchased from the copyright owner.
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * You need any changes or additional functions and you haven't time or knowledge?
 * Don't hesitate to contact us. We can help you.
 * http://www.aventum-solutions.de
 * */
package org.as.iban.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents an IBAN rule for a german bank
 * @author Aventum Solutions GmbH (www.aventum-solutions.de)
 *
 */
public class IbanRuleGerman {
	//	local variables
    final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    final String SCHEMA_LANG = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private String rule_id;
    
    private boolean noCalculation = false;
    private boolean mappingKto = false;
    private boolean mappingBlz = false;
    private boolean modificationKto = false;
    private boolean mappingKtoKr = false;
    private boolean mappingBic = false;
    
    private ArrayList<Element> listNoCalculation = new ArrayList<Element>();
    private ArrayList<MappingKto> listMappingKto = new ArrayList<MappingKto>();
    private ArrayList<Element> listMappingBlz = new ArrayList<Element>();
    private ArrayList<Element> listModificationKto = new ArrayList<Element>();
    private ArrayList<Element> listMappingKtoKr = new ArrayList<Element>();
    private ArrayList<Element> listMappingBic = new ArrayList<Element>();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document = null;
		
    Element element = null;
		    
    /**
     * Constructor. Loads a specified Rule by a given ruleID
     * @param rule_id	The id that identifies the specific rule from iban_rule_german.xml
     */
    public IbanRuleGerman (String rule_id) {
	this.rule_id = rule_id;

	try {
	    factory.setNamespaceAware(true);
	    factory.setValidating(true);
	    factory.setAttribute(SCHEMA_LANG,XML_SCHEMA);
	    factory.setAttribute(SCHEMA_SOURCE, this.getClass().getResourceAsStream("/iban_rules_german.xsd"));
		    
	    builder = factory.newDocumentBuilder();
	    document = builder.parse(this.getClass().getResourceAsStream("/iban_rules_german.xml"));
	
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	    System.exit(-1);
	} catch (SAXException e) {
	    e.printStackTrace();
	    System.exit(-1);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	readRule();
    }
    
    /**
     * Reads the rule from config file
     */
    private void readRule() {
	NodeList nodes = document.getElementById(rule_id).getChildNodes();
		
	for (int i = 0; i < nodes.getLength(); i++) {
	    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
		NodeList nodeRule = nodes.item(i).getChildNodes();
			
		for (int j = 0; j < nodeRule.getLength(); j++) {
		    if (nodeRule.item(j).getNodeType() == Node.ELEMENT_NODE) { 
			element = (Element) nodeRule.item(j);
				    
			switch (nodes.item(i).getNodeName()) {
			case "no_calculation":
			    listNoCalculation.add(element);
			    break;
						    
			case "mappings_kto":
			    MappingKto mapKto = new MappingKto(((Element)element.getParentNode()).getAttribute("blz"));
			    mapKto.setBlzNew(((Element)element.getParentNode()).getAttribute("blz_new"));
			    mapKto.setFrom(element.getAttribute("from"));
			    mapKto.setTo(element.getTextContent());
			    listMappingKto.add(mapKto);
			    break;
						    
			case "mappings_ktokr":
			    listMappingKtoKr.add(element);
			    break;
			    
			case "mappings_blz":
			    listMappingBlz.add(element);
			    break;
			    
			case "modification_kto":
			    listModificationKto.add(element);
			    break;
			    
			case "mappings_bic":
			    listMappingBic.add(element);
			    break;
			}
		    }
		}
	    }
	}
    }
    
    /**
     * Check for no calculation rules for a specific bank identifier
     * @param blz	The bank identifier number to check.
     * @return	'True' in case of existing no calculation rules, otherwise 'false'
     */
    public boolean isNoCalculation (String blz) {
	Iterator<Element> iter = listNoCalculation.iterator();
		
	while (iter.hasNext()) {
	    if (blz.matches(iter.next().getAttribute("blz")))
		this.noCalculation = true;
	}
	return noCalculation;
    }
    
    /**
     * Get the regular expressions of no calculation rule
     * @param blz	The bank identifier number
     * @return	A LinkedList of regular expressions
     */
    public LinkedList<String> getRegexpNoCalculation (String blz) {
	LinkedList<String> tempList = new LinkedList<String>();
	Iterator<Element> iter = listNoCalculation.iterator();
		
	while (iter.hasNext()) {
	    Element tempElement = iter.next();
	    if (blz.matches(tempElement.getAttribute("blz")))
		tempList.add(tempElement.getTextContent());
	}
		
	return tempList;
    }
    
    /**
     * Check for account numbers mapped to differing account numbers
     * @param blz	The bank identifier number
     * @return	'True' in case of existing mapping rules, otherwise 'false'
     */
    public boolean isMappingKto (String blz) {
	Iterator<MappingKto> iter = listMappingKto.iterator();
		
	while (iter.hasNext()) {
	    if (blz.matches(iter.next().getBlz()))
		this.mappingKto = true;
	}
	return mappingKto;
    }
    
    /**
     * Get the mapped account number for a given bank identifier number and account number
     * @param blz	The given bank identifier number
     * @param kto	The given account number
     * @return	The mapped account number
     */
    public String getMappedKto (String blz, String kto) {
	Iterator<MappingKto> iter = listMappingKto.iterator();
	
	while (iter.hasNext()) {
	    MappingKto tempMapping = iter.next();
	    if (blz.matches(tempMapping.getBlz()) && kto.matches(tempMapping.getFrom()))
	    	return tempMapping.getTo();
	}
	return null;
    }

    /**
     * Get the mapped bank identifier for a given bank identifier number and a specific account number
     * @param blz	The given bank identifier number
     * @return	The mapped account number
     */
    public String getMappedBlz(String blz, String kto) {
	Iterator<MappingKto> iter = listMappingKto.iterator();
	
	while (iter.hasNext()) {
	    MappingKto tempMapping = iter.next();
	    if (blz.matches(tempMapping.getBlz()) && kto.matches(tempMapping.getFrom()) && !tempMapping.getBlzNew().isEmpty())
	    	return tempMapping.getBlzNew();
	}
	return null;
    }

    /**
     * Check for bank identifier mappings to an account number circle to a given account number
     * @param kto	The given account number
     * @return	'True' in case of existing mapping rules, otherwise 'false'
     */
    public boolean isMappingKtoKr (String kto) {
	Iterator<Element> iter = listMappingKtoKr.iterator();
	
	while (iter.hasNext()) {
	    if (kto.matches(((Element)iter.next().getParentNode()).getAttribute("kto")))
	    	this.mappingKtoKr = true;
	}
	return mappingKtoKr;
    }
    
    /**
     * Get the mapped bank identifier number for a given account number from a account number circle
     * @param kto	The given account number
     * @return	The mapped account number
     */
    public String getMappedKtoKr (String kto) {
	Iterator<Element> iter = listMappingKtoKr.iterator();
		
	while (iter.hasNext()) {
	    Element tempElement = iter.next();
	    if (kto.substring(0, 3).matches(tempElement.getAttribute("from")))
		return tempElement.getTextContent();
	}
	
	return null;
    }

    /**
     * Check for bank identifier mappings to differing bank identifier numbers
     * @param blz	The given bank identifier number
     * @return	'True' in case of existing mapping rules, otherwise 'false'
     */
    public boolean isMappingBlz (String blz) {
	Iterator<Element> iter = listMappingBlz.iterator();
		
	while (iter.hasNext()) {
	    if (blz.matches(iter.next().getAttribute("from")))
		this.mappingBlz = true;
	}
	return mappingBlz;
    }
    
    /**
     * Get the mapped bank identifier for a given bank identifier number
     * @param blz	The given bank identifier number
     * @return	The mapped account number
     */
    public String getMappedBlz(String blz) {
	Iterator<Element> iter = listMappingBlz.iterator();
		
	while (iter.hasNext()) {
	    Element tempElement = iter.next();
	    if (blz.matches(tempElement.getAttribute("from")))
		return tempElement.getTextContent();
	}
	return null;
    }
    
    /**
     * Check for account number modification rules for a given bank identifier number
     * @param blz	The given bank identifier number
     * @return	'True' in case of existing modification rules, otherwise 'false'
     */
    public boolean isModification (String blz) {
	Iterator<Element> iter = listModificationKto.iterator();
		
	while (iter.hasNext()) {
	    if (blz.matches(iter.next().getAttribute("blz")))
		this.modificationKto = true;
	}
	return modificationKto;
    }
    
    /**
     * Get regular expressions that modifies the bank account number
     * @param blz	The given bank ident number
     * @return	A LinkedList of regular expressions
     */
    public LinkedList<String> getRegexpModification (String blz) {
	LinkedList<String> tempList = new LinkedList<String>();
	Iterator<Element> iter = listModificationKto.iterator();
		
	while (iter.hasNext()) {
	    Element tempElement = iter.next();
	    if (blz.matches(tempElement.getAttribute("blz")))
		tempList.add(tempElement.getTextContent());
	}
		
	return tempList;
    }
    
    /**
     * Check for a BIC-Mapping for the given bank identifier number
     * @param blz	The given bank identifier number
     * @return True in case of BIC-Mapping otherwise false
     */
    public boolean isMappingBic(String blz) {
	Iterator<Element> iter = listMappingBic.iterator();
	
	while (iter.hasNext()) {
	    if (blz.matches(iter.next().getAttribute("blz")))
		this.mappingBic = true;
	}
	return mappingBic;
    }
    
    /**
     * Get the mapped BIC for the given bank identifier number
     * @param blz	The given bank identifier number
     * @return The mapped BIC
     */
    public String getMappedBic (String blz) {
	Iterator<Element> iter = listMappingBic.iterator();
	
	while (iter.hasNext()) {
	    Element tempElement = iter.next();
	    if (blz.matches(tempElement.getAttribute("blz")))
		return tempElement.getTextContent();
	}
	return null;
    }
    
    /**
     * Represents a mapped account number
     * @author Aventum Solutions GmbH (www.aventum-solutions.de)
     *
     */
    class MappingKto {
	
	private String blz;
	private String from;
	private String to;
	private String blzNew;
		
	/**
	 * Constructor
	 * @param blz	The bank ident number of the bank
	 */
	MappingKto (String blz) {
	    this.blz = blz;
	}
		
	/**
	 * Set the "mapped from" account number
	 * @param from	The account number to map from
	 */
	private void setFrom (String from) {
	    this.from = from;
	}
		
	/**
	 * Set the "mapped to" account number
	 * @param to	The account number to map to
	 */
	private void setTo (String to) {
	    this.to = to;
	}
	
	/**
	 * Set the new bank identifier
	 * @param blzNew	The new bank identifier
	 */
	private void setBlzNew (String blzNew) {
	    this.blzNew = blzNew;
	}
		
	/**
	 * Get the "mapped from" account number
	 * @return	The account number to map from
	 */
	private String getFrom() {
	    return this.from;
	}
		
	/**
	 * Get the "mapped from" account number
	 * @return	The account number to map to
	 */
	private String getTo() {
	    return this.to;
	}
		
	/**
	 * Get the bank identifier number
	 * @return	The bank identifier number
	 */
	private String getBlz() {
	    return this.blz;
	}
	
	/**
	 * Get the new bank identifier (in case of matching account number)
	 * @return	The new Bank identifier
	 */
	private String getBlzNew() {
	    return this.blzNew;
	}
    }
}
