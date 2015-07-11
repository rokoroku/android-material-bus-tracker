package kr.rokoroku.mbus.api.seoulweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class TopisMapLineResult {

    public List<ResultEntity> result;

    public class ResultEntity {
        /**
         * x : 201453.4
         * y : 457186.7
         */
        public double x;
        public double y;
    }

}
