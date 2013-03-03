/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.ExceptionUtil;
import mendhak.teamcity.stash.api.StashClient;
import mendhak.teamcity.stash.ui.UpdateChangeStatusFeature;
import mendhak.teamcity.stash.ui.UpdateChangesConstants;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.12 3:29
 */
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
        if (!feature.getType().equals(UpdateChangeStatusFeature.FEATURE_TYPE))
        {
            throw new IllegalArgumentException("Unexpected feature type " + feature.getType());
        }

        final UpdateChangesConstants c = new UpdateChangesConstants();


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

                        client.Notify(status, build.getBuildTypeId(),
                                getBuildDisplayName(build), myWeb.getViewResultsUrl(build),
                                getBuildDisplayDescription(build), getRevision(build));

                        Logger.LogInfo("Updated Stash status for revision: " + hash + ", buildId: " + build.getBuildId() + ", status: " + status);

                    }
                }));
            }
        };
    }
}
