package com.example.golfer.objects;

import com.example.golfer.Utilities;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Player extends Group {
	
	private double width;
	private double height;
	private Translate position;
	private Rotate rotate;
	
	public Player ( double width, double height, Translate position) {
		this.width = width;
		this.height = height;
		this.position = position;

		Circle base=new Circle(width/2);
		base.setFill(Color.ORANGE);
		base.getTransforms().add(
				new Translate(width/2,height-width/2)
		);

		//Rectangle cannon = new Rectangle ( width, height, Color.LIGHTBLUE );

		Path cannon=new Path(
			new MoveTo(width/4,0),
			new LineTo(0,height-width/2),
			new HLineTo(width),
			new LineTo(width*3/4,0),
			new ClosePath()
		);

		cannon.setFill(Color.LIGHTBLUE);

		super.getChildren ( ).addAll ( cannon ,base);
		
		this.rotate = new Rotate ( );
		
		super.getTransforms ( ).addAll (
				position,
				new Translate ( width / 2, height - width/2 ),
				rotate,
				new Translate ( -width / 2, -height + width/2 )
		);
	}

	public Player ( double width, double height, Translate position,String type) {
		this.width = width;
		this.height = height;
		this.position = position;

		Circle base=new Circle(width/2);
		base.setFill(Color.RED);
		base.getTransforms().add(
				new Translate(width/2,height-width/2)
		);

		//Rectangle cannon = new Rectangle ( width, height, Color.LIGHTBLUE );
		Path cannon=null;
		if(type.equals("slow")) {
			 cannon = new Path(
					new MoveTo(0, 0),
					new LineTo(width / 4, height - width / 2),
					new HLineTo(width * 3 / 4),
					new LineTo(width, 0),
					new ClosePath()
			);
		}

		if(type.equals("fast")) {
			cannon = new Path(
					new MoveTo(0,0),
					new LineTo(width/4,height/4),
					new VLineTo(height-width/2),
					new HLineTo(3*width/4),
					new VLineTo(height/4),
					new LineTo(width,0),
					new ClosePath()
			);
		}else{ //slow
			cannon = new Path(
					new MoveTo(0,0),
					new LineTo(width/4,height/4),
					new VLineTo(height-width/2),
					new HLineTo(3*width/4),
					new VLineTo(height/4),
					new LineTo(width,0),
					new ClosePath()
			);
		}

		if(type.equals("fast"))
		cannon.setFill(Color.GOLDENROD);
		else
			cannon.setFill(Color.GREEN);


		super.getChildren ( ).addAll ( cannon ,base);

		this.rotate = new Rotate ( );

		super.getTransforms ( ).addAll (
				position,
				new Translate ( width / 2, height - width/2 ),
				rotate,
				new Translate ( -width / 2, -height + width/2 )
		);
	}

	
	public void handleMouseMoved ( MouseEvent mouseEvent, double minAngleOffset, double maxAngleOffset ) {
		Bounds bounds = super.getBoundsInParent ( );
		
		double startX = bounds.getCenterX ( );
		double startY = bounds.getMaxY ( );
		
		double endX = mouseEvent.getX ( );
		double endY = mouseEvent.getY ( );
		
		Point2D direction     = new Point2D ( endX - startX, endY - startY ).normalize ( );
		Point2D startPosition = new Point2D ( 0, -1 );
		
		double angle = ( endX > startX ? 1 : -1 ) * direction.angle ( startPosition );
		
		this.rotate.setAngle ( Utilities.clamp ( angle, minAngleOffset, maxAngleOffset ) );
	}
	
	public Translate getBallPosition ( ) {
		double startX = this.position.getX ( ) + this.width / 2;
		double startY = this.position.getY ( ) + this.height;
		
		double x = startX + Math.sin ( Math.toRadians ( this.rotate.getAngle ( ) ) ) * this.height;
		double y = startY - Math.cos ( Math.toRadians ( this.rotate.getAngle ( ) ) ) * this.height;
		
		Translate result = new Translate ( x, y );
		
		return result;
	}
	
	public Point2D getSpeed ( ) {
		double startX = this.position.getX ( ) + this.width / 2;
		double startY = this.position.getY ( ) + this.height;
		
		double endX = startX + Math.sin ( Math.toRadians ( this.rotate.getAngle ( ) ) ) * this.height;
		double endY = startY - Math.cos ( Math.toRadians ( this.rotate.getAngle ( ) ) ) * this.height;
		
		Point2D result = new Point2D ( endX - startX, endY - startY );
		
		return result.normalize ( );
	}
}
