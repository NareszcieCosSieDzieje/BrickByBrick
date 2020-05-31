package com.example.mybricklist.Model

class InventoryParts constructor() {

    var id: Int = 0;
    var inventoryId: Int = 0;
    var typeId: String = "0";
    var itemId: String = "0";
    var quantityInSet: Int = 0;
    var quantityInStore: Int = 0;
    var colorId: Int = 0;
    var extra: Int = 0;

    constructor(
        id: Int,
        inventoryId: Int,
        typeId: String,
        itemId: String,
        quantityInSet: Int,
        quantityInStore: Int,
        colorId: Int,
        extra: Int
    ) : this() {
        this.id = id
        this.inventoryId = inventoryId;
        this.typeId = typeId;
        this.itemId = itemId;
        this.quantityInSet = quantityInSet;
        this.quantityInStore = quantityInStore;
        this.colorId = colorId;
        this.extra = extra;
    }

    override fun toString(): String {
        return "InventoryParts(id=$id, inventoryId=$inventoryId, typeId=$typeId, itemId=$itemId, quantityInSet=$quantityInSet, quantityInStore=$quantityInStore, colorId=$colorId, extra=$extra)"
    }


    //TODO: var img: Image? = null;

}