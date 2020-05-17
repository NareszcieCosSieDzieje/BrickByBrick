package com.example.mybricklist.Tools

class ImageLoader {

    val legoLink = "https://www.lego.com/service/bricks/5/2/"; // +   code = 300126
    val bricklinkColorLink = "http://img.bricklink.com/P/7/3001old.gif";
    val bricklinkNoColorLink = "https://www.bricklink.com/PL/3430c02.jpg";

    enum class linkChoice {
        lego, brick, brickCol
    }

    //ogolnie sprawdzanie zdjecia powinno bc takie ze jesli nie ma w bazie danych to sprobuj linkiem
    //TODO: color == int?
    fun tryLink(elementCode: String, color: String, lChoice: linkChoice) {
        var link = "";
        if(lChoice == linkChoice.lego){
            link = legoLink + elementCode;
        } else if (lChoice == linkChoice.brick){
            link = bricklinkNoColorLink + elementCode + ".jpg"; //TODO: jpg|gif?
        } else if (lChoice == linkChoice.brickCol){
            link = bricklinkColorLink + color + elementCode + ".jpg"; //TODO: to samo gif czy jpg
        }
        //TODO: try to get the image

        //TODO: zapisz w bazei danych do tabeli Codes i dodaj jako image
    }


}