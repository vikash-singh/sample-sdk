package com.gopaysense.psdata.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Vikash Singh on 6/10/17.
 */

public class ServerUtil {

    public static final int CONNECTION_TIMEOUT_MILLISEC = 10000;
    public static final int SOCKET_TIMEOUT_MILLISEC = 30000;

    public class ServerResponse {
        int responseCode;
        String response;

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        protected ServerResponse(int responseCode, String response) {
            this.response = response;
            this.responseCode = responseCode;
        }
    }

    public ServerResponse post(String urlStr, String data) throws IOException {
        URL url = new URL(urlStr);
        HttpsURLConnection connection = null;
        InputStreamReader in = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(SOCKET_TIMEOUT_MILLISEC);
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);

            //Add User Identifiers
            //connection.setRequestProperty("device_id", this.deviceId);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            out.write(data);
            out.close();

//            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
//                return new ServerResponse(responseCode, null);
            }

            in = new InputStreamReader(connection.getInputStream());
            StringBuilder result = new StringBuilder();
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                result.append(buff, 0, read);
            }

            return new ServerResponse(responseCode, result.toString());
        } finally {
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
