package foss.longmerge;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class LongMergeGame extends Game {
//	SpriteBatch batch;
//	Texture img;

	public AssetManager assetManager;

	public InputMultiplexer multiplexer;
	public OrthographicCamera camera;
	public ScreenViewport viewport;
	public Stage stage;

	public BitmapFont font;
	public Skin skin;

	/* Assets */
	public HashMap<String, String> textures = new HashMap<>();
	public static String TEXTURE_BOMB 		= "bomb";
	public static String TEXTURE_CELL 		= "cell";

	/* UI */
	public Table gameTable;
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

		multiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(multiplexer);

		this.loadTextures();
		this.buildScene();

//		batch = new SpriteBatch();
//		img = new Texture("badlogic.jpg");

		this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	protected void loadTextures(){
		textures.put(TEXTURE_BOMB, 			"textures/bomb.png");
		textures.put(TEXTURE_CELL, 			"textures/cell.png");

		for(String tex : textures.values())
			assetManager.load(tex, Texture.class);
	}

	protected void buildScene(){
		gameTable = new Table(skin);
		gameTable.setFillParent(true);
//		gameTable.setDebug(true);
		gameTable.pad(10);

		topPanelTable = new Table(skin);
		topPanelTable.align(Align.left);
//		topPanelTable.setDebug(true);
		topPanelTable.setHeight(32);

		title = new Label("Longmerge", skin, "title");
		title.setAlignment(Align.left);
		score = new Label("Score: 0", skin);
		score.setAlignment(Align.right);

		ImageButton[] els = new ImageButton[]
		{
			new ImageButton(skin, "new-game-button"),
			new ImageButton(skin, "undo-button"),
			new ImageButton(skin, "redo-button")
		};

		for(ImageButton el : els)
			topPanelTable.add(el).width(32).height(32);

		gameTable.add(topPanelTable).colspan(2).fillX();
		gameTable.row();
		gameTable.add().colspan(2).expand().fillX();
		gameTable.row();
		gameTable.add(title).fill().expandX();
		gameTable.add(score).fill().expandX();


		// Add to scene
		stage.addActor(gameTable);
	}


	@Override
	public void render () {
		ScreenUtils.clear(53f / 255f, 53f / 255f, 53f / 255f, 1);

		if(!assetManager.update())
			return;

		super.render();

		/*
		try {
			// Number as degree
			byte[] bytesOfMessage = "10".getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] theMD5digest = md.digest(bytesOfMessage);

			int r = theMD5digest[5] + 128;
			int g = theMD5digest[9] + 128;
			int b = theMD5digest[13] + 128;

			ScreenUtils.clear(r / 255f, g / 255f, b / 255f, 1);

		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
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
		stage.dispose();
		font.dispose();
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
