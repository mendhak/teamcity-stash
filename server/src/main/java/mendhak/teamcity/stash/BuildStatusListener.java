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


package mendhak.teamcity.stash;


import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;
import mendhak.teamcity.stash.ui.StashBuildFeature;
import mendhak.teamcity.stash.ui.StashServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class BuildStatusListener
{
    @NotNull
    private final ChangeStatusUpdater updater;

    final StashServerKeyNames keyNames = new StashServerKeyNames();

    public BuildStatusListener(@NotNull final EventDispatcher<BuildServerListener> listener,
                               @NotNull final ChangeStatusUpdater updater)
    {
        this.updater = updater;
        listener.addListener(new BuildServerAdapter()
        {
            @Override
            public void changesLoaded(SRunningBuild build)
            {
                updateBuildStatus(build, true);
            }

            @Override
            public void buildFinished(SRunningBuild build)
            {
                updateBuildStatus(build, false);
            }
        });
    }

    private void updateBuildStatus(@NotNull final SRunningBuild build, boolean isStarting)
    {
        SBuildType buildType = build.getBuildType();
        if (buildType == null)
        {
            return;
        }

        for (SBuildFeatureDescriptor feature : buildType.getBuildFeatures())
        {
            if (!feature.getType().equals(StashBuildFeature.FEATURE_TYPE))
            {
                continue;
            }

            Logger.LogInfo("VCS to ignore:" + feature.getParameters().get(keyNames.getVCSIgnoreKey()));

            final ChangeStatusUpdater.Handler h = updater.getUpdateHandler(feature);

            List<String> changes = getLatestChanges(build);

            if (changes.isEmpty())
            {
                Logger.LogInfo("No revisions were found to update Stash with. Build #:" + String.valueOf(build.getBuildNumber()));
            }

            for (String change : changes)
            {
                if (isStarting)
                {
                    h.scheduleChangeStarted(change, build);
                }
                else
                {
                    h.scheduleChangeCompeted(change, build);
                }
            }
        }
    }

    private List<String> getLatestChanges(final SRunningBuild build)
    {
        final List<String> revisions = new ArrayList<String>();

        for(BuildRevision revision : build.getRevisions())
        {
            revisions.add(revision.getRevision());
        }

        return revisions;
    }


}
