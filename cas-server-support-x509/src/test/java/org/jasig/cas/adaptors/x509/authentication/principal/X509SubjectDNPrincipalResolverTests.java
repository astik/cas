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
package org.jasig.cas.adaptors.x509.authentication.principal;

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0.6
 *
 */
public class X509SubjectDNPrincipalResolverTests extends AbstractX509CertificateTests {

    private final X509SubjectDNPrincipalResolver
        resolver = new X509SubjectDNPrincipalResolver();

    @Test
    public void testResolvePrincipalInternal() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        assertEquals(VALID_CERTIFICATE.getSubjectDN().getName(), this.resolver.resolve(c).getId());
    }

    @Test
    public void testSupport() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }

    @Test
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }

}
