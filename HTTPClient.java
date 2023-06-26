import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HTTPClient {
    private String method = "GET";
    private String url = "";
    private Map<String, String> args = new HashMap<String, String>();
    private Map<String, String> headers = new HashMap<String, String>();
    private boolean isJson = false;

    public HTTPClient(){}
    public HTTPClient(String url)
    {
        this.url = url;
    }

    public void addHeader(String key, String value)
    {
        headers.put(key, value);
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void addParam(String key, String value)
    {
        args.put(key, value);
    }

    public Map<String, String> getParams()
    {
        return args;
    }

    public void setURL(String url)
    {
        this.url = url;
    }

    public String getURL()
    {
        return url;
    }

    public void setMethod(String method)
    {
        this.method = method.toUpperCase(Locale.ROOT);
    }

    public String getMethod()
    {
        return method;
    }

    public void json(boolean isJson)
    {
        this.isJson = isJson;
    }

    public String send()
    {
        String output = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            for(Map.Entry<String, String> entry : headers.entrySet())
            {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            byte[] out = {};
            StringJoiner joiner = new StringJoiner("&");
            if(!isJson) {
                for (Map.Entry<String, String> entry : args.entrySet()) {
                    joiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                out = joiner.toString().getBytes(StandardCharsets.UTF_8);
            }
            else
            {
                String jsonPayload = "{";

                for(Map.Entry<String, String> entry : args.entrySet())
                {
                    jsonPayload += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                }
                jsonPayload += "}";
                out = jsonPayload.getBytes(StandardCharsets.UTF_8);
            }
            int argLength = out.length;
            connection.setFixedLengthStreamingMode(argLength);

            connection.connect();
            if(argLength != 0) {
                OutputStream os = connection.getOutputStream();
                os.write(out);
                os.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = reader.readLine()) != null)
            {
                output += line + "\n";
            }
            reader.close();
        }catch(Exception e)
        {
            output = "Error: " + e.getLocalizedMessage();
        }
        return output;
    }
}
