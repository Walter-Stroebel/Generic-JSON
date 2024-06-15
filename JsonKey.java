/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.genjson;

/**
 * The name, key or path of any item in the JSON file.
 */
public class JsonKey {

    public final JsonKey parent;
    public final String word;
    public final Integer index;

    public JsonKey(JsonKey parent, String word) {
        this.parent = parent;
        this.word = word;
        this.index = null;
    }

    public JsonKey(JsonKey parent, int index) {
        this.parent = parent;
        this.word = null;
        this.index = index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != parent) {
            sb.append(parent.toString()).append('.');
        }
        if (null != word) {
            sb.append(word);
        } else {
            sb.append(index.toString());
        }
        return sb.toString();
    }

}
