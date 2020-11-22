import example.gui.UpdatableGrid;
import example.threading.ZCThread;

var height = 25;
var width = 25;

var grid = new UpdatableGrid(height, width);
var conwayGrid = new ConwayGrid(height, width);

conwayGrid[4,5] = true;
conwayGrid[5,4] = true;
conwayGrid[5,5] = true;
conwayGrid[5,6] = true;


conwayGrid.updateDisplay(grid);
grid.show("Hello World");

var i = 0;
while(true) {
    println("Generation " + i);
    conwayGrid.updateDisplay(grid);
    grid.update();
    ZCThread.sleep((1).seconds());
    conwayGrid.update();
    i++;
}