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
package org.apache.sling.feature.extension.apiregions.analyser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;

import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Extensions;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.analyser.task.AnalyserTask;
import org.apache.sling.feature.analyser.task.AnalyserTaskContext;
import org.apache.sling.feature.extension.apiregions.api.ApiRegion;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;

public class CheckApiRegionsOrder implements AnalyserTask {

    @Override
    public String getId() {
        return ApiRegions.EXTENSION_NAME + "-check-order";
    }

    @Override
    public String getName() {
        return "Api Regions check order analyser task";
    }

    @Override
public final void execute(org.apache.sling.feature.analyser.task.AnalyserTaskContext ctx) throws java.lang.Exception {
    java.lang.String order = ctx.getConfiguration().get("order");
    org.apache.sling.feature.Feature feature = ctx.getFeature();
    {
        if (order == null) {
            reportError(ctx, (("This analyser task must be configured: " + getId()) + " for feature ") + /* NPEX_NULL_EXP */
            feature.getId());
            reportError(ctx, "Must specify configuration key 'order'.");
            return;
        }
        java.lang.String[] sl = order.split("[,]");
        java.util.List<java.lang.String> prescribedOrder = new java.util.ArrayList<>();
        for (java.lang.String s : sl) {
            s = s.trim();
            if (s.length() > 0) {
                prescribedOrder.add(s);
            }
        }
        if (prescribedOrder.size() == 0) {
            reportError(ctx, "No regions declared in the 'order' configuration");
            return;
        }
        // extract and check the api-regions
        org.apache.sling.feature.Extensions extensions = feature.getExtensions();
        org.apache.sling.feature.Extension apiRegionsExtension = extensions.getByName(org.apache.sling.feature.extension.apiregions.api.ApiRegions.EXTENSION_NAME);
        if (apiRegionsExtension == null) {
            // no need to be analyzed
            return;
        }
        java.lang.String jsonRepresentation = apiRegionsExtension.getJSON();
        if ((jsonRepresentation == null) || jsonRepresentation.isEmpty()) {
            // no need to be analyzed
            return;
        }
        try {
            int regionIdx = 0;
            org.apache.sling.feature.extension.apiregions.api.ApiRegions apiRegions = org.apache.sling.feature.extension.apiregions.api.ApiRegions.parse(((javax.json.JsonArray) (apiRegionsExtension.getJSONStructure())));
            for (final org.apache.sling.feature.extension.apiregions.api.ApiRegion region : apiRegions.listRegions()) {
                java.lang.String name = region.getName();
                if (!prescribedOrder.contains(name)) {
                    reportError(ctx, "Region found with undeclared name: " + name);
                    return;
                }
                int prevIdx = regionIdx;
                regionIdx = validateRegion(regionIdx, prescribedOrder, name);
                if (regionIdx < 0) {
                    reportError(ctx, (((("Region '" + name) + "' appears in the wrong order. It appears after '") + prescribedOrder.get(prevIdx)) + "'. Order of regions should be ") + prescribedOrder);
                    return;
                }
            }
        } catch (final java.io.IOException e) {
            ctx.reportError("Invalid api regions");
        }
    }
}

    private int validateRegion(int regionIdx, List<String> order, String name) {
        for (int i=regionIdx; i<order.size(); i++) {
            if (name.equals(order.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private String getPrefix() {
        return getId() + ": ";
    }

    private void reportError(AnalyserTaskContext ctx, String err) {
        ctx.reportError(getPrefix() + err);
    }
}
