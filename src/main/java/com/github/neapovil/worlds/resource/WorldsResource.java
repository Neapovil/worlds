package com.github.neapovil.worlds.resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

public final class WorldsResource
{
    final List<World> worlds = new ArrayList<>();

    public static class World
    {
        public String name;
        @JsonAdapter(LocationAdapter.class)
        public Location spawn;

        public World(String name, Location spawn)
        {
            this.name = name;
            this.spawn = spawn;
        }
    }

    class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location>
    {
        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            final JsonObject jsonobject = json.getAsJsonObject();
            final Map<String, Object> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> i : jsonobject.asMap().entrySet())
            {
                map.put(i.getKey(), i.getValue().getAsString());
            }

            return Location.deserialize(map);
        }

        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context)
        {
            final JsonObject jsonobject = new JsonObject();

            for (Map.Entry<String, Object> i : src.serialize().entrySet())
            {
                jsonobject.add(i.getKey(), new JsonPrimitive("" + i.getValue()));
            }

            return jsonobject;
        }
    }
}
