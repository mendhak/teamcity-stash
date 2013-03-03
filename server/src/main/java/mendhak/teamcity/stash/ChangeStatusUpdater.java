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

import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.ExceptionUtil;
import mendhak.teamcity.stash.api.StashClient;
import mendhak.teamcity.stash.ui.StashBuildFeature;
import mendhak.teamcity.stash.ui.StashServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;


public class ChangeStatusUpdater
{
    private final ExecutorService myExecutor;
    @NotNull

    private final WebLinks myWeb;
    private final BuildsManager buildsManager;

    public ChangeStatusUpdater(@NotNull final ExecutorServices services,
                               @NotNull final WebLinks web,
                               BuildsManager manager)
    {
        myWeb = web;
        myExecutor = services.getLowPriorityExecutorService();
        buildsManager = manager;
    }


    private String getBuildDisplayDescription(SRunningBuild build)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
        Date now = new Date();
        String logEntry = formatter.format(now);

        return String.format("[%s] %s", build.getFullName(), logEntry);

    }

    private String getBuildDisplayName(SRunningBuild build)
    {

        //Due to a bug, build.getStatusDescriptor() returns stale information.
        //http://youtrack.jetbrains.com/issue/TW-22027

        String buildStatus = build.getBuildStatus().getText();

        if (buildsManager != null &&
                buildsManager.findBuildInstanceById(build.getBuildId()) != null
                && buildsManager.findBuildInstanceById(build.getBuildId()).getStatusDescriptor() != null)
        {
            buildStatus = buildsManager.findBuildInstanceById(build.getBuildId()).getStatusDescriptor().getText();

        }

        return String.format("Build #%s, %s",
                String.valueOf(build.getBuildNumber()), buildStatus);

    }

    private String getRevision(SRunningBuild build)
    {
        if (build.getRevisions().size() > 0)
        {
            return build.getRevisions().get(0).getRevision();
        }

        return "";
    }

    public static interface Handler
    {
        void scheduleChangeStarted(@NotNull final String hash, @NotNull final SRunningBuild build);

        void scheduleChangeCompeted(@NotNull final String hash, @NotNull final SRunningBuild build);
    }

    @NotNull
    public Handler getUpdateHandler(@NotNull final SBuildFeatureDescriptor feature)
    {
        if (!feature.getType().equals(StashBuildFeature.FEATURE_TYPE))
        {
            throw new IllegalArgumentException("Unexpected feature type " + feature.getType());
        }

        final StashServerKeyNames c = new StashServerKeyNames();


        return new Handler()
        {

            public void scheduleChangeStarted(@NotNull String hash, @NotNull SRunningBuild build)
            {
                scheduleChangeUpdate(hash, build, StashClient.BuildState.IN_PROGRESS);
            }

            public void scheduleChangeCompeted(@NotNull String hash, @NotNull SRunningBuild build)
            {
                StashClient.BuildState status = build.getStatusDescriptor().isSuccessful() ?
                        StashClient.BuildState.SUCCESSFUL : StashClient.BuildState.FAILED;

                scheduleChangeUpdate(hash, build, status);
            }

            private void scheduleChangeUpdate(@NotNull final String hash,
                                              @NotNull final SRunningBuild build,
                                              @NotNull final StashClient.BuildState status)
            {
                Logger.LogInfo("Scheduling Stash status update for hash: " + hash + ", buildId: "
                        + build.getBuildId() + ", status: " + status);

                myExecutor.submit(ExceptionUtil.catchAll("set change status on Stash", new Runnable()
                {
                    public void run()
                    {

                        StashClient client = new StashClient(feature.getParameters().get(c.getServerKey()),
                                feature.getParameters().get(c.getUserNameKey()), feature.getParameters().get(c.getPasswordKey()));

                        client.SendBuildStatus(status, build.getBuildNumber(),
                                getBuildDisplayName(build), myWeb.getViewResultsUrl(build),
                                getBuildDisplayDescription(build), getRevision(build));

                        Logger.LogInfo("Updated Stash status for revision: " + hash + ", buildId: " + build.getBuildId() + ", status: " + status);

                    }
                }));
            }
        };
    }
}
