/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.mock.MockValidationSpecification;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.util.SimpleHttpClient;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTest {

    protected ServiceValidateController serviceValidateController;

    @Autowired
    private ServicesManager servicesManager;

    @Before
    public void onSetUp() throws Exception {
        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController.setCentralAuthenticationService(getCentralAuthenticationService());
        final Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler();
        proxyHandler.setHttpClient(new SimpleHttpClient());
        this.serviceValidateController.setProxyHandler(proxyHandler);
        this.serviceValidateController.setApplicationContext(context);
        this.serviceValidateController.setArgumentExtractor(new CasArgumentExtractor());
        this.serviceValidateController.setServicesManager(this.servicesManager);
    }

    private HttpServletRequest getHttpServletRequest() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());
        final ServiceTicket sId2 = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId2.getId());
        request.addParameter("renew", "true");

        return request;
    }

    @Test
    public void testAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
    }

    @Test
    public void testEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
                new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    public void testValidServiceTicket() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketInvalidSpec() throws Exception {
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
    }

    @Test(expected=RuntimeException.class)
    public void testValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(MockValidationSpecification.class);

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
        fail(TestUtils.CONST_EXCEPTION_EXPECTED);
    }

    @Test
    public void testInvalidServiceTicket() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId.getId());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final ModelAndView modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        
    }

    @Test
    public void testValidServiceTicketWithInvalidPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "duh");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void testValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNotNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void testValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        this.serviceValidateController.setProxyHandler(new ProxyHandler() {
            @Override
            public String handle(final Credential credential, final TicketGrantingTicket proxyGrantingTicketId) {
                return null;
            }
            
            @Override
            public boolean canHandle(final Credential credential) {
                return true;
            }
        });
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void testValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        
        final String ORIG_SVC = "http://www.jasig.org?param=hello+world";
        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), TestUtils.getService(ORIG_SVC));

        final String REQ_SVC = "http://WWW.JASIG.ORG?PARAM=hello%20world";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService(REQ_SVC).getId());
        request.addParameter("ticket", sId.getId());
        
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }
    
    @Test
    public void testValidServiceTicketWithDifferentEncoding() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        
        final String ORIG_SVC = "http://www.jasig.org?param=hello+world";
        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), TestUtils.getService(ORIG_SVC));

        final String REQ_SVC = "http://www.jasig.org?param=hello%20world";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService(REQ_SVC).getId());
        request.addParameter("ticket", sId.getId());
        
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }
    
    @Test
    public void testValidServiceTicketAndPgtUrlMismatch() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        
        final Service svc = TestUtils.getService("proxyService");
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", svc.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "http://www.github.com");
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }

    protected final ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl() throws Exception {
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");


        return this.serviceValidateController
                .handleRequestInternal(request, new MockHttpServletResponse());
    }
}
