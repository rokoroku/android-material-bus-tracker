package kr.rokoroku.mbus.api.gbisweb.core;

import com.google.gson.Gson;

import kr.rokoroku.mbus.BuildConfig;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.GsonConverter;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisWebRestClient {

    private static final String BASE_URL = "http://www.gbis.go.kr/gbis2014";

    private Client client;
    private GbisWebRestInterface adapter;

    public GbisWebRestClient(Client client) {
        this.client = client;
    }

    public GbisWebRestInterface getAdapter() {
        if(adapter == null) {

            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new GsonConverter(new Gson(), "UTF-8"))
                    .build()
                    .create(GbisWebRestInterface.class);
        }
        return adapter;
    }
}
