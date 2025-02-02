/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.jcr.resource.internal;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.serviceusermapping.ServicePrincipalsValidator;
import org.apache.sling.serviceusermapping.ServiceUserValidator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link org.apache.sling.serviceusermapping.ServiceUserValidator}
 * and {@link org.apache.sling.serviceusermapping.ServicePrincipalsValidator}
 * interfaces that verifies that all registered service users/principals are represented by
 * {@link org.apache.jackrabbit.api.security.user.User#isSystemUser() system users}
 * in the underlying JCR repository.
 *
 * @see org.apache.jackrabbit.api.security.user.User#isSystemUser()
 */
@Designate(ocd = JcrSystemUserValidator.Config.class)
@Component(service = {ServiceUserValidator.class, ServicePrincipalsValidator.class},
           property = {
                   Constants.SERVICE_VENDOR + "=The Apache Software Foundation"
           })
public class JcrSystemUserValidator implements ServiceUserValidator, ServicePrincipalsValidator {

    public static final String VALIDATION_SERVICE_USER = "validation";

    @ObjectClassDefinition(
            name = "Apache Sling JCR System User Validator",
            description = "Enforces the usage of JCR system users for all user mappings being used in the 'Sling Service User Mapper Service'")
    public @interface Config {

        @AttributeDefinition(name = "Allow only JCR System Users",
                description="If set to true, only user IDs bound to JCR system users are allowed in the user mappings of the 'Sling Service User Mapper Service'. Otherwise all users are allowed!")
        boolean allow_only_system_user() default true;
    }
    /**
     * logger instance
     */
    private final Logger log = LoggerFactory.getLogger(JcrSystemUserValidator.class);

    @Reference
    private volatile SlingRepository repository;

    private final Method isSystemUserMethod;

    private final Set<String> validIds = new CopyOnWriteArraySet<String>();
    private final Set<String> validPrincipalNames = new CopyOnWriteArraySet<String>();

    private boolean allowOnlySystemUsers;

    /*
    * We have to prevent a cycle if we are trying to login ourselves. The main idea is that we set the
    * cycleDetection to true for the current thread before we try to loginService('validation', null).
    * That way, if we are asked if a user is valid and the cycleDetection is true we know we are in a
    * cycle and have to shotcut by allowing the user. This should make it so that we use a service user
    * to valid all service users except our own.
    */
    private final ThreadLocal<Boolean> cycleDetection = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public JcrSystemUserValidator() {
        Method m = null;
        try {
            m = User.class.getMethod("isSystemUser");
        } catch (Exception e) {
            log.debug("Exception while accessing isSystemUser method", e);
        }
        isSystemUserMethod = m;
    }

    @Activate
    public void activate(final Config config) {
        allowOnlySystemUsers = config.allow_only_system_user();
    }

    @Override
public boolean isValid(final java.lang.String serviceUserId, final java.lang.String serviceName, final java.lang.String subServiceName) {
    if (cycleDetection.get()) {
        // We are being asked to valid our own service user - hence, allow.
        return true;
    }
    {
        if (!allowOnlySystemUsers) {
            log.debug("There is no enforcement of JCR system users, therefore service user id '{}' is valid", /* NPEX_NULL_EXP */
            serviceUserId);
            return true;
        }
        if (validIds.contains(serviceUserId)) {
            log.debug("The provided service user id '{}' has been already validated and is a known JCR system user id", serviceUserId);
            return true;
        } else {
            javax.jcr.Session session = null;
            try {
                try {
                    /* We have to prevent a cycle if we are trying to login ourselves */
                    cycleDetection.set(true);
                    try {
                        session = repository.loginService(org.apache.sling.jcr.resource.internal.JcrSystemUserValidator.VALIDATION_SERVICE_USER, null);
                    } finally {
                        cycleDetection.set(false);
                    }
                    if (session instanceof org.apache.jackrabbit.api.JackrabbitSession) {
                        final org.apache.jackrabbit.api.security.user.UserManager userManager = ((org.apache.jackrabbit.api.JackrabbitSession) (session)).getUserManager();
                        final org.apache.jackrabbit.api.security.user.Authorizable authorizable = userManager.getAuthorizable(serviceUserId);
                        if (isValidSystemUser(authorizable)) {
                            validIds.add(serviceUserId);
                            log.debug("The provided service user id {} is a known JCR system user id", serviceUserId);
                            return true;
                        }
                    }
                } catch (final javax.jcr.RepositoryException e) {
                    log.warn("Could not get user information", e);
                }
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
            log.warn("The provided service user id '{}' is not a known JCR system user id and therefore not allowed in the Sling Service User Mapper.", serviceUserId);
            return false;
        }
    }
}

    @Override
    public boolean isValid(Iterable<String> servicePrincipalNames, String serviceName, String subServiceName) {
        if (cycleDetection.get()) {
            // We are being asked to valid our own service user - hence, allow.
            return true;
        }
        if (servicePrincipalNames == null) {
            log.debug("The provided service principal names are null");
            return false;
        }
        if (!allowOnlySystemUsers) {
            log.debug("There is no enforcement of JCR system users, therefore service principal names '{}' are valid", servicePrincipalNames);
            return true;
        }

        Session session = null;
        UserManager userManager = null;
        Set<String> invalid = new HashSet<>();
        try {
            for (final String pName : servicePrincipalNames) {
                if (validPrincipalNames.contains(pName)) {
                    log.debug("The provided service principal name '{}' has been already validated and is a known JCR system user", pName);
                } else {
                    if (session == null) {
                        /*
                         * We have to prevent a cycle if we are trying to login ourselves
                        */
                        cycleDetection.set(true);
                        try {
                            session = repository.loginService(VALIDATION_SERVICE_USER, null);
                        } finally {
                            cycleDetection.set(false);
                        }
                        if (session instanceof JackrabbitSession) {
                            userManager = ((JackrabbitSession) session).getUserManager();
                        } else {
                            log.debug("Unable to validate service user principals, JackrabbitSession expected.");
                            return false;
                        }
                    }

                    Authorizable authorizable = userManager.getAuthorizable(new Principal() {
                        @Override
                        public String getName() {
                            return pName;
                        }
                    });
                    if (isValidSystemUser(authorizable)) {
                        validPrincipalNames.add(pName);
                        log.debug("The provided service principal name {} is a known JCR system user", pName);
                    } else {
                        log.warn("The provided service principal name '{}' is not a known JCR system user id and therefore not allowed in the Sling Service User Mapper.", pName);
                        invalid.add(pName);
                    }
                }
            }
        } catch (final RepositoryException e) {
            log.warn("Could not get user information", e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        return invalid.isEmpty();
    }

    private boolean isValidSystemUser(final Authorizable authorizable){
        if (authorizable == null || authorizable.isGroup()) {
            return false;
        }

        User user = (User) authorizable;
        try {
            if (!user.isDisabled()) {
                if (isSystemUserMethod != null) {
                    try {
                        return (Boolean) isSystemUserMethod.invoke(user);
                    } catch (Exception e) {
                        log.debug("Exception while invoking isSystemUser method", e);
                        return true;
                    }
                } else {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            log.debug("Exception while invoking isDisabled method", e);
        }
        return false;
    }
}
