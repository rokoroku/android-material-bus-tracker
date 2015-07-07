package kr.rokoroku.mbus.api.gbis.core;

import android.os.Build;

import kr.rokoroku.mbus.BuildConfig;
import retrofit.RestAdapter;
import retrofit.client.Client;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisRestClient {

    private static final String BASE_URL = "http://openapi.gbis.go.kr/ws/rest";

    private Client client;
    private String apiKey;
    private GbisRestInterface adapter;

    public GbisRestClient(Client client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    public GbisRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new GbisXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("serviceKey", apiKey);
                        }
                    })
                    .build()
                    .create(GbisRestInterface.class);
        }
        return adapter;
    }
}
