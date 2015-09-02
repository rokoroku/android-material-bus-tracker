package kr.rokoroku.mbus.data.model;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by rok on 2015. 6. 3..
 */
public enum Direction {
    UP(0),
    DOWN(1);

    private int value;

    Direction(int value) {
        this.value = value;
    }

    public static Direction valueOf(int value) {
        if (value == 0) return UP;
        else if (value == 1) return DOWN;
        return null;
    }

    public static final Serializer<Direction> SERIALIZER = new Serializer<Direction>() {
        @Override
        public void serialize(DataOutput out, Direction value) throws IOException {
            if (value == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeInt(value.value);
            }
        }

        @Override
        public Direction deserialize(DataInput in, int available) throws IOException {
            boolean isNull = in.readByte() == 0;
            if (!isNull) {
                int value = in.readInt();
                return Direction.valueOf(value);
            } else {
                return null;
            }
        }
    };
}
