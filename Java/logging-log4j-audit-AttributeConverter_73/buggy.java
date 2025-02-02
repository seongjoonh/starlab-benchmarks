/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.catalog.jpa.converter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.catalog.api.Attribute;
import org.apache.logging.log4j.catalog.api.Constraint;
import org.apache.logging.log4j.catalog.jpa.model.AttributeModel;
import org.apache.logging.log4j.catalog.jpa.model.ConstraintModel;
import org.apache.logging.log4j.catalog.jpa.service.AttributeService;
import org.modelmapper.AbstractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AttributeConverter extends AbstractConverter<Attribute, AttributeModel> {
    private static final Logger LOGGER = LogManager.getLogger(AttributeConverter.class);

    @Autowired
    private AttributeService attributeService;

public org.apache.logging.log4j.catalog.jpa.model.AttributeModel convert(org.apache.logging.log4j.catalog.api.Attribute attribute) {
    org.apache.logging.log4j.catalog.jpa.converter.AttributeConverter.LOGGER.traceEntry(attribute.getName());
    org.apache.logging.log4j.catalog.jpa.model.AttributeModel model;
    if (attribute.getId() != null) {
        model = attributeService.getAttribute(attribute.getId()).orElseGet(org.apache.logging.log4j.catalog.jpa.model.AttributeModel::new);
    } else {
        model = new org.apache.logging.log4j.catalog.jpa.model.AttributeModel();
    }
    model.setName(attribute.getName());
    model.setAliases(attribute.getAliases());
    model.setDescription(attribute.getDescription());
    model.setDisplayName(attribute.getDisplayName());
    model.setDataType(attribute.getDataType());
    model.setId(attribute.getId());
    model.setCatalogId(attribute.getCatalogId());
    model.setIndexed(attribute.isIndexed());
    model.setRequestContext(attribute.isRequestContext());
    model.setRequired(attribute.isRequired());
    model.setSortable(attribute.isSortable());
    model.setExamples(attribute.getExamples());
    java.util.Set<org.apache.logging.log4j.catalog.jpa.model.ConstraintModel> constraintModels = (model.getConstraints() != null) ? model.getConstraints() : new java.util.HashSet<>();
    java.util.Map<java.lang.Long, org.apache.logging.log4j.catalog.jpa.model.ConstraintModel> constraintMap = constraintModels.stream().collect(java.util.stream.Collectors.toMap(org.apache.logging.log4j.catalog.jpa.model.ConstraintModel::getId, java.util.function.Function.identity()));
    {
        constraintModels.removeIf(( a) -> attribute.getConstraints().stream().noneMatch(( b) -> b.getId().equals(a.getId())));
        for (org.apache.logging.log4j.catalog.api.Constraint constraint : attribute.getConstraints()) {
            org.apache.logging.log4j.catalog.jpa.model.ConstraintModel constraintModel;
            {
                constraintModel = constraintMap.get(/* NPEX_NULL_EXP */
                constraint.getId());
                constraintModel.setConstraintType(constraint.getConstraintType().getName());
                constraintModel.setValue(constraint.getValue());
            }
        }
    }
    model.setConstraints(constraintModels);
    return org.apache.logging.log4j.catalog.jpa.converter.AttributeConverter.LOGGER.traceExit(model);
}
}
