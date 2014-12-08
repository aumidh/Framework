/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package gla.sowf.module.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.axis2.util.XMLUtils;
import java.util.UUID;
import org.joda.time.DateTime;
import java.util.Date;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;

import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Element;

public class OutFlowHandler extends AbstractHandler implements Handler {
	private static final Log log = LogFactory.getLog(ProtocolHandler.class);
    private String name;
	public String getName(){
        return name;
    } 
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
//		String _service=msgContext.getTo().getAddress();
//		String _action=msgContext.getOptions().getAction().substring(4);
//		log.info("Other request Count: " + OtherRequests.getSize() );
//		log.info("Target Service: " + _service);
//		log.info("Target Action: " + _action);
//		if(OtherRequests.getSize() > 0)
//		{
//			OtherMessageRequest or=OtherRequests.get(_service,_action);
//			if(or !=null)
//			{
//				log.info("Id of the corresponding action:  " + or.id);
//			}
//			else
//			{
//				log.info("Other request is null");
//			}
//		}
//		log.info(msgContext.getEnvelope().toString());
    	return InvocationResponse.CONTINUE;
    }
	public void revoke(MessageContext msgContext) 
	{
        log.info(" Out Flow Reovke");
    }
	public void setName(String name) {
        this.name = name;
    }

}
