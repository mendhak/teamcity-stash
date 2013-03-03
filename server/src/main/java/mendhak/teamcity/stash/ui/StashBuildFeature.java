/*
*    This file is part of TeamCity Stash.
*
*    TeamCity Stash is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    TeamCity Stash is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with TeamCity Stash.  If not, see <http://www.gnu.org/licenses/>.
*/


package mendhak.teamcity.stash.ui;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class StashBuildFeature extends BuildFeature
{
    public static final String FEATURE_TYPE = "teamcity.stash.status";
    private final PluginDescriptor descriptor;

    public StashBuildFeature(@NotNull final PluginDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    @NotNull
    @Override
    public String getType()
    {
        return FEATURE_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName()
    {
        return "Report build status to Atlassian Stash";
    }

    @Nullable
    @Override
    public String getEditParametersUrl()
    {
        return descriptor.getPluginResourcesPath("feature.html");
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params)
    {
        return "Build statuses will be sent to " + params.get(new StashServerKeyNames().getServerKey());

    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor()
    {
        final StashServerKeyNames keyNames = new StashServerKeyNames();
        return new PropertiesProcessor()
        {
            private void validate(@NotNull final Map<String, String> properties,
                                  @NotNull final String key,
                                  @NotNull final String message,
                                  @NotNull final Collection<InvalidProperty> res)
            {
                if (jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(properties.get(key)))
                {
                    res.add(new InvalidProperty(key, message));
                }
            }

            @NotNull
            public Collection<InvalidProperty> process(@Nullable final Map<String, String> propertiesMap)
            {
                final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
                if (propertiesMap == null)
                {
                    return result;
                }

                validate(propertiesMap, keyNames.getUserNameKey(), "Username must be specified", result);
                validate(propertiesMap, keyNames.getPasswordKey(), "Password must be specified", result);
                validate(propertiesMap, keyNames.getServerKey(), "Stash server base URL", result);

                return result;
            }
        };
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters()
    {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(new StashServerKeyNames().getServerKey(), "http://127.0.0.1:7990");
        return map;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed()
    {
        return true;
    }
}
