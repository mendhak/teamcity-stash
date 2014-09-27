/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mendhak.teamcity.stash;

import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import mendhak.teamcity.stash.ui.StashBuildFeature;
import mendhak.teamcity.stash.ui.StashServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class StashBuildParametersProvider extends AbstractBuildParametersProvider {
    @Override
    @NotNull
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        Map<String, String> parameters = super.getParameters(build,emulationMode);

        final StashServerKeyNames keyNames = new StashServerKeyNames();

        SBuildType buildType = build.getBuildType();
        if (buildType != null){
            for (SBuildFeatureDescriptor feature : buildType.getBuildFeatures()){
                if (feature.getBuildFeature().getType().equals(StashBuildFeature.FEATURE_TYPE)){

                    ValueResolver resolver = build.getValueResolver();

                    parameters.put(keyNames.getServerKey(), resolver.resolve(feature.getParameters().get(keyNames.getServerKey())).getResult());
                    parameters.put(keyNames.getPasswordKey(), resolver.resolve(feature.getParameters().get(keyNames.getPasswordKey())).getResult());
                    parameters.put(keyNames.getUserNameKey(), resolver.resolve(feature.getParameters().get(keyNames.getUserNameKey())).getResult());
                }
            }
        }
        return parameters;
    }
}
