package com.example.golfer.objects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Translate;

import java.util.Random;

public class Chip extends Circle {

    private static  final double low=2.5;
    private static  final double high=6;


    private double time_to_live;
    private int ability; // 0 - extra points , 1- extra time, 2-extra try


    public Chip(double radius, Translate position){

        super(radius);
        super.getTransforms().add(position);

        Image image=new Image("chip.png");
        ImagePattern texture=new ImagePattern(image);

        super.setFill(texture);

        Random random=new Random(System.nanoTime());
        time_to_live=(high-low)*random.nextDouble() + low;
        ability= (int)((3-0)*random.nextDouble() + 0);


    }
    public double getTime_to_live(){
        return  time_to_live;
    }
    public void change_time_to_live(double time){
        this.time_to_live-=time;
    }
    public int getAbility(){
        return  ability;
    }

    public boolean handleCollision (Circle ball) {
        Bounds ballBounds = ball.getBoundsInParent ( );

        double ballX      = ballBounds.getCenterX ( );
        double ballY      = ballBounds.getCenterY ( );
        double ballRadius = ball.getRadius ( );

        Bounds chipBounds = super.getBoundsInParent ( );

        double chipX      = chipBounds.getCenterX ( );
        double chipY      = chipBounds.getCenterY ( );
        double holeRadius = super.getRadius ( );

        double distanceX = chipX - ballX;
        double distanceY = chipY - ballY;

        double distanceSquared = distanceX * distanceX + distanceY * distanceY;

        boolean result = distanceSquared < ( holeRadius * holeRadius );



        return result;
    }


}
