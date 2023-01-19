package foss.longmerge.ui.field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import foss.longmerge.LongMergeGame;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GameField extends Widget {

//    public int convertToPos(int x, int y){
//        return x * fieldWidth + y * fieldHeight;
//    }

//    public Vector2 convertToVector2(int pos){
//        return new Vector2(pos % fieldWidth, pos / fieldHeight);
//    }

    private final int fieldWidth;
    private final int fieldHeight;
    private final int platesCount = 16;
    private final BitmapFont cellFont;
    private GameCell[] field;
    public ShapeRenderer shapeRenderer = new ShapeRenderer();

    public GameCell selected = null;
    public GameCell underCursor = null;

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void regenerateField(){
        selected = null;
        underCursor = null;

        field = new GameCell[fieldHeight * fieldWidth];

        // Generate plates

        for(int i = 0; i < platesCount; i++) {
            field[i] = new GameCell(GameCell.CellType.PLATE, 1, i, cellFont);
        }

        // Generate empty
        for(int i = platesCount; i < fieldHeight * fieldWidth; i++) {
            field[i] = new GameCell(i, cellFont);
        }

        // Random shuffle
        Random rnd = ThreadLocalRandom.current();
        for (int i = field.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            field[index].setPos(i);
            field[i].setPos(index);
            GameCell a = field[index];
            field[index] = field[i];
            field[i] = a;
        }
    }

    public GameField(BitmapFont cellFont) {
        this(cellFont, 16, 16);
    }

    public GameField(BitmapFont cellFont, int fieldWidth, int fieldHeight) {
        this.cellFont = cellFont;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;

        // Listeners
        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameField.this.clicked(event,x,y);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button) && GameField.this.touchDown(event,x,y,pointer,button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                GameField.this.touchDragged(event,x,y,pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                GameField.this.touchUp(event,x,y,pointer,button);
            }
        });

        this.regenerateField();
    }

    public int getCellSize(){
        return (int) (Math.min(getWidth(), getHeight()) / (int)Math.max(fieldWidth, fieldHeight));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.end();

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;

        int cellSize = getCellSize();

        float startX = centerX - (cellSize * fieldWidth) / 2f;
        float startY = centerY - (cellSize * fieldHeight) / 2f;

        //        underCursor.setHighlighted(!underCursor.isHighlighted());

//        selected.setHighlighted(true);
//        underCursor.setHighlighted(true);

        int selectedX = 0, selectedY = 0, underCursorX = 0, underCursorY = 0;

        if(selected != null && underCursor != null) {
            selectedX = selected.getPos() / platesCount;
            selectedY = selected.getPos() % platesCount;
            underCursorX = underCursor.getPos() / platesCount;
            underCursorY = underCursor.getPos() % platesCount;
        }

        for(int i = 0; i < fieldWidth * fieldHeight; i++) {
            if(field[i].getVisualDragging().status) continue;
//                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            int cellX = (i / fieldWidth);
            int cellY = (i % fieldWidth);
            float renderX = startX + cellX * cellSize;
            float renderY = startY + cellY * cellSize;

            boolean highlight = false;

            if(selected != null && underCursor != null) {
                if (selectedX != underCursorX && selectedY == underCursorY && cellY == selectedY) {
                    int from = Math.min(selectedX, underCursorX);
                    int to = Math.max(selectedX, underCursorX);
                    if (cellX >= from && cellX <= to)highlight = true;
                } else if (selectedY != underCursorY && selectedX == underCursorX && cellX == selectedX) {
                    int from = Math.min(selectedY, underCursorY);
                    int to = Math.max(selectedY, underCursorY);
                    if (cellY >= from && cellY <= to) highlight = true;
                }
            }

            field[i].draw(
                batch,
                parentAlpha,
                shapeRenderer,
                renderX,
                renderY,
                cellSize,
                highlight
            );
        }

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
//        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.rect(startX, startY, cellSize * fieldWidth, cellSize * fieldHeight);
        shapeRenderer.end();

        // Selected cell
        if(selected != null){
            GameCell.VisualDragging dragging = selected.getVisualDragging();
            if(dragging.status){
                selected.draw(
                    batch,
                    parentAlpha,
                    shapeRenderer,
                    dragging.position.x - cellSize / 2f,
                    dragging.position.y - cellSize / 2f,
                    cellSize,
                    false
                );
            }
        }


        batch.begin();
    }

    public void dispose() {
        for(int i = 0; i < fieldHeight * fieldWidth; i++) {
            if(field[i] != null)
                field[i].dispose();
        }
        cellFont.dispose();
        shapeRenderer.dispose();
    }

    /* Tools */

    private GameCell getCellUnderCursor(float stageX, float stageY){
        int cellSize = getCellSize();

        float selPosX = stageX - (getWidth() - cellSize * platesCount) / 2f;
        float selPosY = stageY - (getHeight() - cellSize * platesCount) / 2f;

        if(
            selPosX < 0 || selPosY < 0 ||
            selPosY >= platesCount * cellSize ||
            selPosX >= platesCount * cellSize
        )
            return null;

        int cellX = (int)selPosX / cellSize;
        int cellY = (int)selPosY / cellSize;

        return this.field[cellX * platesCount + cellY];
    }

    /* Input event listeners */
    private void clicked(InputEvent event, float x, float y) {

    }
    private boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//        if(selected != null && selected.getVisualDragging().status) return true;

        // x,y = screen cords
        // event.getStageX(), event.getStageY() = stage cords

        GameCell testSelected = getCellUnderCursor(x, y);
