# game-of-life
A Java implementation of Game of Life

---

## Rules
This application tries to reproduce John Conway's Game of Life : [Wikipedia](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)

Rules to determine cell state are as follows :
- Any live cell with fewer than 2 live neighbours dies, as if by underpopulation.
- Any live cell with 2 or 3 live neighbours lives on to the next generation.
- Any live cell with more than 3 live neighbours dies, as if by overpopulation.
- Any dead cell with exactly 3 live neighbours becomes a live cell, as if by reproduction.
