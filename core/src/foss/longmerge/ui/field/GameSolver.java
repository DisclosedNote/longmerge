package foss.longmerge.ui.field;

public class GameSolver {

    public interface GameSolverListener {
        public void solved(Result status);
    }

    public enum Result{
        UNKNOWN,
        SOLVABLE,
        PARTLY_SOLVABLE,
        GAME_OVER
    }
    private Result status = Result.UNKNOWN;
    private GameSolverListener listener = new GameSolverListener(){
        @Override
        public void solved(Result status) { }
    };

    public void reset(){
        status = Result.SOLVABLE;
        listener.solved(status);
    }

    public void solve(GameCell[] field, int fieldSide){

        Result result = Result.UNKNOWN;

        int total = 0;
        int stuck = 0;

        for(int i = 0; i < field.length; i++){
            GameCell cur = field[i];
            if(cur.getType() != GameCell.CellType.PLATE) continue;
            total++;

            int x = i / fieldSide;
            int y = i % fieldSide;

            int trails = 0;
            if(!cellStatus(cur, x + 1, y, fieldSide, field)) trails++;
            if(!cellStatus(cur, x, y + 1, fieldSide, field)) trails++;
            if(!cellStatus(cur, x - 1, y, fieldSide, field)) trails++;
            if(!cellStatus(cur, x, y - 1, fieldSide, field)) trails++;

            if(trails == 4) stuck++;
        }

        if(stuck >= total)
            result = Result.GAME_OVER;
        else if(stuck > 0)
            result = Result.PARTLY_SOLVABLE;
        else
            result = Result.SOLVABLE;

        listener.solved(result);
    }

    private boolean cellStatus(GameCell curCell, int x, int y, int fieldSide, GameCell[] field){
        int pos = x * fieldSide + y;
        if(pos < 0 || pos >= field.length || x < 0 || x >= fieldSide || y < 0 || y >= fieldSide)
            return false;

        GameCell cell = field[pos];
//        GameCell curCell = field[cur];

        if(cell.getType() == GameCell.CellType.TRAIL && cell.getPower() >= curCell.getPower())
            return false;

        if(cell.getType() == GameCell.CellType.PLATE && cell.getPower() != curCell.getPower())
            return false;

        return true;
    }

    public Result getStatus() {
        return status;
    }

    public void setListener(GameSolverListener listener) {
        this.listener = listener;
    }
}
