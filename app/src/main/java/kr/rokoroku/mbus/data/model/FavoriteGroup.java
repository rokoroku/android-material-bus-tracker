package kr.rokoroku.mbus.data.model;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kr.rokoroku.mbus.util.SerializeUtil;

/**
 * Created by rok on 2015. 7. 7..
 */
public class FavoriteGroup implements Serializable {

    static final long serialVersionUID = 1L;

    private String name;
    private List<FavoriteItem> list;

    public FavoriteGroup(String name) {
        super();
        this.name = name;
        this.list = new ArrayList<>();
    }

    public FavoriteGroup(String name, Collection<? extends FavoriteItem> collection) {
        this(name);
        if (collection != null) {
            this.list.addAll(collection);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FavoriteGroup{" +
                "name='" + name + '\'' +
                ", " + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FavoriteGroup that = (FavoriteGroup) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void move(int from, int to) {
        FavoriteItem item = remove(from);
        if (item != null) {
            add(to, item);
        }
    }

    public void add(int index, FavoriteItem object) {
        list.add(index, object);
    }

    public boolean add(FavoriteItem object) {
        return list.add(object);
    }

    public FavoriteItem get(int index) {
        return list.get(index);
    }

    public boolean contains(Object object) {
        return list.contains(object);
    }

    public FavoriteItem remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object object) {
        return list.remove(object);
    }

    public FavoriteItem set(int index, FavoriteItem object) {
        return list.set(index, object);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public Collection<? extends FavoriteItem> getItems() {
        return list;
    }

    public long getId() {
        return name.hashCode();
    }

    public static final Serializer<FavoriteGroup> SERIALIZER = new Serializer<FavoriteGroup>() {
        @Override
        public void serialize(DataOutput out, FavoriteGroup value) throws IOException {
            if(value != null && value.name != null) {
                out.writeByte(1);
                out.writeUTF(value.name);
                SerializeUtil.writeList(out, value.list, FavoriteItem.SERIALIZER);
            } else {
                out.writeByte(0);
            }
        }

        @Override
        public FavoriteGroup deserialize(DataInput in, int available) throws IOException {
            boolean isNull = in.readByte() == 0;
            if(!isNull) {
                String name = in.readUTF();
                List<FavoriteItem> items = SerializeUtil.readList(in, FavoriteItem.SERIALIZER);
                return new FavoriteGroup(name, items);
            } else {
                return null;
            }
        }
    };
}
