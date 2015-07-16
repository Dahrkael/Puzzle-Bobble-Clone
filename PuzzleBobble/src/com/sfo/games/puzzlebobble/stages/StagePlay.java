package com.sfo.games.puzzlebobble.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.sfo.games.puzzlebobble.Game;
import com.sfo.games.puzzlebobble.Preferences;
import com.sfo.games.puzzlebobble.RoundLoader;
import com.sfo.games.puzzlebobble.Score;
import com.sfo.games.puzzlebobble.entities.Cannon;
import com.sfo.games.puzzlebobble.entities.Smoke;
import com.sfo.games.puzzlebobble.entities.Sphere;
import com.sfo.games.puzzlebobble.entities.Sphere.Colors;
import com.sfo.games.puzzlebobble.helpers.InputHelper;

public class StagePlay implements Stage 
{
	public static int LEFT_BORDER 		= 40;
	public static int RIGHT_BORDER 		= 440;
	public static int TOP_BORDER 		= 615;
	public static int BOTTOM_BORDER 	= 35;
	public static int DEADEND			= 185;
	public static int MAX_SPHERES		= -9; // fixed top
	
	private enum State {Beginning, Playing, Lost, Won};
	private static int MAX_ROUNDS = 5;
	private static int POINTS_PER_SPHERE = 10;
	
	private State state;
	private int round = 1;
	private RoundLoader loader;
	private Sprite background;
	private Sprite deadend;
	private Sprite youlose;
	private Sprite ready;
	private Sprite go;
	private BitmapFont font;
	private List<Smoke> smokes;
	private List<Sphere> spheres;
	private Cannon cannon;
	private Sphere currentSphere;
	private Sphere nextSphere;
	private Score score;
	private Sound boo;
	private float counter = 0.0f;
	
	@Override
	public void load() 
	{
		this.font = new BitmapFont();
		this.font.setScale(5);
		this.boo = Gdx.audio.newSound(Gdx.files.internal("sounds/boo.ogg"));
		this.background = new Sprite(new Texture(Gdx.files.internal("images/playscreen1.png")));
		this.deadend = new Sprite(new Texture(Gdx.files.internal("images/deadend.png")));
		this.deadend.setPosition(LEFT_BORDER, DEADEND);
		
		this.youlose = new Sprite(new Texture(Gdx.files.internal("images/youlose.png")));
		this.youlose.setPosition(Game.GAME_WIDTH / 2 - (this.youlose.getWidth() / 2), Game.GAME_HEIGHT / 2 - (this.youlose.getHeight() / 2));
		this.ready = new Sprite(new Texture(Gdx.files.internal("images/ready.png")));
		this.ready.setPosition(Game.GAME_WIDTH / 2 - (this.ready.getWidth() / 2), Game.GAME_HEIGHT / 2 - (this.ready.getHeight() / 2));
		this.go = new Sprite(new Texture(Gdx.files.internal("images/go.png")));
		this.go.setPosition(Game.GAME_WIDTH / 2 - (this.go.getWidth() / 2), Game.GAME_HEIGHT / 2 - (this.go.getHeight() / 2));
		
		this.score = new Score(20, 785);
		this.cannon = new Cannon();
		this.cannon.setPosition(Game.GAME_WIDTH / 2, 35);
		this.loader = new RoundLoader();
		this.smokes = new ArrayList<Smoke>();
		Smoke.preloadImages();
		this.state = State.Beginning;
		this.nextRound();
	}

	@Override
	public void dispose() 
	{
		//Sphere.destroySound.dispose();
		this.boo.dispose();
		this.cannon.dispose();
	}

	private void beginning(float delta)
	{
		this.counter += delta / 1000;
		if (this.counter > 3.0f) { this.state = State.Playing; }
	}
	
	private void playing(float delta)
	{	
		if (this.currentSphere.state() == Sphere.State.Stopped)
		{
			boolean brokeFlag = false;
			this.currentSphere.fixPosition(false);
			List<Sphere> similars = this.currentSphere.findSimilars(this.spheres);
			this.spheres.add(this.currentSphere);
			if (similars.size() > 2)
			{
				brokeFlag = true;
				InputHelper.play(Sphere.destroySound);
				InputHelper.vibrate(500);
				this.score.addPoints(similars.size() * POINTS_PER_SPHERE);
				this.smokeOnDestroyedSpheres(similars);
				this.spheres.removeAll(similars);
				// Delete floating ones
				List<Sphere> floatingSpheres = new ArrayList<Sphere>();
				Iterator<Sphere> it = this.spheres.iterator();
		        while(it.hasNext())
		        {
		        	Sphere sphere = it.next();
		        	if (sphere.floating(spheres))
		        	{
		        		floatingSpheres.add(sphere);
		        	}
		        }
		        if (floatingSpheres.size() > 0) 
		        {
		        	this.smokeOnDestroyedSpheres(floatingSpheres);
		        	this.spheres.removeAll(floatingSpheres);
		        	this.score.addPoints((int)Math.pow(2, floatingSpheres.size()-1) * 10); 
		        }
			}
			if (this.spheres.size() == 0)
			{ 
				this.currentSphere = null;
				this.nextSphere = null;
				this.score.addPoints(this.score.secondsLeft() * 1000);
				this.state = State.Won; 
				return; 
			}
			if (brokeFlag == false)
			{
				if (this.currentSphere.gridPosition().y <= MAX_SPHERES)
				{ 
					InputHelper.play(this.boo);
					this.state = State.Lost; 
					return; 
				}
			}
			this.nextSphere();
		}
		
		if (InputHelper.isTouched())
		{
			if (InputHelper.touch.y > 185)
			{
				this.cannon.target(InputHelper.touch.x, InputHelper.touch.y);
			}
		}
		if (InputHelper.justTouched())
		{
			if (this.cannon.touched(InputHelper.touch.x, InputHelper.touch.y))
			{
				this.cannon.shoot(this.currentSphere);
			}
		}
		this.cannon.update(delta);
		this.currentSphere.update(spheres, delta);
		this.score.updateTime(delta);
	}
	
