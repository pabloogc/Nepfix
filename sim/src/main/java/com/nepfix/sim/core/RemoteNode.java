package com.nepfix.sim.core;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Instruction;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteNode extends Node {

    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkUrlFactory urlFactory = new OkUrlFactory(new OkHttpClient());
    @Expose private String remoteAddress;
    @Expose private String networkId;
    @Expose private int maxResults;
    @Expose private long timeoutMillis;

    @Override public List<Instruction> process(String input) {
        ComputationRequest body = new ComputationRequest(input, networkId, timeoutMillis, maxResults);
        try {
            URL url = new URL(remoteAddress + "/compute");
            HttpURLConnection httpURLConnection = urlFactory.open(url);
            httpURLConnection.setReadTimeout((int) timeoutMillis);
            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.getOutputStream().write(gson.toJson(body).getBytes());

            httpURLConnection.connect();
            int status = httpURLConnection.getResponseCode();
            if (status != 200) {
                throw new IOException(String.format("Unexpected response from: %s, status: %s", remoteAddress, status));
            }
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> remoteResult = gson.fromJson(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"), type);
            httpURLConnection.disconnect();
            List<Instruction> result = new ArrayList<>();
            for (String output : remoteResult) {
                connections.stream()
                        .filter(connection -> connection.getFilter().accept(output))
                        .forEach(connection -> result.add(new Instruction(output, connection.getDestiny())));
            }

            return result;

        } catch (IOException e) {
            System.out.println("Remote error from: " + remoteAddress + " " + e.getMessage());
            return Collections.emptyList(); //Timed out or error
        }

    }


}
