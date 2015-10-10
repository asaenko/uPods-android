package com.chickenkiller.upods2.controllers;

import com.chickenkiller.upods2.interfaces.ISimpleRequestHandler;
import com.chickenkiller.upods2.interfaces.IRequestHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class BackendManager {

    public enum TopType {

        MAIN_FEATURED("main_featured"), MAIN_BANNER("main_banner"), MAIN_PODCAST("podcasts_featured");

        private String name;

        TopType(String name) {
            this.name = name;
        }

        public String getStringValue() {
            return name;
        }
    }

    private final OkHttpClient client;
    private final int MAX_RETRY = 5;
    private static BackendManager backendManager;
    private ArrayList<QueueTask> searchQueue;

    private class QueueTask {
        public Request request;
        public IRequestHandler iRequestHandler;

        public QueueTask(Request request, IRequestHandler uiUpdater) {
            this.request = request;
            this.iRequestHandler = uiUpdater;
        }
    }

    private BackendManager() {
        super();
        this.client = new OkHttpClient();
    }

    public static BackendManager getInstance() {
        if (backendManager == null) {
            backendManager = new BackendManager();
            backendManager.searchQueue = new ArrayList<>();
        }
        return backendManager;
    }

    /**
     * Wrapper for simple backend queries
     *
     * @param request   - OKHttp request
     * @param uiUpdater - INetworkUIupdater to update UI
     */
    private void sendRequest(final Request request, final IRequestHandler uiUpdater) {
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.updateUIFailed();
                    searchQueueNextStep();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        final JSONObject jResponse = new JSONObject(response.body().string());
                        uiUpdater.updateUISuccess(jResponse);
                        searchQueueNextStep();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.updateUIFailed();
            searchQueueNextStep();
        }
    }

    private void sendRequest(final Request request, final ISimpleRequestHandler uiUpdater) {
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.updateUIFailed();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        uiUpdater.updateUISuccess(response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.updateUIFailed();
        }
    }


    private void sendRequest(QueueTask queueTask) {
        sendRequest(queueTask.request, queueTask.iRequestHandler);
    }

    private void searchQueueNextStep() {
        if (!searchQueue.isEmpty()) {
            searchQueue.remove(searchQueue.get(0));
            if (!searchQueue.isEmpty()) {
                sendRequest(searchQueue.get(0));
            }
        }
    }

    /**
     * Simple HTTP GET request
     *
     * @param url       - any url
     * @param uiUpdater ISimpleRequestHandler
     */
    public void sendRequest(String url, final ISimpleRequestHandler uiUpdater) {
        Request request = new Request.Builder().url(url).build();
        sendRequest(request, uiUpdater);
    }

    /**
     * Simple HTTP GET request
     *
     * @param url       - any url
     * @param uiUpdater IRequestHandler
     */
    public void sendRequest(String url, final IRequestHandler uiUpdater) {
        Request request = new Request.Builder().url(url).build();
        sendRequest(request, uiUpdater);
    }

    public void loadTops(TopType topType, String topLink, final IRequestHandler uiUpdater) {
        Request request = new Request.Builder()
                .url(topLink + topType.getStringValue())
                .build();
        sendRequest(request, uiUpdater);
    }

    public void doSearch(String query, final IRequestHandler uiUpdater) {
        Request request = new Request.Builder()
                .url(query)
                .build();
        if (searchQueue.isEmpty()) {
            searchQueue.add(new QueueTask(request, uiUpdater));
            sendRequest(request, uiUpdater);
        } else {
            searchQueue.add(new QueueTask(request, uiUpdater));
        }
    }

    public void clearSearchQueue() {
        searchQueue.clear();
    }
}