	private void lost(float delta)
	{
		if (InputHelper.justTouched())
		{
			Game.i.changeStage(new StageMainMenu());
		}
	}
	
	private void won(float delta)
	{
		if (round < MAX_ROUNDS) 
		{ 
			this.round++; 
			this.nextRound();
			this.score.setRound(this.round);
			this.score.resetTime();
			this.state = State.Playing;
		}
		else
		{
			// end game state
			if (this.round >= MAX_ROUNDS)
			{
				if (InputHelper.justTouched())
				{
					this.round++;
				}
			}
			if (this.round > MAX_ROUNDS + 1)
			{
				if (InputHelper.justTouched())
				{
					if (this.score.points() > Preferences.maxScore())
					{
						Preferences.maxScore(this.score.points());
					}
					Game.i.changeStage(new StageMainMenu());
				}
			}
		}
		
	}
	
	@Override
	public void logic(float delta) 
	{
		if (Gdx.input.isKeyPressed(Keys.BACK)){ Game.i.changeStage(new StageMainMenu()); }
		// update animations
		Iterator<Smoke> it = this.smokes.iterator();
        while(it.hasNext()) { if (it.next().update(delta) == true) { it.remove(); } }
        // update the rest depending on the state
		if (this.state == State.Beginning) 	{ this.beginning(delta); }
		if (this.state == State.Playing) 	{ this.playing(delta); }
		if (this.state == State.Lost) 		{ this.lost(delta); }
		if (this.state == State.Won) 		{ this.won(delta); }
	}

	@Override
	public void draw(SpriteBatch spriteBatch) 
	{
		this.background.draw(spriteBatch);
		this.score.draw(spriteBatch);
		this.deadend.draw(spriteBatch);
		Iterator<Sphere> it1 = this.spheres.iterator();
        while(it1.hasNext()) { it1.next().draw(spriteBatch); }
        Iterator<Smoke> it2 = this.smokes.iterator();
        while(it2.hasNext()) { it2.next().draw(spriteBatch); }
	    cannon.draw(spriteBatch);
	    if (this.currentSphere != null) { this.currentSphere.draw(spriteBatch); }
	    if  (this.nextSphere != null) { this.nextSphere.draw(spriteBatch); }
	    
	    if (this.state == State.Beginning)
	    {
	    	if (this.counter < 2.0f) { this.ready.draw(spriteBatch); }
	    	if (this.counter > 2.0f) { this.go.draw(spriteBatch); }
	    }
	    if (this.state == State.Lost) { this.youlose.draw(spriteBatch); }
	    if (this.state == State.Won) 
	    { 
	    	if (this.round == MAX_ROUNDS)
			{
	    		this.font.draw(spriteBatch, "¡Has Ganado!", 20, Game.GAME_HEIGHT - 150);
	    		this.font.drawMultiLine(spriteBatch, "Puntuación\n" + this.score.points() +
						 "\nRecord\n" + Preferences.maxScore(), 50, Game.GAME_HEIGHT - 300);
			}
	    	if (this.round > MAX_ROUNDS)
			{
	    		if (this.score.points() > Preferences.maxScore())
	    		{
	    			this.font.drawMultiLine(spriteBatch, " ¡Nuevo\n    Record!", 40, Game.GAME_HEIGHT / 2 + 200);
	    		}
	    		else
	    		{
	    			this.font.drawMultiLine(spriteBatch, "  ¡Más suerte\nla próxima vez!", 0, Game.GAME_HEIGHT / 2 + 200);
	    		}
	    	}
	    }
	}
	
	private void newSphere()
	{
		this.nextSphere = new Sphere(this.ingameColor());
		this.nextSphere.setPosition((Game.GAME_WIDTH / 4), 35);
	}
	
	private void nextSphere()
	{
		this.nextSphere.setPosition((Game.GAME_WIDTH / 2) - (this.currentSphere.getWidth() / 2), 65);
		this.currentSphere = this.nextSphere;
		this.newSphere();
	}

	private void nextRound()
	{
		this.cannon.reset();
		this.spheres = this.loader.loadRound(this.round);
		this.currentSphere = new Sphere(this.loader.loadRoundColor(this.round));
		this.currentSphere.setPosition((Game.GAME_WIDTH / 2) - (this.currentSphere.getWidth() / 2), 65);
		this.newSphere();
	}
	
	private Colors ingameColor()
	{
		List<Colors> colors = new ArrayList<Colors>();
		Iterator<Sphere> it = this.spheres.iterator();
		while(it.hasNext())
		{
			Sphere s = it.next();
			if (!colors.contains(s.color()))
			{
				colors.add(s.color());
			}
		}
		return colors.get(MathUtils.random(colors.size()-1));
	}
	
	private void smokeOnDestroyedSpheres(List<Sphere> spheres)
	{
		Iterator<Sphere> it = spheres.iterator();
		while(it.hasNext())
		{
			Sphere s = it.next();
			Smoke smoke = new Smoke(s.getX(), s.getY());
			this.smokes.add(smoke);
		}
	}
}
