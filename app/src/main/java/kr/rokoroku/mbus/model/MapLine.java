package kr.rokoroku.mbus.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rok on 2015. 7. 10..
 */
public class MapLine implements Parcelable {

    private double latitude;
    private double longitude;
    private Direction direction;

    public MapLine() {
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "MapLine{" +
                "direction=" + direction +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.direction == null ? -1 : this.direction.ordinal());
    }

    protected MapLine(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        int tmpDirection = in.readInt();
        this.direction = tmpDirection == -1 ? null : Direction.values()[tmpDirection];
    }

    public static final Creator<MapLine> CREATOR = new Creator<MapLine>() {
        public MapLine createFromParcel(Parcel source) {
            return new MapLine(source);
        }

        public MapLine[] newArray(int size) {
            return new MapLine[size];
        }
    };
}
