package com.example.golfer.objects;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Translate;

public class Teleport {

    private Circle first_portal;
    private Circle second_portal;



    public Teleport(double radius, Translate position1,Translate position2){

        first_portal=new Circle(radius);
        second_portal=new Circle(radius);

        Image portal_blue=new Image("blue_portal.png");
        ImagePattern texture1=new ImagePattern(portal_blue);
        Image portal_orange=new Image("orange_portal.png");
        ImagePattern texture2=new ImagePattern(portal_orange);

        first_portal.setFill(texture1);
        second_portal.setFill(texture2);




        first_portal.getTransforms().add(position1);
        second_portal.getTransforms().add(position2);

    }

    public Circle getFirst_portal() {
        return first_portal;
    }

    public Circle getSecond_portal() {
        return second_portal;
    }
}
