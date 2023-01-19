package foss.longmerge;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import foss.longmerge.ui.field.GameField;

import java.util.HashMap;

public class LongMergeGame extends Game {
//	SpriteBatch batch;
//	Texture img;

	public static AssetManager assetManager;

	public InputMultiplexer multiplexer;
	public OrthographicCamera camera;
	public ScreenViewport viewport;
	public Stage stage;

	public static Skin skin;

	/* Assets */
	public static BitmapFont cellFont;
	public static String TEXTURE_BOMB 		= "textures/bomb.png";
	public static String TEXTURE_CELL 		= "textures/cell.png";

	/* UI */
	public boolean triggeredToBuildUI = false;
	public GameField gameField;
	public Table uiTable;
	public Table topPanelTable;
	public Label score;
	public Label title;

	@Override
	public void create () {
		multiplexer 			= new InputMultiplexer();
		camera 					= new OrthographicCamera();
		viewport 				= new ScreenViewport(camera);
		stage 					= new Stage(viewport);
		assetManager 			= new AssetManager();
		skin 					= new Skin(Gdx.files.internal("skin/default-skin.json"));
		cellFont				= new BitmapFont(Gdx.files.internal("skin/longmerge-cell.fnt"), false);
//		cellFont				= new BitmapFont();

		multiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(multiplexer);

		this.loadTextures();

//		batch = new SpriteBatch();
//		img = new Texture("badlogic.jpg");

		this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private void loadTextures(){
		assetManager.load(TEXTURE_BOMB, Texture.class);
		assetManager.load(TEXTURE_CELL, Texture.class);
	}

	private void buildScene(){
		uiTable = new Table(skin);
		uiTable.setFillParent(true);
//		uiTable.setDebug(true);
		uiTable.pad(10);

		topPanelTable = new Table(skin);
		topPanelTable.align(Align.left);
//		topPanelTable.setDebug(true);
		topPanelTable.setHeight(32);

		gameField = new GameField(cellFont);
		title = new Label("Longmerge", skin, "title");
		title.setAlignment(Align.left);
		score = new Label("Score: 0", skin);
		score.setAlignment(Align.right);

		ImageButton newGameButton = new ImageButton(skin, "new-game-button");
		ImageButton undoButton = new ImageButton(skin, "undo-button");
		ImageButton redoButton = new ImageButton(skin, "redo-button");

		ImageButton[] els = new ImageButton[]
		{
			newGameButton,
			undoButton,
			redoButton
		};

		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				LongMergeGame.this.gameField.regenerateField();
			}
		});


		for(ImageButton el : els)
			topPanelTable.add(el).width(32).height(32);

		uiTable.add(topPanelTable).colspan(2).fillX();
		uiTable.row();
		uiTable.add(gameField).colspan(2).expand().fill();
		uiTable.row();
		uiTable.add(title).fill().expandX();
		uiTable.add(score).fill().expandX();

		// Add to scene
		stage.addActor(uiTable);
	}


	@Override
	public void render () {
		ScreenUtils.clear(53f / 255f, 53f / 255f, 53f / 255f, 1);

		if(!assetManager.update()){
			ScreenUtils.clear(assetManager.getProgress() / 255f, assetManager.getProgress() / 255f, assetManager.getProgress() / 255f, 1);
			return;
		}

		if(!triggeredToBuildUI){
			this.buildScene();
			triggeredToBuildUI = true;
		}

		super.render();

		/*

		*/

//		camera.update();
//		handleInput();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

//		batch.setProjectionMatrix(camera.combined);

//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
	}

	private void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			camera.zoom += 0.02;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
			camera.zoom -= 0.02;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			camera.translate(-3, 0, 0);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			camera.translate(3, 0, 0);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			camera.translate(0, -3, 0);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			camera.translate(0, 3, 0);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			camera.rotate(-0.5f, 0, 0, 1);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.E)) {
			camera.rotate(0.5f, 0, 0, 1);
		}

//		camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100/camera.viewportWidth);

		float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
		float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

//		camera.position.x = 0;
//		camera.position.y = 0;

//		camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
//		camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);
	}

	@Override
	public void dispose () {
		super.dispose();
//		batch.dispose();
//		img.dispose();
		gameField.dispose();
		stage.dispose();
		skin.dispose();
		assetManager.dispose();
	}

	@Override
	public void resize(int width, int height) {
//		camera.viewportWidth = width;
//		camera.viewportHeight = height;
//		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
//		camera.update();
		viewport.update(width, height);
		stage.getViewport().update(width, height, true);
	}

}
