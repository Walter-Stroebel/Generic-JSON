/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.genjson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A fully generic JSON parser.
 *
 * Following the KISS principle, this class takes a random file as input and at
 * the user's choice does a Depth-First traversal or a Breath-First traversal.
 *
 * The design philosophy is simple, this either works (the user gets the
 * Name-Value pairs expected) or it does not. This collapses reality into a
 * single callback: usable data or SN:AFU.
 *
 * @author Walter Stroebel.
 */
public class GenJSON {

    /**
     * Sample invocation using an Ansible Facts file as input. As this can yield
     * anything, depending on Ansible version and varying Python versions on
     * host and target as well as OS type and version of the target, this is a
     * perfect example.
     *
     * @param args Not used.
     * @throws Exception It did not work.
     */
    public static void demo(String[] args) throws Exception {
        GenJSON genJSON = new GenJSON(new File("/home/walter/ansfacts/pi4"));
        genJSON.traverseBFS(new JsonNVP() {
            @Override
            public boolean nextNVP(JsonKey name, JsonPrimitive value) {
                System.out.println(name + " = " + value);
                return true;
            }
        });
    }

    public final File jsonFile;
    public final JsonElement root;
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Sample constructor.
     *
     * @param jsonFile Or Path, extend as desired.
     * @throws Exception It did not work.
     */
    public GenJSON(File jsonFile) throws Exception {
        this.jsonFile = jsonFile;
        try (BufferedReader rdr = new BufferedReader(new FileReader(jsonFile))) {
            this.root = gson.fromJson(rdr, JsonElement.class);
        }
    }

    /**
     * Depth first.
     *
     * @param element That we are at.
     * @param currentKey As build so far.
     * @param callback As defined by the user.
     * @return True if we are done, false if the user aborted.
     */
    private boolean traverseDFS(JsonElement element, JsonKey currentKey, JsonNVP callback) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (String key : obj.keySet()) {
                JsonKey newKey = new JsonKey(currentKey, key);
                traverseDFS(obj.get(key), newKey, callback);
            }
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonKey newKey = new JsonKey(currentKey, i);
                traverseDFS(arr.get(i), newKey, callback);
            }
        } else if (element.isJsonPrimitive()) {
            if (!callback.nextNVP(currentKey, element.getAsJsonPrimitive())) {
                return false; // Stop traversal if callback returns false
            }
        }
        return true;
    }

    /**
     * Breath first.
     *
     * @param element That we are at.
     * @param callback As defined by the user.
     * @return True if we are done, false if the user aborted.
     */
    private boolean traverseBFS(JsonElement element, JsonNVP callback) {
        Deque<Pair<JsonElement, JsonKey>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(element, null));

        while (!queue.isEmpty()) {
            Pair<JsonElement, JsonKey> current = queue.poll();
            JsonElement el = current.getKey();
            JsonKey key = current.getValue();

            if (el.isJsonObject()) {
                for (String k : el.getAsJsonObject().keySet()) {
                    queue.add(new Pair<>(el.getAsJsonObject().get(k), new JsonKey(key, k)));
                }
            } else if (el.isJsonArray()) {
                for (int i = 0; i < el.getAsJsonArray().size(); i++) {
                    queue.add(new Pair<>(el.getAsJsonArray().get(i), new JsonKey(key, i)));
                }
            } else if (el.isJsonPrimitive()) {
                if (!callback.nextNVP(key, el.getAsJsonPrimitive())) {
                    return false; // Stop traversal if callback returns false
                }
            }
        }
        return true;
    }

    /**
     * Parse Depth First.
     *
     * @param callback Will be called for each primitive.
     * @return false if processing was aborted.
     */
    public boolean traverseDFS(JsonNVP callback) {
        return traverseDFS(root, null, callback);  // Start DFS from the root
    }

    /**
     * Parse Breath First.
     *
     * @param callback Will be called for each primitive.
     * @return false if processing was aborted.
     */
    public boolean traverseBFS(JsonNVP callback) {
        return traverseBFS(root, callback); // Start BFS from the root
    }

    private static class Pair<K, V> {

        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

}
