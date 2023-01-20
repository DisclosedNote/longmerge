package foss.longmerge.ui.field;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import foss.longmerge.LongMergeGame;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GameCell {

    public static class VisualDragging {
        public boolean status = false;
        public final Vector2 position = Vector2.Zero;
    }

    enum CellType {
        EMPTY,
        BOMB,
        PLATE
    }

    enum HighlightType {
        NONE,
        AVAILABLE,
        MERGED,
        FORBIDDEN
    }

    private CellType type;
    private Color color = Color.valueOf("C29A24");
    static final Color shadowAvailableFrom = new Color(1f,1f,1f,1f);
    static final Color shadowForbiddenFrom = new Color(1f,0f,0f,1f);
    static final Color shadowMergedFrom = new Color(0f,1f,0f,1f);
    private int power = 0;
    private int pos = 0;
    private boolean highlighted = false;
    private final VisualDragging visualDragging = new VisualDragging();
    private final BitmapFont cellFont;
    private final Texture bombTexture;
    private final Texture cellTexture;
    private final GlyphLayout cellTextLayout = new GlyphLayout();
    public GameCell(CellType type, int power, int pos, BitmapFont cellFont) {
        this.type = type;
        this.pos = pos;
        this.cellFont = cellFont;
        bombTexture = LongMergeGame.assetManager.get(LongMergeGame.TEXTURE_BOMB);
        cellTexture = LongMergeGame.assetManager.get(LongMergeGame.TEXTURE_CELL);
        this.setPower(power);
    }

    public GameCell(int pos, BitmapFont cellFont) {
        this(CellType.EMPTY, 0, pos, cellFont);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public VisualDragging getVisualDragging() {
        return visualDragging;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getPower() {
        return power;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public BitmapFont getCellFont() {
        return cellFont;
    }

    public void setPower(int power) {
        this.power = power;

        // Generate color
        switch(type){
            case PLATE:
            case BOMB:
                color = hashColor(5,9,13);
                break;

            case EMPTY:
                color = new Color(194 / 255f, 154 / 255f, 36 / 255f, 1); // #c29a24
                break;
        }

        // Measure text to draw
        cellTextLayout.setText(cellFont, String.valueOf(this.power));
    }

    public Color hashColor(int rOffset, int gOffset, int bOffset){
        try {
            // TODO: optimize
            byte[] bytesOfMessage = String.valueOf(this.power).getBytes(StandardCharsets.UTF_8);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theMD5digest = md.digest(bytesOfMessage);

            int r = theMD5digest[rOffset] + 128;
            int g = theMD5digest[gOffset] + 128;
            int b = theMD5digest[bOffset] + 128;

            return new Color((float)r / 255f, (float)g / 255f, (float)b / 255f, 1f);

        } catch (NoSuchAlgorithmException e) {
            return Color.BLACK;
        }
    }

    public void draw(
        Batch batch,
        float parentAlpha,
        ShapeRenderer shapeRenderer,
        float renderX,
        float renderY,
        int cellSize,
        HighlightType tempHighlight
    ) {

        switch(tempHighlight){
            default:
            case NONE:
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(this.color);
                shapeRenderer.rect(renderX, renderY, cellSize, cellSize);
                shapeRenderer.end();
                break;

            case AVAILABLE:
                this.drawInnerShadow(batch, parentAlpha, shapeRenderer, renderX, renderY, cellSize, GameCell.shadowAvailableFrom);
                break;

            case FORBIDDEN:
                this.drawInnerShadow(batch, parentAlpha, shapeRenderer, renderX, renderY, cellSize, GameCell.shadowForbiddenFrom);
                break;

            case MERGED:
                this.drawInnerShadow(batch, parentAlpha, shapeRenderer, renderX, renderY, cellSize, GameCell.shadowMergedFrom);
                break;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(renderX, renderY, cellSize, cellSize);
        shapeRenderer.end();

        if(this.type == CellType.PLATE){
            batch.begin();
            batch.draw(cellTexture, renderX, renderY, cellSize, cellSize);
            batch.end();
        }

        if(this.type == CellType.BOMB || this.type == CellType.PLATE) {
            batch.begin();

            float textPosX = renderX + cellSize / 2f - cellTextLayout.width / 2;
            float textPosY = renderY + cellSize / 2f + cellTextLayout.height / 2;

            cellFont.setColor(255 - this.color.r, 255 - this.color.g, 255 - this.color.b, 255);
            cellFont.draw(batch, String.valueOf(this.power), textPosX, textPosY);
            batch.end();
        }
//        Gdx.gl.
    }

    private void drawInnerShadow(
        Batch batch,
        float parentAlpha,
        ShapeRenderer shapeRenderer,
        float renderX,
        float renderY,
        int cellSize,
        Color shadowColor
    ){

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(renderX, renderY, cellSize, cellSize / 2f, shadowColor, shadowColor, color, color);
        shapeRenderer.rect(renderX, renderY + cellSize / 2, cellSize, cellSize / 2f, color, color, shadowColor, shadowColor);
//        shapeRenderer.rect(renderX, renderY, cellSize / 2f, cellSize, shadowFrom, color, color, shadowFrom);
        shapeRenderer.end();

    }

    public void dispose() {
        // cellFont will be disposed in GameFiled
    }


}
