/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.genjson;

import com.google.gson.JsonPrimitive;

/**
 * The callback interface.
 *
 * @author walter
 */
public interface JsonNVP {

    /**
     * Will be called for every primitive in the JSON file, either in DFS or BFS
     * order.
     *
     * @param name Or key of path, the "id" of the item.
     * @param value The value of the item.
     * @return false to stop processing.
     */
    boolean nextNVP(JsonKey name, JsonPrimitive value);

}
