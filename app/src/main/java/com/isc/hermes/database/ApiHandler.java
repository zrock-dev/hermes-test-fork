package com.isc.hermes.database;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class responsible for handling API requests and managing the execution of asynchronous tasks.
 */
public class ApiHandler {
    private final String API_URL = "https://api-rest-hermes.onrender.com/";

    private ExecutorService executorService;
    private final ApiRequestHandler requestHandler;
    private static ApiHandler instance;

    /**
     * Constructs a new instance of ApiHandler.
     */
    public ApiHandler() {
        executorService = Executors.newSingleThreadExecutor();
        requestHandler = ApiRequestHandler.getInstance();
    }

    /**
     * Retrieves the future result of a collection string from the API.
     *
     * @param params The parameters to be appended to the API URL.
     * @return A Future object representing the asynchronous result of the API request.
     */
    public Future<String> getFutureCollectionString(String params) {
        return executorService.submit(
                () -> requestHandler.getDataFromApi(API_URL + params)
        );
    }

    /**
     * Posts future collections to the specified API URL with the given parameters and object.
     *
     * @param params  The parameters to append to the API URL.
     * @param object  The object to be posted as JSON data.
     * @return A Future representing the result of the asynchronous operation.
     */
    public Future<?> postFutureCollections(String params, Object object) {
        String url = API_URL + params;
        Gson gson = new Gson();
        String result = gson.toJson(object);
        System.out.println("RESULT: " + result);
        return executorService.submit(() -> requestHandler.postDataFromApi(url, result));
    }

    /**
     * Updates the validity of a verification code through the specified API URL and boolean value.
     *
     * @param params    The parameters to append to the API URL.
     * @param isValid   The boolean value indicating the validity of the verification code.
     * @return A Future representing the result of the asynchronous operation.
     */
    public Future<?> updateVerificationCodeValidity(String params, boolean isValid) {
        String url = API_URL + params;
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("isValid", isValid);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return executorService.submit(() -> requestHandler.updateDataInDatabase(url, updateData.toString()));
    }

    /**
     * Retrieves the singleton instance of ApiHandler.
     *
     * @return The singleton instance of ApiHandler.
     */
    public static ApiHandler getInstance() {
        if (instance == null) instance = new ApiHandler();
        return instance;
    }
}
