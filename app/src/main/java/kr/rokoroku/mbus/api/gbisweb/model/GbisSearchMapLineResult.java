package kr.rokoroku.mbus.api.gbisweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class GbisSearchMapLineResult {

    private ResultEntity result;
    private boolean success;

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResultEntity getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public class ResultEntity {
        private GgEntity gg;

        public void setGg(GgEntity gg) {
            this.gg = gg;
        }

        public GgEntity getGg() {
            return gg;
        }

        public class GgEntity {

            private UpLineEntity upLine;
            private DownLineEntity downLine;

            public void setUpLine(UpLineEntity upLine) {
                this.upLine = upLine;
            }

            public void setDownLine(DownLineEntity downLine) {
                this.downLine = downLine;
            }

            public UpLineEntity getUpLine() {
                return upLine;
            }

            public DownLineEntity getDownLine() {
                return downLine;
            }

            public class UpLineEntity {

                private int count;
                private List<ListEntity> list;

                public void setCount(int count) {
                    this.count = count;
                }

                public void setList(List<ListEntity> list) {
                    this.list = list;
                }

                public int getCount() {
                    return count;
                }

                public List<ListEntity> getList() {
                    return list;
                }

                public class ListEntity {
                    /**
                     * linkId : 2180046700
                     * lon : 14127284.783138
                     * type :
                     * seq : 1
                     * lat : 4529605.06895
                     */
                    private String linkId;
                    private String lon;
                    private String type;
                    private String seq;
                    private String lat;

                    public void setLinkId(String linkId) {
                        this.linkId = linkId;
                    }

                    public void setLon(String lon) {
                        this.lon = lon;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public void setSeq(String seq) {
                        this.seq = seq;
                    }

                    public void setLat(String lat) {
                        this.lat = lat;
                    }

                    public String getLinkId() {
                        return linkId;
                    }

                    public String getLon() {
                        return lon;
                    }

                    public String getType() {
                        return type;
                    }

                    public String getSeq() {
                        return seq;
                    }

                    public String getLat() {
                        return lat;
                    }
                }
            }

            public class DownLineEntity {

                private int count;
                private List<ListEntity> list;

                public void setCount(int count) {
                    this.count = count;
                }

                public void setList(List<ListEntity> list) {
                    this.list = list;
                }

                public int getCount() {
                    return count;
                }

                public List<ListEntity> getList() {
                    return list;
                }

                public class ListEntity {
                    private String linkId;
                    private String lon;
                    private String type;
                    private String seq;
                    private String lat;

                    public void setLinkId(String linkId) {
                        this.linkId = linkId;
                    }

                    public void setLon(String lon) {
                        this.lon = lon;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public void setSeq(String seq) {
                        this.seq = seq;
                    }

                    public void setLat(String lat) {
                        this.lat = lat;
                    }

                    public String getLinkId() {
                        return linkId;
                    }

                    public String getLon() {
                        return lon;
                    }

                    public String getType() {
                        return type;
                    }

                    public String getSeq() {
                        return seq;
                    }

                    public String getLat() {
                        return lat;
                    }
                }
            }
        }
    }
}
