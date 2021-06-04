package com.techheralds.annam.prod.supplier;

import java.util.ArrayList;
import java.util.Map;

public class bundle {
    String name;
    ArrayList<Map<String, Object>> items;
    String img;

    public bundle(String name, ArrayList<Map<String, Object>> items, String img) {
        this.name = name;
        this.items = items;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Map<String, Object>> items) {
        this.items = items;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public bundle() {
    }


}
