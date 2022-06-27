package com.example.golfer.objects;

import com.example.golfer.Utilities;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

import java.util.ArrayList;

public class Ball extends Circle {
	private Translate position;
	private Point2D speed;
	
	public Ball ( double radius, Translate position, Point2D speed ) {
		super ( radius, Color.RED );
		
		this.position = position;
		this.speed = speed;
		
		super.getTransforms ( ).addAll ( this.position );
	}

	public Point2D getSpeed(){
		return speed;
	}

	public boolean update (double ds, double left, double right, double top, double bottom, double dampFactor, double minBallSpeed, Rectangle[] obstacles, Rectangle[] ice, Rectangle[]mud, ArrayList<Teleport> teleports) {
		boolean result = false;
		
		double newX = this.position.getX ( ) + this.speed.getX ( ) * ds;
		double newY = this.position.getY ( ) + this.speed.getY ( ) * ds;
		
		double radius = super.getRadius ( );
		
		double minX = left + radius;
		double maxX = right - radius;
		double minY = top + radius;
		double maxY = bottom - radius;
		
		this.position.setX ( Utilities.clamp ( newX, minX, maxX ) );
		this.position.setY ( Utilities.clamp ( newY, minY, maxY ) );
	
		if ( newX < minX || newX > maxX ) {
			this.speed = new Point2D ( -this.speed.getX ( ), this.speed.getY ( ) );
		}
		
		if ( newY < minY || newY > maxY ) {
			this.speed = new Point2D ( this.speed.getX ( ), -this.speed.getY ( ) );
		}

		//TELEPORTS CHECK

		for(int i=0;i<teleports.size();++i){

			double portal_radius=teleports.get(i).getFirst_portal().getRadius();

			Bounds blue_bound=teleports.get(i).getFirst_portal().getBoundsInParent();

			double portal1_x=blue_bound.getCenterX();
			double portal1_y=blue_bound.getCenterY();

			Bounds orange_bound=teleports.get(i).getSecond_portal().getBoundsInParent();

			double portal2_x=orange_bound.getCenterX();
			double portal2_y=orange_bound.getCenterY();


			double distance1= Math.sqrt(Math.pow(newX-portal1_x,2)+ Math.pow(newY-portal1_y,2));

			if(distance1 < portal_radius + this.getRadius()){


				if(newX > portal1_x)
				this.position.setX ( Utilities.clamp ( portal2_x - this.getRadius() - portal_radius, minX, maxX ) );
				else
					this.position.setX ( Utilities.clamp ( portal2_x + this.getRadius() + portal_radius, minX, maxX ) );

				if(newY > portal1_y)
				this.position.setY ( Utilities.clamp ( portal2_y - this.getRadius() - portal_radius, minY, maxY ) );
				else
					this.position.setY ( Utilities.clamp ( portal2_y + this.getRadius() + portal_radius, minY, maxY ) );


				break;
			}



			double distance2= Math.sqrt(Math.pow(newX-portal2_x,2)+ Math.pow(newY-portal2_y,2));


			if(distance2 < portal_radius + this.getRadius()){



				if(newX > portal2_x)
					this.position.setX ( Utilities.clamp ( portal1_x - this.getRadius() - portal_radius, minX, maxX ) );
				else
					this.position.setX ( Utilities.clamp ( portal1_x + this.getRadius() + portal_radius, minX, maxX ) );

				if(newY > portal2_y)
					this.position.setY ( Utilities.clamp ( portal1_y - this.getRadius() - portal_radius, minY, maxY ) );
				else
					this.position.setY ( Utilities.clamp ( portal1_y + this.getRadius() + portal_radius, minY, maxY ) );

				break;
			}



		}





		//OBSTACLES CHECK BEGIN

		for (int i=0;i<obstacles.length;++i){

			Bounds obstacle_bound=obstacles[i].getBoundsInParent();

			double obstacle_min_x=obstacle_bound.getMinX();
			double obstacle_min_y=obstacle_bound.getMinY();
			double obstacle_max_x=obstacle_bound.getMaxX();
			double obstacle_max_y=obstacle_bound.getMaxY();

			if((newX + radius) >= obstacle_min_x &&  (newX-radius) <= obstacle_max_x){
				double distance1=Math.abs(newY-obstacle_min_y);
				double distance2=Math.abs(newY-obstacle_max_y);

				if(distance1 <= radius|| distance2 <= radius)
					this.speed = new Point2D ( this.speed.getX ( ), -this.speed.getY ( ) );

			}
			 if((newY + radius) >=obstacle_min_y && (newY - radius) <=obstacle_max_y){

				double distance1=Math.abs(newX-obstacle_min_x);
				double distance2=Math.abs(newX-obstacle_max_x);

				if(distance1 <= radius|| distance2 <= radius)
					this.speed = new Point2D ( -this.speed.getX ( ), this.speed.getY ( ) );


			}



		}
		//OBSTACLES CHECK END

		//ICE CHECK BEGIN
		boolean speed_up=false;

		for(int i=0;i<ice.length;++i){
			Bounds ice_bound=ice[i].getBoundsInParent();

			double centerX= ice_bound.getCenterX();
			double centerY=ice_bound.getCenterY();

			double distance= Math.sqrt(Math.pow(newX-centerX,2) + Math.pow(newY-centerY,2));
			if(distance < (radius + ice[i].getWidth()/2))
			{
				speed_up=true;
				break;
			}

		}

		//ICE CHECK END

		//MUD CHECK BEGIN
		boolean slow_down=false;

		for(int i=0;i<mud.length;++i){
			Bounds mud_bound=mud[i].getBoundsInParent();

			double centerX= mud_bound.getCenterX();
			double centerY=mud_bound.getCenterY();

			double distance= Math.sqrt(Math.pow(newX-centerX,2) + Math.pow(newY-centerY,2));
			if(distance < (radius + mud[i].getWidth()/2))
			{
				slow_down=true;
				break;
			}

		}

		//MUD CHECK END

		if(speed_up)
			this.speed = this.speed.multiply ( dampFactor + 0.2 );
		else if(slow_down)
			this.speed = this.speed.multiply ( dampFactor - 0.2 );
		else
		this.speed = this.speed.multiply ( dampFactor );


		double ballSpeed = this.speed.magnitude ( );
		
		if ( ballSpeed < minBallSpeed ) {
			result = true;
		}
		
		return result;
	}
}
