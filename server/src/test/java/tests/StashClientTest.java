/*
 * Copyright 2000-2013 JetBrains s.r.o.
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


package tests;


import junit.framework.TestCase;
import mendhak.teamcity.stash.api.StashClient;


public class StashClientTest extends TestCase
{


    public void testStashUrlConstruction_ReturnsStashUrl()
    {
        StashClient client = new StashClient();
        String stashBase = "http://example.com";
        String revision = "12349782349";
        String stashBuildStatusUrl = client.GetBuildStatusUrl(stashBase, revision);
        assertEquals("http://example.com/rest/build-status/1.0/commits/12349782349", stashBuildStatusUrl);

    }


    public void testStashUrlConstructionWithTrailingSlash_RemovesTrailingSlash()
    {
        StashClient client = new StashClient();
        String stashBase = "http://example.com/";
        String revision = "12349782349";
        String stashBuildStatusUrl = client.GetBuildStatusUrl(stashBase, revision);
        assertEquals("http://example.com/rest/build-status/1.0/commits/12349782349", stashBuildStatusUrl);

    }

    public void testStashUrlConstructionWithInvalidBaseUrl_ReturnsNull()
    {
        StashClient client = new StashClient();
        String stashBase = "://example.com/";
        String revision = "12349782349";
        String stashBuildStatusUrl = client.GetBuildStatusUrl(stashBase, revision);
        assertNull(stashBuildStatusUrl);

    }

    public void testStashUrlConstructionWithHTTPSSchema_ReturnsUrl()
    {
        StashClient client = new StashClient();
        String stashBase = "https://example.com/";
        String revision = "12349782349";
        String stashBuildStatusUrl = client.GetBuildStatusUrl(stashBase, revision);
        assertEquals("https://example.com/rest/build-status/1.0/commits/12349782349", stashBuildStatusUrl);

    }






    public void testStashBuildStateFromTeamCityBuildState()
    {
        StashClient client = new StashClient();
        String stashBuildState = client.GetBuildState(StashClient.BuildState.IN_PROGRESS);
        assertEquals("INPROGRESS", stashBuildState);

        stashBuildState = client.GetBuildState(StashClient.BuildState.FAILED);
        assertEquals("FAILED", stashBuildState);

        stashBuildState = client.GetBuildState(StashClient.BuildState.SUCCESSFUL);
        assertEquals("SUCCESSFUL", stashBuildState);
    }


    public void testAuthorizationHeader()
    {
        StashClient client = new StashClient();
        String authHeaderValue = client.GetAuthorizationHeaderValue("testuser","2jfksfjadf");
        assertEquals("dGVzdHVzZXI6Mmpma3NmamFkZg==", authHeaderValue);
    }

    public void testBuildStatusJsonBody()
    {

        StashClient client = new StashClient();
        String jsonBody = client.GetJsonBody("SUCCESSFUL", "REPO-MASTER", "REPO-MASTER-42",
                "http://example.com/browse/REPO-MASTER-42", "A description...");

        String expected = "{\n" +
                "    \"state\": \"SUCCESSFUL\",\n" +
                "    \"key\": \"REPO-MASTER\",\n" +
                "    \"name\": \"REPO-MASTER-42\",\n" +
                "    \"url\": \"http://example.com/browse/REPO-MASTER-42\",\n" +
                "    \"description\": \"A description...\"\n" +
                "}";

        assertEquals(expected, jsonBody);
    }




}
