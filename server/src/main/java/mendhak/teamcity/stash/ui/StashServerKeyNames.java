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

import jetbrains.buildServer.agent.Constants;

public class StashServerKeyNames
{
    public String getServerKey()
    {
        return "stash_host";
    }

    public String getUserNameKey()
    {
        return "stash_username";
    }

    public String getPasswordKey()
    {
        return Constants.SECURE_PROPERTY_PREFIX + "stash_password";
    }

    public String getOnlyLatestKey()
    {
        return "stash_only_latest";
    }

    public String getVCSIgnoreKey()
    {
        return "stash_vcsignorecsv";
    }

    public String getfailCancelledBuilds()
    {
        return "stash_failCancelledBuilds";
    }

}
