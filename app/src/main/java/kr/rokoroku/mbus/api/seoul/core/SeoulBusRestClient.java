package kr.rokoroku.mbus.api.seoul.core;

import kr.rokoroku.mbus.BuildConfig;

import retrofit.RestAdapter;
import retrofit.client.Client;

/**
 * Created by rok on 2015. 4. 22..
 */
public class SeoulBusRestClient {

    private static final String BASE_URL = "http://ws.bus.go.kr/api/rest";

    private String apiKey;
    private Client client;
    private SeoulBusRestInterface adapter;

    public SeoulBusRestClient(Client client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    public SeoulBusRestInterface getAdapter() {
        if(adapter == null) {

            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new SeoulBusXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("ServiceKey", apiKey);
                        }
                    })
                    .build()
                    .create(SeoulBusRestInterface.class);
        }
        return adapter;
    }
}
