import net.minecraft.src.Session;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class SessionID
{
    public static final Session GetSSID(String username, String password)
    {
        byte[] b = null;
        String jsonEncoded =
                "{\"agent\":{\"name\":\"Minecraft\",\"version\":1},\"username\":\""
                        + username
                        + "\",\"password\":\""
                        + password + "\"}";
        String response = executePost("https://authserver.mojang.com/authenticate", jsonEncoded);
        if (response == null || response.isEmpty())
            return null;

        // Session ID = "token:<accessToken>:<profile ID>"
        // Username will probably *not be an email address
        String[] pieces = response.split("\"");
        String sessionID = "token:" + pieces[3] + ":" + pieces[13];    // TODO: Get these values based on json field name, not just index location (which may change and then be invalid!)
        String userName = pieces[17];
        Session session = new Session(userName, sessionID);
        return session;
    }

    public static String executePost(String targetURL, String urlParameters)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}