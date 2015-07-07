package kr.rokoroku.mbus.model;

/**
 * Created by rok on 2015. 6. 3..
 */
public enum District {
    SEOUL,
    GYEONGGI,
    INCHEON,
    UNKNOWN;

    public static District valueOfGbis(String siflag) {
        int flag = 0;

        if (siflag != null) try {
            flag = Integer.valueOf(siflag);
        } catch (Exception ignored) {

        }

        switch (flag) {
            case 1:
                return SEOUL;
            case 2:
                return GYEONGGI;
            case 3:
                return INCHEON;
            default:
                return UNKNOWN;
        }
    }
}
