/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.server.authentication;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA-compliant implementation of the
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 4.0.0
 *
 */
@Entity(name = "authentication")
@Embeddable()
public final class JpaAuthenticationImpl implements Authentication {

    @Column(name = "auth_date", nullable = false, insertable = true, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date authenticationDate = new Date();

    @Column(name = "auth_long_term", nullable = false, insertable = true, updatable = false)
    private boolean longTermAuthentication;

    @Embedded
    private JpaAttributePrincipalImpl attributePrincipal;

    @Lob
    @Column(name="auth_meta_data")
    private HashMap<String, List<Object>> authenticationMetaData = new HashMap<String, List<Object>>();

    public JpaAuthenticationImpl() {
        // this should only be called by JPA
    }

    public JpaAuthenticationImpl(final AttributePrincipal attributePrincipal, final boolean longTermAuthentication, final Map<String, List<Object>> metaData) {
        Assert.isInstanceOf(JpaAttributePrincipalImpl.class, attributePrincipal);
        this.longTermAuthentication = longTermAuthentication;
        this.attributePrincipal = (JpaAttributePrincipalImpl) attributePrincipal;
        this.authenticationMetaData.putAll(metaData);
    }

    public Date getAuthenticationDate() {
        return new Date(this.authenticationDate.getTime());
    }

    public Map<String, List<Object>> getAuthenticationMetaData() {
        return this.authenticationMetaData;
    }

    public AttributePrincipal getPrincipal() {
        return this.attributePrincipal;
    }

    public boolean isLongTermAuthentication() {
        return this.longTermAuthentication;
    }
}