package com.example.golfer;

import com.example.golfer.objects.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main extends Application implements EventHandler<Event> {
	private static final double WINDOW_WIDTH  = 600;
	private static final double WINDOW_HEIGHT = 800;
	
	private static final double PLAYER_WIDTH            = 20;
	private static final double PLAYER_HEIGHT           = 80;
	private static final double PLAYER_MAX_ANGLE_OFFSET = 60;
	private static final double PLAYER_MIN_ANGLE_OFFSET = -60;
	
	private static final double MS_IN_S            = 1e3;
	private static final double NS_IN_S            = 1e9;
	private static final double MAXIMUM_HOLD_IN_S  = 3;
	private static double MAXIMUM_BALL_SPEED = 1500;
	private static final double BALL_RADIUS        = Main.PLAYER_WIDTH / 4;
	private static final double BALL_DAMP_FACTOR   = 0.995;
	private static final double MIN_BALL_SPEED     = 1;
	
	private static final double HOLE_RADIUS = 3 * BALL_RADIUS;
	private static final double SPEED_LIMIT_HOLE = 750;
	private static final int NUMBER_OF_TRIES = 5;
	private static final int TIME_LIMIT = 30;
	private static final int EXTRA_POINTS = 5;
	private static final int EXTRA_TIME = 5;
	private static final int EXTRA_TRY = 1;


	private Group root;
	private Player player;
	private Ball ball;
	private long time;
	private Hole holes[];
	private Rectangle speed_indicator;
	private boolean mouse_pressed;
	private boolean mouse_released;
	private Rectangle obstacles[];
	private Rectangle ice_obstacles[];
	private Rectangle mud_obstacles[];
	private boolean ball_dont_update; //da bi se zavrsila animacija loptice
	private Circle tries[];
	private int remaining_tries;
	private int score;
	private Text score_text;
	private boolean space_pressed;

	private int time_remaining;
	private Text time_remaining_text;
	private Rectangle timer;
	private double time_count; //protok vremena

	private ArrayList<Chip> chip_list;
	private Random random_chip_spawner;
	private double random_chip_time;

	private boolean first_shot; //igra pocinje kada ovo bude true


	private ArrayList<Teleport> teleport_list;
	private ArrayList<Enemy> enemy_list;


	private void addHoles (String field_name ) {

		Translate hole0Position = new Translate ();
		Translate hole1Position = new Translate ();
		Translate hole2Position = new Translate ();
		Translate hole3Position = new Translate ();


		if(field_name.equals("GrassField")){
			hole0Position = new Translate (
					Main.WINDOW_WIDTH / 2,
					Main.WINDOW_HEIGHT * 0.1
			);
			hole1Position = new Translate (
					Main.WINDOW_WIDTH / 2,
					Main.WINDOW_HEIGHT * 0.4
			);
			hole2Position = new Translate (
					Main.WINDOW_WIDTH / 3,
					Main.WINDOW_HEIGHT * 0.25
			);
			hole3Position = new Translate (
					Main.WINDOW_WIDTH * 2 / 3,
					Main.WINDOW_HEIGHT * 0.25
			);

		}else if(field_name.equals("Space")){

			hole0Position = new Translate (
					Main.WINDOW_WIDTH / 7,
					Main.WINDOW_HEIGHT * 0.1
			);
			hole1Position = new Translate (
					Main.WINDOW_WIDTH *5/6,
					Main.WINDOW_HEIGHT * 0.1
			);
			hole2Position = new Translate (
					Main.WINDOW_WIDTH / 2,
					Main.WINDOW_HEIGHT / 2
			);
			hole3Position = new Translate (
					Main.WINDOW_WIDTH * 1 / 3 +100,
					Main.WINDOW_HEIGHT * 0.25
			);


		}else if(field_name.equals("SkyField")){

			hole0Position = new Translate (
					Main.WINDOW_WIDTH / 5,
					Main.WINDOW_HEIGHT * 0.6
			);
			hole1Position = new Translate (
					Main.WINDOW_WIDTH /5*2,
					Main.WINDOW_HEIGHT * 0.1
			);
			hole2Position = new Translate (
					Main.WINDOW_WIDTH / 5*3,
					Main.WINDOW_HEIGHT *0.1
			);
			hole3Position = new Translate (
					Main.WINDOW_WIDTH / 5*4,
					Main.WINDOW_HEIGHT * 0.6
			);



		}



		Hole hole0 = new Hole ( Main.HOLE_RADIUS, hole0Position,Color.SANDYBROWN,20 );
		Hole hole1 = new Hole ( Main.HOLE_RADIUS, hole1Position,Color.LIGHTGREEN,5 );
		Hole hole2 = new Hole ( Main.HOLE_RADIUS, hole2Position,Color.YELLOW,10 );
		Hole hole3 = new Hole ( Main.HOLE_RADIUS, hole3Position,Color.YELLOW,10 );
		this.root.getChildren ( ).addAll ( hole0,hole1,hole2,hole3 );
		
		this.holes = new Hole[] {
				hole0,
				hole1,
				hole2,
				hole3,
		};
	}



	public void add_tries(){

		remaining_tries=5;

		tries=new Circle[NUMBER_OF_TRIES];
		for(int i =0 ; i< NUMBER_OF_TRIES ; ++i){

			Circle circle=new Circle(BALL_RADIUS);
			circle.setFill(Color.RED);
			circle.setCenterX(WINDOW_WIDTH-4*BALL_RADIUS - i*3*BALL_RADIUS);
			circle.setCenterY(2*BALL_RADIUS);

			tries[i]=circle;

			root.getChildren().add(circle);

		}

	}

	public void add_obstalces(String field_name){

		if(field_name.equals("GrassField")) {

			Rectangle obstl1 = new Rectangle(WINDOW_WIDTH / 5, WINDOW_HEIGHT / 2, WINDOW_WIDTH / 5, 15);
			obstl1.setFill(Color.GRAY);

			Rectangle obstl2 = new Rectangle(3 * WINDOW_WIDTH / 5, WINDOW_HEIGHT / 2, WINDOW_WIDTH / 5, 15);
			obstl2.setFill(Color.GRAY);

			Rectangle obstl3 = new Rectangle(WINDOW_WIDTH / 2 - 5, WINDOW_HEIGHT / 6, 15, WINDOW_WIDTH / 5);
			obstl3.setFill(Color.GRAY);

			this.obstacles = new Rectangle[]{
					obstl1, obstl2, obstl3
			};

			Rectangle mud1 = new Rectangle(WINDOW_WIDTH / 6, WINDOW_HEIGHT / 8, 30, 30);
			mud1.setFill(Color.BROWN);

			Rectangle ice1 = new Rectangle(5 * WINDOW_WIDTH / 6 - 20, WINDOW_HEIGHT / 8, 30, 30);
			ice1.setFill(Color.LIGHTBLUE);

			Rectangle ice2 = new Rectangle(WINDOW_WIDTH / 6, 6 * WINDOW_HEIGHT / 8 - 50, 30, 30);
			ice2.setFill(Color.LIGHTBLUE);

			Rectangle mud2 = new Rectangle(5 * WINDOW_WIDTH / 6 - 20, 6 * WINDOW_HEIGHT / 8 - 50, 30, 30);
			mud2.setFill(Color.BROWN);

			ice_obstacles = new Rectangle[]{
					ice1, ice2
			};
			mud_obstacles = new Rectangle[]{
					mud1, mud2
			};


			root.getChildren().addAll(obstl1, obstl2, obstl3, mud1, ice1, ice2, mud2);

		} else if(field_name.equals("Space")){

			Rectangle obstl1 = new Rectangle(WINDOW_WIDTH / 3 + 40 , WINDOW_HEIGHT / 2 + 80, WINDOW_WIDTH / 5, 15);
			obstl1.setFill(Color.MEDIUMPURPLE);

			Rectangle obstl2 = new Rectangle(0,0, WINDOW_WIDTH / 5, 15);
			obstl2.setFill(Color.MEDIUMPURPLE);
			obstl2.getTransforms().addAll(new Translate(3 * WINDOW_WIDTH / 5 + 50 , WINDOW_HEIGHT / 2 - 80 ),new Rotate());


			Rectangle obstl3 = new Rectangle(0,0, WINDOW_WIDTH / 5, 15);
			obstl3.getTransforms().addAll(new Translate(WINDOW_WIDTH / 5  , WINDOW_HEIGHT / 2 - 80 ),new Rotate());
			obstl3.setFill(Color.MEDIUMPURPLE);

			this.obstacles = new Rectangle[]{
					obstl1, obstl2, obstl3
			};

			Rectangle mud1 = new Rectangle(WINDOW_WIDTH / 2 - 15 , WINDOW_HEIGHT / 8, 30, 30);
			mud1.setFill(Color.PALEGREEN);

			Rectangle ice1 = new Rectangle(WINDOW_WIDTH / 4 , WINDOW_HEIGHT * 5/ 8, 30, 30);
			ice1.setFill(Color.RED);

			Rectangle ice2 = new Rectangle(WINDOW_WIDTH *3/ 4 , WINDOW_HEIGHT * 5/ 8, 30, 30);
			ice2.setFill(Color.RED);


			ice_obstacles = new Rectangle[]{
					ice1,ice2
			};
			mud_obstacles = new Rectangle[]{
					mud1
			};


			root.getChildren().addAll(obstl1, obstl2, obstl3,mud1,ice1,ice2);
		}else if(field_name.equals("SkyField")){

			Rectangle obstl1 = new Rectangle(WINDOW_WIDTH / 5 + 30 , WINDOW_HEIGHT / 2 + 30  , 15, WINDOW_WIDTH / 5);
			obstl1.setFill(Color.WHITE);

			Rectangle obstl2 = new Rectangle(WINDOW_WIDTH *4/ 5 - 50 ,WINDOW_HEIGHT / 2 + 30, 15, WINDOW_WIDTH / 5);
			obstl2.setFill(Color.WHITE);



			Rectangle obstl3 = new Rectangle(WINDOW_WIDTH / 3 + 40, WINDOW_HEIGHT/4, WINDOW_WIDTH / 5, 15);

			obstl3.setFill(Color.WHITE);



			Rectangle ice1 = new Rectangle(WINDOW_WIDTH / 2 - 15 , WINDOW_HEIGHT *2/ 4 , 30, 30);
			ice1.setFill(Color.GOLD);


			Rectangle mud2 = new Rectangle(WINDOW_WIDTH / 2 - 15 , WINDOW_HEIGHT *1/ 4+40 , 30, 30);
			mud2.setFill(Color.DARKGRAY);

			Rectangle mud3 = new Rectangle(WINDOW_WIDTH / 2 - 15 , WINDOW_HEIGHT *3/ 4 - 40 , 30, 30);
			mud3.setFill(Color.DARKGRAY);


			this.obstacles = new Rectangle[]{
					obstl1, obstl2, obstl3
			};


			ice_obstacles = new Rectangle[]{
					ice1
			};
			mud_obstacles = new Rectangle[]{
					mud2,mud3
			};


			root.getChildren().addAll(obstl1, obstl2, obstl3,mud2,mud3,ice1);





		}








	}
	public void add_fence(){
		Image image=new Image("fence.jpg");
		ImagePattern texture=new ImagePattern(image);

		Rectangle h1=new Rectangle(20,0,WINDOW_WIDTH-20,20);
		h1.setFill(texture);

		Rectangle v1=new Rectangle(0,0,20,WINDOW_HEIGHT);
		v1.setFill(texture);

		Rectangle h2=new Rectangle(20,WINDOW_HEIGHT-20,WINDOW_WIDTH-20,20);
		h2.setFill(texture);

		Rectangle v2=new Rectangle(WINDOW_WIDTH-20,0,20,WINDOW_HEIGHT-20);
		v2.setFill(texture);



		root.getChildren().addAll(h1,v1,h2,v2);
	}

	public void add_timer(){

		time_remaining=TIME_LIMIT;
		timer=new Rectangle(WINDOW_WIDTH-15,20,15,WINDOW_HEIGHT-20);

		timer.setFill(Color.RED);

		time_remaining_text=new Text(Integer.toString(time_remaining));

		time_remaining_text.setFont(Font.font(15));

		time_remaining_text.getTransforms().add(new Translate(
				WINDOW_WIDTH-15,40
		));

		root.getChildren().addAll(timer,time_remaining_text);

	}

	public void add_teleports(){
		teleport_list=new ArrayList<>();

		Translate pos1=new Translate(WINDOW_WIDTH/6,WINDOW_HEIGHT/5);
		Translate pos2=new Translate(WINDOW_WIDTH/6,WINDOW_HEIGHT*4/5);
		Teleport teleport1=new Teleport(HOLE_RADIUS,pos1,pos2);

		pos1=new Translate(WINDOW_WIDTH*5/6,WINDOW_HEIGHT/5);
		pos2=new Translate(WINDOW_WIDTH*5/6,WINDOW_HEIGHT*4/5);
		Teleport teleport2=new Teleport(HOLE_RADIUS,pos1,pos2);





		teleport_list.add(teleport1);
		teleport_list.add(teleport2);

		root.getChildren().addAll(teleport1.getFirst_portal(),teleport1.getSecond_portal());
		root.getChildren().addAll(teleport2.getFirst_portal(),teleport2.getSecond_portal());



	}
	public void add_enemies(String field_name){

		double a=30;

		if(field_name.equals("Space"))
			a=60;

		if(field_name.equals("SkyField"))
			a=50;

		Translate t=new Translate(WINDOW_WIDTH/4, WINDOW_HEIGHT/4);

		Enemy enemy=new Enemy(a,t,WINDOW_WIDTH,WINDOW_HEIGHT);

		Translate t2=new Translate(WINDOW_WIDTH*3/4, WINDOW_HEIGHT/4);

		Enemy enemy2=new Enemy(a,t2,WINDOW_WIDTH,WINDOW_HEIGHT);

		enemy_list.add(enemy);
		enemy_list.add(enemy2);

		if(field_name.equals("Space")){
			Enemy enemy3=new Enemy(a,new Translate(WINDOW_WIDTH/2-15,WINDOW_HEIGHT/14),WINDOW_WIDTH,WINDOW_HEIGHT);
			enemy_list.add(enemy3);
			Image image=new Image("enemy_alien.png");
			ImagePattern texture=new ImagePattern(image);
			enemy3.setFill(texture);
			enemy.setFill(texture);
			enemy2.setFill(texture);

			root.getChildren().addAll(enemy3);
		}else if(field_name.equals("SkyField")){
			Image image=new Image("cloud.png");
			ImagePattern texture=new ImagePattern(image);
			enemy.setFill(texture);
			enemy2.setFill(texture);

		}


		root.getChildren().addAll(enemy,enemy2);



	}

	public void add_chip(){

		Random random=new Random(System.nanoTime());

		double lowX=20+HOLE_RADIUS;
		double highX= WINDOW_WIDTH-(20+HOLE_RADIUS);
		double lowY= 20+HOLE_RADIUS;
		double highY= WINDOW_HEIGHT-(20+HOLE_RADIUS);
		Translate position=new Translate();

		while(true){

			double x = (highX-lowX)*random.nextDouble() + lowX;
			double y = (highY-lowY)*random.nextDouble() + lowY;
			boolean again=false;

			for(int i=0;i<holes.length;++i){ // preklapanje s rupama
				Bounds bound=holes[i].getBoundsInParent();

				double distance= Math.sqrt(Math.pow(bound.getCenterX()-x,2)+ Math.pow(bound.getCenterY()-y,2));


				if(distance < 2*HOLE_RADIUS)
				{
					again=true;
					break;
				}



			}
			for(int i=0;i<obstacles.length;++i){

				Bounds obstacle_bound=obstacles[i].getBoundsInParent();

				double obstacle_min_x=obstacle_bound.getMinX();
				double obstacle_min_y=obstacle_bound.getMinY();
				double obstacle_max_x=obstacle_bound.getMaxX();
				double obstacle_max_y=obstacle_bound.getMaxY();

				if((x + HOLE_RADIUS) >= obstacle_min_x &&  (x-HOLE_RADIUS) <= obstacle_max_x){
					double distance1=Math.abs(y-obstacle_min_y);
					double distance2=Math.abs(y-obstacle_max_y);

					if(distance1 < 1.5*HOLE_RADIUS|| distance2 < 1.5*HOLE_RADIUS)
					{
						again=true;
						break;
					}

				}
				if((y + HOLE_RADIUS) >=obstacle_min_y && (y - HOLE_RADIUS) <=obstacle_max_y){

					double distance1=Math.abs(x-obstacle_min_x);
					double distance2=Math.abs(x-obstacle_max_x);

					if(distance1 <= 1.5*HOLE_RADIUS|| distance2 <= 1.5*HOLE_RADIUS)
					{
						again=true;
						break;
					}

				}


			}


			//TELEPORTS CHECK

			for(int i=0;i<teleport_list.size();++i){

				double portal_radius=teleport_list.get(i).getFirst_portal().getRadius();

				Bounds blue_bound=teleport_list.get(i).getFirst_portal().getBoundsInParent();

				double portal1_x=blue_bound.getCenterX();
				double portal1_y=blue_bound.getCenterY();

				Bounds orange_bound=teleport_list.get(i).getSecond_portal().getBoundsInParent();

				double portal2_x=orange_bound.getCenterX();
				double portal2_y=orange_bound.getCenterY();

				double distance1= Math.sqrt(Math.pow(x-portal1_x,2)+ Math.pow(y-portal1_y,2));
				double distance2= Math.sqrt(Math.pow(x-portal2_x,2)+ Math.pow(y-portal2_y,2));

				if(distance1 < portal_radius + HOLE_RADIUS || distance2 < portal_radius + HOLE_RADIUS )
				{
					again=true;
					break;
				}




			}

			//ICE CHECK BEGIN

			for(int i=0;i<ice_obstacles.length;++i){
				Bounds ice_bound=ice_obstacles[i].getBoundsInParent();

				double centerX= ice_bound.getCenterX();
				double centerY=ice_bound.getCenterY();

				double distance= Math.sqrt(Math.pow(x-centerX,2) + Math.pow(y-centerY,2));
				if(distance < (HOLE_RADIUS + ice_obstacles[i].getWidth()/2))
				{

					again=true;
					break;
				}

			}

			//ICE CHECK END

			//MUD CHECK BEGIN

			for(int i=0;i<this.mud_obstacles.length;++i){
				Bounds mud_bound=mud_obstacles[i].getBoundsInParent();

				double centerX= mud_bound.getCenterX();
				double centerY=mud_bound.getCenterY();

				double distance= Math.sqrt(Math.pow(x-centerX,2) + Math.pow(y-centerY,2));
				if(distance < (HOLE_RADIUS + mud_obstacles[i].getWidth()/2))
				{
					again=true;
					break;
				}

			}

			//MUD CHECK END



			//CHIP CHECK
			for (int i=0;i<chip_list.size();++i){

				Bounds bounds=chip_list.get(i).getBoundsInParent();

				double distance=Math.sqrt(Math.pow(x-bounds.getCenterX(),2) + Math.pow(y-bounds.getCenterY(),2) );

				if(distance < 2*HOLE_RADIUS)
				{
					again=true;
					break;
				}

			}










			if(again)
				continue;

			position.setX(x);
			position.setY(y);


			break;
		}








		Chip chip = new Chip(HOLE_RADIUS, position);

		chip_list.add(chip);


		root.getChildren().add(chip);


	}


	public void start_game(Stage stage){
		this.root  = new Group ( );

		mouse_pressed=false;
		mouse_released=false;
		ball_dont_update=false;
		space_pressed=false;
		first_shot=false;

		this.chip_list=new ArrayList<>();
		this.enemy_list=new ArrayList<>();

		this.random_chip_spawner=new Random(System.nanoTime());

//		//obstacles
//		add_obstalces();

		//fence
		add_fence();

		//tries
		add_tries();

		//timer
		add_timer();

//		//teleport
//		add_teleports();
//
//		//enemies
//
//		add_enemies();

		score=0;
		score_text=new Text("0");
		score_text.setFont(Font.font(20));
		score_text.getTransforms().add(new Translate(
				10,15
		));

		root.getChildren().add(score_text);
		//speed indicator
		this.speed_indicator=new Rectangle(0,WINDOW_HEIGHT,10,10);
		speed_indicator.setFill(Color.RED);
		root.getChildren().add(speed_indicator);


		Scene scene = new Scene ( this.root, Main.WINDOW_WIDTH, WINDOW_HEIGHT, Color.BLACK );

		//background
//		Image image=new Image("grass.jpg");
//		ImagePattern background=new ImagePattern(image);
//		scene.setFill(background);




		Translate playerPosition = new Translate (
				Main.WINDOW_WIDTH / 2 - Main.PLAYER_WIDTH / 2,
				Main.WINDOW_HEIGHT - Main.PLAYER_HEIGHT
		);

		this.player = new Player (
				Main.PLAYER_WIDTH,
				Main.PLAYER_HEIGHT,
				playerPosition
		);

		this.root.getChildren ( ).addAll ( this.player );

		//this.addHoles ( );

		scene.addEventHandler (
				MouseEvent.MOUSE_MOVED,
				mouseEvent -> this.player.handleMouseMoved (
						mouseEvent,
						Main.PLAYER_MIN_ANGLE_OFFSET,
						Main.PLAYER_MAX_ANGLE_OFFSET
				)
		);

		scene.addEventHandler ( MouseEvent.ANY, this );
		scene.addEventHandler ( KeyEvent.ANY, this );




		Timer timer = new Timer (
				deltaNanoseconds -> {
					double deltaSeconds = ( double ) deltaNanoseconds / Main.NS_IN_S;
					time_count+=deltaSeconds;


					//REMAINING TIME BEGIN

					double timer_step=(WINDOW_HEIGHT - 50) / TIME_LIMIT * (deltaSeconds);

					if(time_remaining!=0 && !(remaining_tries==0 && this.ball==null) && first_shot) {
						this.timer.setY(this.timer.getY() + timer_step);

						this.time_remaining_text.setY(this.time_remaining_text.getY() + timer_step);
					}


					//CHIP TIME_TO_LIVE BEGIN
					ArrayList<Chip> list_pom=new ArrayList<>();
					if(!(time_remaining<=0 || (this.remaining_tries==0 && this.ball==null))) {
						for (int i = 0; i < chip_list.size(); ++i) {
							chip_list.get(i).change_time_to_live(deltaSeconds);

							if (chip_list.get(i).getTime_to_live() > 0)
								list_pom.add(chip_list.get(i));
							else
								root.getChildren().remove(chip_list.get(i));

						}
					}else{
						for (int i = 0; i < chip_list.size(); ++i)
							this.root.getChildren().remove(chip_list.get(i));

					}
					chip_list=list_pom;
					//CHIP TIME_TO_LIVE END

					//ENEMY UPDATE BEGIN

					if(time_remaining!=0 && remaining_tries!=0 && first_shot) {
						for (int i = 0; i < enemy_list.size(); ++i) {
							boolean collision = enemy_list.get(i).update(ball);
							if (collision) {
								this.root.getChildren().remove(ball);
								ball = null;
							}


						}
					}

					//ENEMY UPDATE END



					if(time_count>=1){ // prosla je 1 sekunda

						random_chip_time= (12-3.5)*random_chip_spawner.nextDouble() + 3.5;


						if(random_chip_time >= 5 && random_chip_time <= 7 && (time_remaining>0 && this.remaining_tries!=0) && first_shot)
							add_chip();

						if(time_remaining>0  && !(remaining_tries==0 && this.ball==null) && first_shot) {
							--time_remaining;
							time_remaining_text.setText(time_remaining + "");
						}




						time_count=0;
					}




					//REMAINING TIME END

					//SPEED IND BEGIN ---------------------------------------
					double step=WINDOW_HEIGHT/3. * (deltaSeconds/5);

					if(mouse_pressed && this.ball==null && remaining_tries!=0 && time_remaining!=0)
						speed_indicator.setScaleY(speed_indicator.getScaleY()- step );
					else if(mouse_released) {
						root.getChildren().remove(speed_indicator);
						speed_indicator = new Rectangle(0, WINDOW_HEIGHT, 10, 10);
						speed_indicator.setFill(Color.RED);
						root.getChildren().add(speed_indicator);
					}
					//SPEED IND END ------------------------------------------



					//specijalni slucaj ako loptica upadne u rupicu pa se klikne space BEGIN
					if(space_pressed && !ball_dont_update)
					{
						this.root.getChildren().remove(this.ball);
						this.ball = null;
						ball_dont_update=false;

						space_pressed = false;
					}
					//specijalni slucaj ako loptica upadne u rupicu pa se klikne space END

					if ( this.ball != null && !ball_dont_update  && time_remaining!=0  ) {


						boolean stopped = this.ball.update (
								deltaSeconds,
								20,
								Main.WINDOW_WIDTH-20,
								20,
								Main.WINDOW_HEIGHT-20,
								Main.BALL_DAMP_FACTOR,
								Main.MIN_BALL_SPEED,
								this.obstacles,
								ice_obstacles,
								mud_obstacles,
								teleport_list

						);


						//CHIPS BEGIN
						for(int i=0;i<chip_list.size();++i){

							if(this.chip_list.get(i).handleCollision(this.ball))
							{
								if(this.chip_list.get(i).getAbility()==0){ // extra points
									score+=EXTRA_POINTS;
									score_text.setText(score+"");

									this.root.getChildren().remove(chip_list.get(i));

									this.chip_list.remove(i);
									break;


								}else if(this.chip_list.get(i).getAbility()==1) // extra time
								{
									double time_step=(WINDOW_HEIGHT - 50) / TIME_LIMIT * EXTRA_TIME;
									time_remaining+=EXTRA_TIME;
									time_remaining_text.setText(time_remaining+"");
									this.timer.setY(this.timer.getY() - time_step);

									this.time_remaining_text.setY(this.time_remaining_text.getY() - time_step);
									this.root.getChildren().remove(chip_list.get(i));

									this.chip_list.remove(i);
									break;

								}else{ //extra try

									if(remaining_tries < 5){


										Circle circle=new Circle(BALL_RADIUS);
										circle.setFill(Color.RED);
										circle.setCenterX(WINDOW_WIDTH-4*BALL_RADIUS - (remaining_tries)*3*BALL_RADIUS);
										circle.setCenterY(2*BALL_RADIUS);

										tries[remaining_tries++]=circle;

										root.getChildren().add(circle);

									}
									this.root.getChildren().remove(chip_list.get(i));

									this.chip_list.remove(i);
									break;



								}

							}


						}
						//CHIPS END



						boolean isInHole = Arrays.stream ( this.holes ).anyMatch ( hole -> hole.handleCollision ( this.ball,Main.SPEED_LIMIT_HOLE ,ball.getSpeed()) );

						if ( stopped || isInHole || space_pressed ) {

							if(isInHole){

								for(int i=0;i<holes.length;++i){
									if(this.holes[i].handleCollision(this.ball,Main.SPEED_LIMIT_HOLE ,ball.getSpeed()))
									{
										score+=this.holes[i].getPoints();
										score_text.setText(""+score);
									}
								}



								Scale scale=new Scale();
								ball.getTransforms().add(scale);
								Timeline timeline=new Timeline(new KeyFrame(Duration.seconds(0),new KeyValue(scale.yProperty(),1),new KeyValue(scale.xProperty(),1)),
										new KeyFrame(Duration.seconds(2),new KeyValue(scale.yProperty(),0.25),new KeyValue(scale.xProperty(),0.25))	);
								ball_dont_update=true;
								timeline.setOnFinished(actionEvent -> {
									this.root.getChildren().remove(this.ball);
									this.ball = null;
									ball_dont_update=false;
								});

								timeline.play();




							}
							else {
								//stopped or space_pressed



								this.root.getChildren().remove(this.ball);
								this.ball = null;
								space_pressed = false;



							}



						}
					}
				}
		);
		timer.start ( );
		scene.setCursor ( Cursor.NONE );

		//CHOOSE FIELD SCENE BEGIN

		Group root_field=new Group();

		Scene scene_field=new Scene(root_field, Main.WINDOW_WIDTH, WINDOW_HEIGHT, Color.BLACK);
		Text text=new Text("CHOOSE A FIELD ");

		Text title=new Text("Golfer 2D");
		title.getTransforms().add(new Translate(WINDOW_WIDTH/5,WINDOW_HEIGHT/5));
		title.setFont(Font.font(80));
		title.setFill(Color.WHITE);

		text.setFill(Color.RED);
		text.setFont(Font.font(50));
		text.getTransforms().add(new Translate(WINDOW_WIDTH/5,WINDOW_HEIGHT/2));


		Text grass_text=new Text("GrassField");
		grass_text.setFill(Color.GREEN);
		grass_text.getTransforms().add(new Translate(WINDOW_WIDTH/5,WINDOW_HEIGHT*2/3));
		grass_text.setFont(Font.font(30));

		Text space_text=new Text("Space");
		space_text.setFill(Color.PURPLE);
		space_text.getTransforms().add(new Translate(WINDOW_WIDTH/2-15,WINDOW_HEIGHT*2/3));
		space_text.setFont(Font.font(30));


		Text sky_text=new Text("SkyField");
		sky_text.setFill(Color.SKYBLUE);
		sky_text.getTransforms().add(new Translate(WINDOW_WIDTH*2/3,WINDOW_HEIGHT*2/3));
		sky_text.setFont(Font.font(30));


		root_field.getChildren().addAll(title,text,grass_text,space_text,sky_text);


		grass_text.setOnMouseEntered(mouseDragEvent -> {
			grass_text.setFill(Color.RED);
		});
		grass_text.setOnMouseExited(mouseDragEvent -> {
			grass_text.setFill(Color.GREEN);

		});

		space_text.setOnMouseEntered(mouseDragEvent -> {
			space_text.setFill(Color.RED);
		});
		space_text.setOnMouseExited(mouseDragEvent -> {
			space_text.setFill(Color.PURPLE);

		});

		sky_text.setOnMouseEntered(mouseDragEvent -> {
			sky_text.setFill(Color.RED);
		});
		sky_text.setOnMouseExited(mouseDragEvent -> {
			sky_text.setFill(Color.SKYBLUE);

		});


		grass_text.setOnMouseClicked(mouseEvent -> {

			setTerrain("GrassField",scene);
			//stage.setScene(scene);
			setCannon(stage,scene);

		});

		space_text.setOnMouseClicked(mouseEvent -> {

			setTerrain("Space",scene);
			setCannon(stage,scene);

		//	stage.setScene(scene);

		});

		sky_text.setOnMouseClicked(mouseEvent -> {

			setTerrain("SkyField",scene);
			setCannon(stage,scene);

		//	stage.setScene(scene);

		});




		//CHOOSE FIELD SCENE END








		stage.setTitle ( "Golfer" );
		stage.setResizable ( false );
		stage.setScene ( scene_field );
		stage.show ( );
	}


	public void setUpCannon(String cannon){

			if(cannon.equals("normal")){

			}else if(cannon.equals("fast")){
				Translate playerPosition = new Translate (
						Main.WINDOW_WIDTH / 2 - Main.PLAYER_WIDTH / 2,
						Main.WINDOW_HEIGHT - Main.PLAYER_HEIGHT
				);

				this.root.getChildren().remove(this.player);

				this.player = new Player (
						Main.PLAYER_WIDTH,
						Main.PLAYER_HEIGHT,
						playerPosition,
						"fast"
				);
				this.MAXIMUM_BALL_SPEED=this.MAXIMUM_BALL_SPEED*2;
				this.root.getChildren().add(this.player);

			}else if(cannon.equals("slow")){//slow
				Translate playerPosition = new Translate (
						Main.WINDOW_WIDTH / 2 - Main.PLAYER_WIDTH / 2,
						Main.WINDOW_HEIGHT - Main.PLAYER_HEIGHT
				);

				this.root.getChildren().remove(this.player);

				this.player = new Player (
						Main.PLAYER_WIDTH,
						Main.PLAYER_HEIGHT,
						playerPosition,
						"slow"
				);
				this.root.getChildren().add(this.player);


				this.MAXIMUM_BALL_SPEED=this.MAXIMUM_BALL_SPEED/2;
			}


	}

	public void setCannon(Stage stage,Scene scena){

		Group root_cannon=new Group();

		Scene scene_cannon=new Scene(root_cannon, Main.WINDOW_WIDTH, WINDOW_HEIGHT, Color.BLACK);
		Text text=new Text("CHOOSE A CANNON ");

		Text title=new Text("Golfer 2D");
		title.getTransforms().add(new Translate(WINDOW_WIDTH/5,WINDOW_HEIGHT/5));
		title.setFont(Font.font(80));
		title.setFill(Color.WHITE);

		text.setFill(Color.RED);
		text.setFont(Font.font(50));
		text.getTransforms().add(new Translate(WINDOW_WIDTH/7,WINDOW_HEIGHT/2));


		Text normal_cannon_text=new Text("Regular");
		normal_cannon_text.setFill(Color.GREEN);
		normal_cannon_text.getTransforms().add(new Translate(WINDOW_WIDTH/5,WINDOW_HEIGHT*2/3));
		normal_cannon_text.setFont(Font.font(30));

		Text fast_cannon_text=new Text("Fast");
		fast_cannon_text.setFill(Color.PURPLE);
		fast_cannon_text.getTransforms().add(new Translate(WINDOW_WIDTH/2-15,WINDOW_HEIGHT*2/3));
		fast_cannon_text.setFont(Font.font(30));


		Text slow_cannon_text=new Text("Slow");
		slow_cannon_text.setFill(Color.SKYBLUE);
		slow_cannon_text.getTransforms().add(new Translate(WINDOW_WIDTH*2/3,WINDOW_HEIGHT*2/3));
		slow_cannon_text.setFont(Font.font(30));

		normal_cannon_text.setOnMouseEntered(mouseDragEvent -> {
			normal_cannon_text.setFill(Color.RED);
		});
		normal_cannon_text.setOnMouseExited(mouseDragEvent -> {
			normal_cannon_text.setFill(Color.GREEN);

		});

		fast_cannon_text.setOnMouseEntered(mouseDragEvent -> {
			fast_cannon_text.setFill(Color.RED);
		});
		fast_cannon_text.setOnMouseExited(mouseDragEvent -> {
			fast_cannon_text.setFill(Color.PURPLE);

		});

		slow_cannon_text.setOnMouseEntered(mouseDragEvent -> {
			slow_cannon_text.setFill(Color.RED);
		});
		slow_cannon_text.setOnMouseExited(mouseDragEvent -> {
			slow_cannon_text.setFill(Color.SKYBLUE);

		});


		normal_cannon_text.setOnMouseClicked(mouseEvent -> {

			setUpCannon("normal");
			stage.setScene(scena);


		});

		fast_cannon_text.setOnMouseClicked(mouseEvent -> {
			setUpCannon("fast");

			stage.setScene(scena);


		});

		slow_cannon_text.setOnMouseClicked(mouseEvent -> {
			setUpCannon("slow");

			stage.setScene(scena);


		});






		root_cannon.getChildren().addAll(title,text,normal_cannon_text,fast_cannon_text,slow_cannon_text);

		stage.setScene(scene_cannon);

	}


	public void setTerrain(String field_name,Scene scene){

		if(field_name.equals("GrassField")){

			Image image=new Image("grass.jpg");
			ImagePattern background=new ImagePattern(image);
			scene.setFill(background);



		}else if(field_name.equals("Space")){
			Image image=new Image("space.png");
			ImagePattern background=new ImagePattern(image);
			scene.setFill(background);

		}else if(field_name.equals("SkyField"))
		{
			Image image=new Image("sky.jpg");
			ImagePattern background=new ImagePattern(image);
			scene.setFill(background);
		}
		add_obstalces(field_name);
		add_teleports();
		add_enemies(field_name);
		this.addHoles(field_name);


	}


	@Override
	public void start ( Stage stage ) throws IOException {
		start_game(stage);
	}
	
	public static void main ( String[] args ) {
		launch ( );
	}
	
	@Override public void handle ( Event event ) {

		if(event instanceof MouseEvent) { //mouse event

			if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED) && ((MouseEvent)event).isPrimaryButtonDown()) {
				this.time = System.currentTimeMillis();
				mouse_pressed = true;
				mouse_released = false;

			} else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
				mouse_pressed = false;
				mouse_released = true;
				if (this.time != -1 && this.ball == null && remaining_tries != 0 && time_remaining!=0) {


					if(!first_shot)
						first_shot=true;

					double value = (System.currentTimeMillis() - this.time) / Main.MS_IN_S;
					double deltaSeconds = Utilities.clamp(value, 0, Main.MAXIMUM_HOLD_IN_S);

					double ballSpeedFactor = deltaSeconds / Main.MAXIMUM_HOLD_IN_S * Main.MAXIMUM_BALL_SPEED;

					Translate ballPosition = this.player.getBallPosition();
					Point2D ballSpeed = this.player.getSpeed().multiply(ballSpeedFactor);

					this.ball = new Ball(Main.BALL_RADIUS, ballPosition, ballSpeed);
					this.root.getChildren().addAll(this.ball);
					root.getChildren().remove(tries[--remaining_tries]);
				}
				this.time = -1;
			}
		}else if(event instanceof KeyEvent){ //keyboard event

			KeyEvent keyboard_event=(KeyEvent)event;

			if(keyboard_event.getEventType().equals(KeyEvent.KEY_TYPED) && this.ball!=null && keyboard_event.getCharacter().equals(" ")){
				space_pressed=true;
			}


		}
	}
}