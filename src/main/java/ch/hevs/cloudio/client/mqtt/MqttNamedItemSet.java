package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.NamedItem;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

class MqttNamedItemSet<T extends NamedItem> implements JsonSerializable, Iterable<T> {
    private List<T> items = new LinkedList<T>();

    public T getItem(String itemName) {
        for (T item: items) {
            if (item.getName().equals(itemName))
                return item;
        }
        return null;
    }

    public void addItem(T item) throws MqttDuplicateItemException {
        if (getItem(item.getName()) == null) {
            items.add(item);
        } else {
            throw new MqttDuplicateItemException();
        }
    }

    public boolean isEmtpy() {
        return items.isEmpty();
    }


    /*** JsonSerializable Implementation ******************************************************************************/

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (T item: items) {
            if (item instanceof JsonSerializable) {
                gen.writeObjectField(item.getName(), item);
            } else {
                gen.writeStringField(item.getName(), "NOT SERIALIZABLE");
            }
        }
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {}


    /*** Iterable<T> Implementation ***********************************************************************************/
    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (T item: items) {
            action.accept(item);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return items.spliterator();
    }
}
