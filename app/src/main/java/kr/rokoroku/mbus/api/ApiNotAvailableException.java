package kr.rokoroku.mbus.api;

import kr.rokoroku.mbus.data.model.Provider;

/**
 * Created by rok on 2015. 7. 13..
 */
public class ApiNotAvailableException extends Exception {

    public ApiNotAvailableException(Provider provider) {
        super("No api found to given provider: " + provider.name());
    }

    public ApiNotAvailableException(String detailMessage) {
        super(detailMessage);
    }
}
