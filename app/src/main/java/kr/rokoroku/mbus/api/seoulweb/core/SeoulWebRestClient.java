package kr.rokoroku.mbus.api.seoulweb.core;

import retrofit.RestAdapter;
import retrofit.client.Client;

/**
 * Created by rok on 2015. 4. 22..
 */
public class SeoulWebRestClient {

    private Client client;
    private SeoulWebRestInterface adapter;

    public SeoulWebRestClient(Client client) {
        this.client = client;
    }

    public SeoulWebRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint("http:")
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new TopisJsonConverter())
                    .build()
                    .create(SeoulWebRestInterface.class);
        }
        return adapter;
    }

}
