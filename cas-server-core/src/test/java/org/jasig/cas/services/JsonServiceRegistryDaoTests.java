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
package org.jasig.cas.services;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.authentication.principal.CachingPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Handles test cases for {@link JsonServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDaoTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private ServiceRegistryDao dao;

    public JsonServiceRegistryDaoTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE.getFile());
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithNonExistentServiceAndNoAttributes");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveMethodWithDefaultUsernameAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultUsernameAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void ensureSaveMethodWithDefaultPrincipalAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultPrincipalAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }
    @Test
    public void verifySaveMethodWithDefaultAnonymousAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultAnonymousAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
                new ShibbolethCompatiblePersistentIdGenerator("helloworld")
        ));
        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicy");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithExistingServiceNoAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        this.dao.save(r);
        r.setTheme("mytheme");

        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());
        assertEquals(r, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicyMappingRules() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyMappingRules");
        r.setServiceId("testId");

        final Map<String, String> map = new HashMap<>();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");


        final ReturnMappedAttributeReleasePolicy policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(map);
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRules() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRules");
        r.setServiceId("testId");

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter");
        r.setServiceId("testId");
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);
        r.setAuthorizationStrategy(new DefaultRegisteredServiceAuthorizationStrategy(true, false));
        r.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https://.+"));
        r.setRequiredHandlers(new HashSet<String>(Arrays.asList("h1", "h2")));

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);
        r.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifyServiceType() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("testServiceType");
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof  RegexRegisteredService);
    }

    @Test(expected=RuntimeException.class)
    public void verifyServiceWithInvalidFileName() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("hell/o@world:*");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
    }

    @Test
    public void checkLoadingOfJsonServiceFiles() throws Exception {
        prepTests();
        verifySaveAttributeReleasePolicyAllowedAttrRulesWithCaching();
        verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter();
        assertEquals(this.dao.load().size(), 2);
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesWithCaching() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRulesWithCaching");
        r.setServiceId("testId");

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));

        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put("values", Arrays.asList(new Object[]{"v1", "v2", "v3"}));

        final CachingPrincipalAttributesRepository repository =
                new CachingPrincipalAttributesRepository(
                        new StubPersonAttributeDao(attributes),
                        TimeUnit.MILLISECONDS, 100);
        repository.setMergingStrategy(new ReplacingAttributeAdder());

        policy.setPrincipalAttributesRepository(repository);
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifyServiceRemovals() {
        final List<RegisteredService> list = new ArrayList<>(5);
        for (int i = 1; i < 5; i++) {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId("^https://.+");
            r.setName("testServiceType");
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        }

        for (final RegisteredService r2 : list) {
            this.dao.delete(r2);
            assertNull(this.dao.findServiceById(r2.getId()));
        }

    }

    @Test
    public void checkForAuthorizationStrategy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("checkForAuthorizationStrategy");
        r.setId(42);

        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(false, false);
        authz.setRequireAllAttributes(true);

        final Map<String, List<String>> attrs = new HashMap<>();
        attrs.put("cn", new ArrayList<String>(Arrays.asList("v1, v2, v3")));
        attrs.put("memberOf", new ArrayList<String>(Arrays.asList("v4, v5, v6")));
        authz.setRequiredAttributes(attrs);
        r.setAuthorizationStrategy(authz);

        this.dao.save(r);
        final List<RegisteredService> list = this.dao.load();
        assertEquals(list.size(), 1);
    }
}
