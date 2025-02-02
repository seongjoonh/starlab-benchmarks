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
import javax.json.JsonArray;

import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Extensions;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.analyser.task.AnalyserTask;
import org.apache.sling.feature.analyser.task.AnalyserTaskContext;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;

public abstract class AbstractApiRegionsAnalyserTask implements AnalyserTask {

    @Override
public final void execute(org.apache.sling.feature.analyser.task.AnalyserTaskContext ctx) throws java.lang.Exception {
    org.apache.sling.feature.Feature feature = ctx.getFeature();
    // extract and check the api-regions
    org.apache.sling.feature.Extensions extensions = feature.getExtensions();
    org.apache.sling.feature.Extension apiRegionsExtension = extensions.getByName(org.apache.sling.feature.extension.apiregions.api.ApiRegions.EXTENSION_NAME);
    {
        if ((/* NPEX_NULL_EXP */
        apiRegionsExtension.getJSON() == null) || apiRegionsExtension.getJSON().isEmpty()) {
            // no need to be analyzed
            return;
        }
        if (apiRegionsExtension.getJSONStructure() == null) {
            ctx.reportError(("API Regions '" + apiRegionsExtension.getJSON()) + "' does not represent a valid JSON 'api-regions'");
            return;
        }
        // read the api-regions and create a Sieve data structure for checks
        org.apache.sling.feature.extension.apiregions.api.ApiRegions apiRegions;
        try {
            apiRegions = org.apache.sling.feature.extension.apiregions.api.ApiRegions.parse(((javax.json.JsonArray) (apiRegionsExtension.getJSONStructure())));
        } catch (java.io.IOException e) {
            ctx.reportError((("API Regions '" + apiRegionsExtension.getJSON()) + "' does not represent a valid JSON 'api-regions': ") + e.getMessage());
            return;
        }
        execute(apiRegions, ctx);
    }
}

    protected abstract void execute(ApiRegions apiRegions, AnalyserTaskContext ctx) throws Exception;

}