//        selected = getCellUnderCursor(event.getStageX(), event.getStageY());
        if(testSelected == null) return true;
        if(testSelected.getType() != GameCell.CellType.PLATE) return true;
        GameCell.VisualDragging drag = testSelected.getVisualDragging();
        drag.status = true;
        drag.position.x = event.getStageX(); //Gdx.input.getX();
        drag.position.y = event.getStageY(); //Gdx.input.getY();

        this.selected = testSelected;
        return true;
    }

    private void touchDragged(InputEvent event, float x, float y, int pointer) {
        if(selected == null) return;
        GameCell.VisualDragging drag = selected.getVisualDragging();
        if(!drag.status) return;

        // x,y = screen cords
        // event.getStageX(), event.getStageY() = stage cords

        drag.position.x = event.getStageX();
        drag.position.y = event.getStageY();

        this.underCursor = getCellUnderCursor(x, y);
//        if(underCursor == null) return;

//        this.underCursor = underCursor;
    }

    private void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if(selected == null) return;
        selected.getVisualDragging().status = false;
        if(underCursor == null) return;

        if(selected == underCursor) {
            selected = null;
            underCursor = null;
            return;
        }

        if(
            selected.getType() == GameCell.CellType.PLATE &&
            selected.getType() == underCursor.getType() &&
            selected.getPower() != underCursor.getPower()
        ) {
            selected = null;
            underCursor = null;
            return;
        }

        if(
            underCursor.getType() == GameCell.CellType.BOMB &&
            selected.getPower() < underCursor.getPower()
        ) {
            selected = null;
            underCursor = null;
            return;
        }

        int selectedX = selected.getPos() / platesCount;
        int selectedY = selected.getPos() % platesCount;
        int underCursorX = underCursor.getPos() / platesCount;
        int underCursorY = underCursor.getPos() % platesCount;

        int underCursorPos = underCursor.getPos();

        int selectedPos = selected.getPos();
        int selectedPower = selected.getPower();

        if (selectedX != underCursorX && selectedY == underCursorY) {
            int from = Math.min(selectedX, underCursorX);
            int to = Math.max(selectedX, underCursorX);
            for(int check = from; check <= to; check++){
                int pos = check * platesCount + selectedY;
                this.field[pos] = new GameCell(GameCell.CellType.BOMB, selectedPower, pos, cellFont);
            }
        } else if (selectedY != underCursorY && selectedX == underCursorX) {
            int from = Math.min(selectedY, underCursorY);
            int to = Math.max(selectedY, underCursorY);
            for(int check = from; check <= to; check++){
                int pos = selectedX * platesCount + check;
                this.field[pos] = new GameCell(GameCell.CellType.BOMB, selectedPower, pos, cellFont);
            }
        }

        if(this.underCursor.getType() == GameCell.CellType.EMPTY){
            this.field[underCursorPos] = new GameCell(GameCell.CellType.PLATE, selectedPower, underCursorPos, cellFont);
        } else if (this.underCursor.getType() == GameCell.CellType.PLATE){
            this.field[underCursorPos] = new GameCell(GameCell.CellType.PLATE, selectedPower + 1, underCursorPos, cellFont);
        }

        this.underCursor = null;
        this.selected = null;

//        selected = null;
    }

}
