package foss.longmerge.ui.field;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GameField extends Widget {
    private final int fieldSide;
    private final int platesCount = 8;
    private final BitmapFont cellFont;
    private GameCell[] field;
    public ShapeRenderer shapeRenderer = new ShapeRenderer();

    public GameCell selected = null;
    public GameCell underCursor = null;

    public int getFieldSide() {
        return fieldSide;
    }

    public void regenerateField(){
        selected = null;
        underCursor = null;

        field = new GameCell[fieldSide * fieldSide];

        // Generate plates
        for(int i = 0; i < platesCount; i++) {
            field[i] = new GameCell(GameCell.CellType.PLATE, 1, i, cellFont);
        }

        // Generate empty
        for(int i = platesCount; i < fieldSide * fieldSide; i++) {
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
        this(cellFont, 8);
    }

    public GameField(BitmapFont cellFont, int fieldSide) {
        this.cellFont = cellFont;
        this.fieldSide = fieldSide;

        // Listeners
        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameField.this.clicked(event,x,y);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return
                    super.touchDown(event, x, y, pointer, button) &&
                    GameField.this.touchDown(event,x,y,pointer,button);
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
        return (int) (Math.min(getWidth(), getHeight()) / fieldSide);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.end();

        // using primitives here to get maximum rendering performance
        int cellSize = getCellSize();
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float startX = centerX - (cellSize * fieldSide) / 2f;
        float startY = centerY - (cellSize * fieldSide) / 2f;

        GameCell.CellType selectedCellType = GameCell.CellType.PLATE;
        int selectedX = 0;
        int selectedY = 0;
        int selectedPower = -1;
        int underCursorX = 0;
        int underCursorY = 0;
//        int pathMergingPlateCellsVisited = 0;

        if(selected != null && underCursor != null) {
            selectedX = selected.getPos() / getFieldSide();
            selectedY = selected.getPos() % getFieldSide();
            selectedPower = selected.getPower();
            underCursorX = underCursor.getPos() / getFieldSide();
            underCursorY = underCursor.getPos() % getFieldSide();
        }

        for(int i = 0; i < fieldSide * fieldSide; i++) {
            GameCell cell = field[i];
            if(cell.getVisualDragging().status) continue;

            GameCell.CellType cellType = cell.getType();
            int cellPower = cell.getPower();
            int cellX = (i / fieldSide);
            int cellY = (i % fieldSide);
            float renderX = startX + cellX * cellSize;
            float renderY = startY + cellY * cellSize;

            GameCell.HighlightType highlight = drawGetHighlightStatus(
                selectedX, selectedY,
                selectedCellType, selectedPower,
                underCursorX, underCursorY,
                cellX, cellY,
                cellType,
                cellPower
            );

            cell.draw(
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
        shapeRenderer.rect(startX, startY, cellSize * fieldSide, cellSize * fieldSide);
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
                    GameCell.HighlightType.NONE
                );
            }
        }

        batch.begin();
    }

    private GameCell.HighlightType drawGetHighlightStatus(
        int selectedX, int selectedY,
        GameCell.CellType selectedCellType,
        int selectedPower,
        int underCursorX, int underCursorY,
        int cellX, int cellY,
        GameCell.CellType cellType,
        int cellPower
    ){
        // If nothing is selected
        if(selected == null || underCursor == null)
            return GameCell.HighlightType.NONE;

        int from = -1;
        int to = -1;
        int selectedCord = 0;

        // get path from & to positions, path direction
        if (selectedX != underCursorX && selectedY == underCursorY && cellY == selectedY) {
            from = Math.min(selectedX, underCursorX);
            to = Math.max(selectedX, underCursorX);
            selectedCord = cellX;
        } else if (selectedY != underCursorY && selectedX == underCursorX && cellX == selectedX) {
            from = Math.min(selectedY, underCursorY);
            to = Math.max(selectedY, underCursorY);
            selectedCord = cellY;
        }

        // if not within path
        if(!(selectedCord >= from && selectedCord <= to))
            return GameCell.HighlightType.NONE;

        // if current cell can be merged with the selected one
        if(selectedCellType == cellType && selectedPower == cellPower)
            return GameCell.HighlightType.MERGED;


        // check whether the move is available
        final boolean checkPlates = (
            cellType == GameCell.CellType.PLATE &&
            cellPower == selectedPower
        );
        final boolean checkBombs = (
            cellType == GameCell.CellType.BOMB &&
            cellPower < selectedPower
        );
        final boolean checkEmpty = (
            cellType == GameCell.CellType.EMPTY
        );

        // Available if movable
        if(checkPlates || checkBombs || checkEmpty)
            return GameCell.HighlightType.AVAILABLE;

        // Forbidden for any other cases
        return GameCell.HighlightType.FORBIDDEN;
    }

    public void dispose() {
        for(int i = 0; i < fieldSide * fieldSide; i++) {
            if(field[i] != null)
                field[i].dispose();
        }
        cellFont.dispose();
        shapeRenderer.dispose();
    }

    /* Tools */

    private GameCell getCellUnderCursor(float stageX, float stageY){
        int cellSize = getCellSize();

        float selPosX = stageX - (getWidth() - cellSize * getFieldSide()) / 2f;
        float selPosY = stageY - (getHeight() - cellSize * getFieldSide()) / 2f;

        if(
            selPosX < 0 || selPosY < 0 ||
            selPosY >= getFieldSide() * cellSize ||
            selPosX >= getFieldSide() * cellSize
        )
            return null;

        int cellX = (int)selPosX / cellSize;
        int cellY = (int)selPosY / cellSize;

        return this.field[cellX * getFieldSide() + cellY];
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

        this.doMove();

        this.underCursor = null;
        this.selected = null;
    }

    private void doMove() {
        if (selected == underCursor)
            return;

        if (
            selected.getType() == GameCell.CellType.PLATE &&
            selected.getType() == underCursor.getType() &&
            selected.getPower() != underCursor.getPower()
        ) return;

        if (
            underCursor.getType() == GameCell.CellType.BOMB &&
            selected.getPower() < underCursor.getPower()
        ) return;

        final int fieldSide = getFieldSide();
        int selectedX = selected.getPos() / fieldSide;
        int selectedY = selected.getPos() % fieldSide;
        int underCursorX = underCursor.getPos() / fieldSide;
        int underCursorY = underCursor.getPos() % fieldSide;

        int underCursorPos = underCursor.getPos();

        int selectedPos = selected.getPos();
        int selectedPower = selected.getPower();

        if (selectedX != underCursorX && selectedY == underCursorY) {
            int from = Math.min(selectedX, underCursorX);
            int to = Math.max(selectedX, underCursorX);

            // pre check
            for(int check = from; check <= to; check++){
                int pos = check * fieldSide + selectedY;
                if(pos == selectedPos) continue;

                GameCell.CellType type = this.field[pos].getType();
                int power = this.field[pos].getPower();
                if(
                    (type == GameCell.CellType.PLATE && pos != underCursorPos) ||
                    (type == GameCell.CellType.BOMB && power >= selectedPower)
                ) return;
            }

            // post check
            for(int check = from; check <= to; check++){
                int pos = check * fieldSide + selectedY;
                this.field[pos] = new GameCell(GameCell.CellType.BOMB, selectedPower, pos, cellFont);
            }
        } else if (selectedY != underCursorY && selectedX == underCursorX) {
            int from = Math.min(selectedY, underCursorY);
            int to = Math.max(selectedY, underCursorY);

            // pre check
            for(int check = from; check <= to; check++){
                int pos = selectedX * fieldSide + check;
                if(pos == selectedPos) continue;

                GameCell.CellType type = this.field[pos].getType();
                int power = this.field[pos].getPower();
                if(
                    (type == GameCell.CellType.PLATE && pos != underCursorPos) ||
                    (type == GameCell.CellType.BOMB && power >= selectedPower)
                ) return;
            }

            // post check
            for(int check = from; check <= to; check++){
                int pos = selectedX * fieldSide + check;
                this.field[pos] = new GameCell(GameCell.CellType.BOMB, selectedPower, pos, cellFont);
            }
        } else return;

        if (this.underCursor.getType() == GameCell.CellType.PLATE){
            this.field[underCursorPos] =
                new GameCell(GameCell.CellType.PLATE, selectedPower + 1, underCursorPos, cellFont);
        } else {
            this.field[underCursorPos] =
                new GameCell(GameCell.CellType.PLATE, selectedPower, underCursorPos, cellFont);
        }
    }

}
