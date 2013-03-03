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


package mendhak.teamcity.stash.api;

import mendhak.teamcity.stash.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class StashClient
{


    private final String stashBaseUrl;
    private final String stashUsername;
    private final String stashPassword;

    public StashClient()
    {
        this("http://example.com", "testuser", "hunter2");
    }



    public StashClient(String stashBaseUrl, String username, String password)
    {
        this.stashBaseUrl = stashBaseUrl;
        stashUsername = username;
        stashPassword = password;
    }

    public String GetJsonBody(String buildState, String key, String name, String url, String description)
    {
        return String.format("{\n" +
                "    \"state\": \"%s\",\n" +
                "    \"key\": \"%s\",\n" +
                "    \"name\": \"%s\",\n" +
                "    \"url\": \"%s\",\n" +
                "    \"description\": \"%s\"\n" +
                "}", buildState, key, name, url, description);
    }

    public enum BuildState
    {
        IN_PROGRESS,
        SUCCESSFUL,
        FAILED
    }



    public String GetBuildStatusUrl(String stashBase, String revision)
    {

        if (!(stashBase.startsWith("http://") || stashBase.startsWith("https://")))
        {
            return null;
        }

        if (stashBase.endsWith("/"))
        {
            stashBase = stashBase.substring(0, stashBase.length() - 1);
        }

        return String.format("%s/rest/build-status/1.0/commits/%s", stashBase, revision);
    }

    public String GetBuildState(BuildState buildState)
    {
        return buildState.name().replace("_", "");
    }

    public void Notify(BuildState state, String key, String displayName,
                       String url, String description, String revision)
    {


        String stashUrl = GetBuildStatusUrl(stashBaseUrl, revision);
        String basicAuthHeader = GetAuthorizationHeaderValue(stashUsername, stashPassword);
        String jsonBody = GetJsonBody(GetBuildState(state), key, displayName, url, description);
        PostBuildStatusToStash(stashUrl, jsonBody, basicAuthHeader);

    }

    private String PostBuildStatusToStash(String targetURL, String body, String authHeader)
    {

        HttpURLConnection connection = null;
        try
        {

            Logger.LogInfo("Sending build status to " + targetURL);
            Logger.LogInfo("With body: " + body);
            Logger.LogInfo("Auth header: " + authHeader);

            connection = GetConnection(targetURL);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
            connection.setRequestProperty("Authorization", "Basic " + authHeader);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append("\r\n");
            }
            rd.close();
            return response.toString();

        }
        catch (Exception e)
        {

            Logger.LogError("Could not send data to Stash. ", e);
        }
        finally
        {

            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return null;

    }

    private HttpURLConnection GetConnection(String targetURL) throws IOException, NoSuchAlgorithmException, KeyManagementException
    {
        URL url = new URL(targetURL);
        if (targetURL.startsWith("http://"))
        {
            return (HttpURLConnection) url.openConnection();
        }

        //Create an all trusting SSL URL Connection
        //For in-house Stash servers with self-signed certs

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager()
                {
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier()
        {
            public boolean verify(String hostname, SSLSession session)
            {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        return (HttpsURLConnection) url.openConnection();

    }

    public String GetAuthorizationHeaderValue(String stashUsername, String stashPassword)
    {
        //To generate a basic authorization header, create a base64 encoded string of
        // username:password
        String headerFormat = String.format("%s:%s", stashUsername, stashPassword);
        return new String(Base64.encodeBase64(headerFormat.getBytes()));
    }


}
