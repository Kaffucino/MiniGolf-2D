package com.example.golfer.objects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.Random;

public class Enemy extends Rectangle {


    private Translate end_position;
    private Translate current_position;

    private double a;
    private double window_width , window_height;

    private int animation_duration=5; // in seconds

    private boolean animation_finished=true;

    private int sleep_time;


    public Enemy(double a, Translate position,double window_width,double window_height){

        super(0,0,a,a);

        end_position=new Translate(-1,-1);



        this.current_position=position;

        super.getTransforms().addAll(current_position);

        this.window_width=window_width;
        this.window_height=window_height;





        this.a=a;


        Image image=new Image("enemy.png");
        ImagePattern texture=new ImagePattern(image);

        super.setFill(texture);

    }


    public void generate_random_position(){
        Random seed_x=new Random(System.nanoTime());
        double random_x=(window_width-20 -20)*seed_x.nextDouble() + 20;
        Random seed_y=new Random(System.nanoTime());
        double random_y=(window_height-20 -20)*seed_y.nextDouble() + 20;

        end_position.setX(random_x);
        end_position.setY(random_y);

        Random seed_time=new Random(System.nanoTime());

        sleep_time= (int)((7-3)*seed_time.nextDouble()) + 3;


        Timeline timeline=new Timeline(new KeyFrame(Duration.seconds(0),new KeyValue(current_position.xProperty(),current_position.getX())
        ,new KeyValue(current_position.yProperty(),current_position.getY())),
                new KeyFrame(Duration.seconds(animation_duration),new KeyValue(current_position.xProperty(),random_x)
                        ,new KeyValue(current_position.yProperty(),random_y)));


        timeline.setOnFinished(actionEvent ->
        {
            animation_finished=true;
        });
        timeline.setDelay(Duration.seconds(sleep_time));
        timeline.play();

    }


    public boolean update(Ball ball){

        if(animation_finished){
            animation_finished=false;
            generate_random_position();

        }

        //collision
        if(ball!=null){

            Bounds enemy_bound=super.getBoundsInParent();

            double center_enemy_x=enemy_bound.getCenterX();
            double center_enemy_y=enemy_bound.getCenterY();

            Bounds ball_bound=ball.getBoundsInParent();

            double center_ball_x=ball_bound.getCenterX();
            double center_ball_y=ball_bound.getCenterY();

            double radius=ball.getRadius();

            double distance=Math.sqrt(Math.pow(center_enemy_x-center_ball_x,2)+ Math.pow(center_enemy_y-center_ball_y,2));

            if(distance < (radius + a/2))
            return true;



        }
        return false;

    }








}
