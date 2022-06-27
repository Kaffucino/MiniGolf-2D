package com.example.golfer.objects;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Translate;

public class Hole extends Circle {

	private int points;

	public Hole ( double radius, Translate position,Color color, int points ) {



		super (radius);
		
		super.getTransforms ( ).addAll ( position );

		this.points=points;
		Stop [] stops=new Stop[]{
				new Stop(0,Color.BLACK),
				new Stop(1,color)
		};

		RadialGradient radialGradient=new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,stops);

		super.setFill(radialGradient);


	}

	public int getPoints(){
		return  points;
	}
	
	public boolean handleCollision (Circle ball, double speed_limit, Point2D current_speed) {
		Bounds ballBounds = ball.getBoundsInParent ( );
		
		double ballX      = ballBounds.getCenterX ( );
		double ballY      = ballBounds.getCenterY ( );
		double ballRadius = ball.getRadius ( );
		
		Bounds holeBounds = super.getBoundsInParent ( );
		
		double holeX      = holeBounds.getCenterX ( );
		double holeY      = holeBounds.getCenterY ( );
		double holeRadius = super.getRadius ( );
		
		double distanceX = holeX - ballX;
		double distanceY = holeY - ballY;
		
		double distanceSquared = distanceX * distanceX + distanceY * distanceY;
		
		boolean result = distanceSquared < ( holeRadius * holeRadius );



		if(speed_limit <= current_speed.magnitude())
		result=false;

		return result;
	};
}
