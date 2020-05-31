package com.example.mybricklist.Tools

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.mybricklist.Model.InventoryParts
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XML_Writer(inventoryPartslist: MutableList<InventoryParts>, context: Context) {

    var list = inventoryPartslist;
    var context: Context = context;
    val fileType = "SavedXMLS";

    fun saveXML() {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(context, "Nie można zapisać pliku, brak dostępu do karty SD!", Toast.LENGTH_LONG);
            return;
        }

        if(list == null || list.isEmpty()){
            Toast.makeText(context, "Zestaw kompletny, brak niekompletnych części.", Toast.LENGTH_LONG);
            return;
        }

        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()
        val rootElement: Element =  doc.createElement("INVENTORY");
        doc.appendChild(rootElement);

        this.list.forEach {
            val element: Element =  doc.createElement("ITEM");
            val typeId: Element =  doc.createElement("ITEMTYPE");
            typeId.appendChild(doc.createTextNode(it.typeId));
            element.appendChild(typeId);
            val itemId: Element =  doc.createElement("ITEMID");
            itemId.appendChild(doc.createTextNode(it.itemId));
            element.appendChild(itemId);
            val colorId: Element =  doc.createElement("COLOR");
            colorId.appendChild(doc.createTextNode(it.colorId.toString()));
            element.appendChild(colorId);
            val qtyFilled: Element =  doc.createElement("QTYFILLED");
            qtyFilled.appendChild(doc.createTextNode( (it.quantityInSet-it.quantityInSet).toString() ));
            element.appendChild(qtyFilled);

            rootElement.appendChild(element);
        }
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");


        val file = File(context.getExternalFilesDir(fileType) , "${list[0].inventoryId}.xml");
        transformer.transform(DOMSource(doc), StreamResult(file));
        this.readXml(list[0].inventoryId);
    }

    fun readXml(_inventoryId: Int){
        val path = context.getExternalFilesDir(fileType).toString() + "/${_inventoryId}.xml";
        var text = "";
        File(path).forEachLine { text+=it; text+='\n'}
        val ad = AlertDialog.Builder(context).create();
        ad.setMessage(text);
        ad.setCancelable(true);
        ad.show();
    }


    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }



}


