package com.codenjoy.dojo.snake.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.*;


import java.util.*;


/**
 * User: Maksim.L
 */
public class YourSolver implements Solver<Board> {

  private Dice dice;
  private static Board board;

  public YourSolver(Dice dice) {
    this.dice = dice;
  }

  @Override
  public String get(Board board) {
    this.board = board;
    System.out.println(board.toString());
    System.out.println("Snake size: " + board.getSnake().size());

    Point apple = board.getApples().get(0);
    Point head = board.getHead();
    Point stone = board.getStones().get(0);
    List<Point> walls = board.getWalls();
    List<Point> snake = board.getSnake();
    String right = Direction.RIGHT.toString();
    String left = Direction.RIGHT.toString();
    String up = Direction.RIGHT.toString();
    String down = Direction.RIGHT.toString();

    if (head == null) {
      return Direction.UP.toString();
    } else {
      head = board.getHead();

    }
//СОздание графа с точками и препятствиями
    Graph graph = new Graph(board.size() * board.size());
    createGraph(graph);

    graph.addBarriers((board.size() * stone.getY()) + stone.getX());
    walls.stream().forEach(wall -> {
      graph.addBarriers((board.size() * wall.getY()) + wall.getX());
    });
    snake.stream().forEach(snakes -> {
      graph.addBarriers((board.size() * snakes.getY()) + snakes.getX());
    });


    int headNode = (board.size() * head.getY()) + head.getX();
    int dest = (board.size() * apple.getY()) + apple.getX();
    ;
    List<Integer> res = graph.BFS(headNode, dest);
    LinkedList<Point> path = new LinkedList<>();
    if (!res.isEmpty()) {
      for (int i = 0; i < res.size(); i++) {
        Point p = intToPointConvert(res.get(i));
        path.add(p);
      }
    }

    if (path.size() > 0) {
      Point nextPoint = path.poll();
      return moveToPoint(head, nextPoint);
    } else {
      boolean avoidStone = false;
      boolean[] dirs = ignoreBarriers(false);
      if (dirs[1]) {
        return right;
      } else if (dirs[0]) {
        return up;
      } else if (dirs[2]) {
        return down;
      } else if (dirs[3]) {
        return left;
      } else {
        avoidStone = true;
        dirs = ignoreBarriers(true);
        if (dirs[1]) {
          return right;
        } else if (dirs[0]) {
          return up;
        } else if (dirs[2]) {
          return down;
        } else if (dirs[3]) {
          return left;
        } else {
          return up;
        }
      }
    }
  }

  //Общее направление в зависимости от положения
  private String moveToPoint(Point source, Point target) {
    if (target.getY() < source.getY()) {
      return Direction.DOWN.toString();
    } else if (target.getY() > source.getY()) {
      return Direction.UP.toString();
    } else if (target.getX() < source.getX()) {
      return Direction.LEFT.toString();
    } else if (target.getX() > source.getX()) {
      return Direction.RIGHT.toString();
    } else {
      return Direction.DOWN.toString();
    }
  }

  //Инт в поинт
  private Point intToPointConvert(int nodeNum) {
    int intX = nodeNum % board.size();
    int intY = (nodeNum - intX) / board.size();
    return new PointImpl(intX, intY);
  }

  //Граф и дуги
  private void createGraph(Graph g) {
    int node = 0;
    for (int x = 1; x < board.size(); x++) {
      for (int y = 1; y < board.size(); y++) {
        node += 1;
        if (y == board.size() - 1 && x == board.size() - 1) {
          g.addEdge(node, node - 1);
          g.addEdge(node, node - board.size());
        } else if (x == board.size() - 1) {
          g.addEdge(node, node + 1);
          g.addEdge(node, node - board.size());
          g.addEdge(node, node - 1);
        } else if (y == board.size() - 1) {
          g.addEdge(node, node - 1);
          g.addEdge(node, node - board.size());
          g.addEdge(node, node + board.size());
        } else {
          g.addEdge(node, node + 1);
          g.addEdge(node, node + board.size());
          g.addEdge(node, node - board.size());
          g.addEdge(node, node - 1);
        }
      }
    }
  }

  private boolean isPossible(boolean avoidStone, Point stone, List<Point> walls, List<Point> snake, Point pointToMove) {
    if (pointToMove.itsMe(stone) && !avoidStone) {
      return false;
    }
    for (int i = 0; i < walls.size(); i++) {
      if (pointToMove.itsMe(walls.get(i))) {
        return false;
      }
    }
    for (int i = 0; i < snake.size(); i++) {
      if (pointToMove.itsMe(snake.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean[] ignoreBarriers(boolean avoidStone) {
    Point head = board.getHead();
    Point stone = board.getStones().get(0);
    List<Point> walls = board.getBarriers();
    List<Point> snake = board.getSnake();
    boolean up;
    boolean down;
    boolean right;
    boolean left;
    //Массив результатов
    boolean[] movements = new boolean[4];
//Возможность идти наверх
    head.move(head.getX(), head.getY() + 1);
    up = isPossible(avoidStone, stone, walls, snake, head);
//Возможность идти вниз
    head.move(head.getX(), head.getY() - 1);
    down = isPossible(avoidStone, stone, walls, snake, head);
//Возможность идти вправо
    head.move(head.getX() + 1, head.getY());
    right = isPossible(avoidStone, stone, walls, snake, head);
//Возможность идти влево
    head.move(head.getX() - 1, head.getY());
    left = isPossible(avoidStone, stone, walls, snake, head);
    movements[0] = up;
    movements[1] = right;
    movements[2] = down;
    movements[3] = left;
    return movements;
  }

  public static void main(String[] args) {
    WebSocketRunner.runClient(
            // paste here board page url from browser after registration
            "http://164.90.213.43/codenjoy-contest/board/player/qizdzdu5mlpd34wszibk?code=7110864378654893059",
            new YourSolver(new RandomDice()),
            new Board());
  }
}

class Graph {
  private int V; //количество вершин
  private final LinkedList<Integer>[] adj; //список смежних вершин
  boolean[] visited;

  Graph(int v) {
    V = v;
    visited = new boolean[V];
    adj = new LinkedList[v];
    for (int i = 0; i < v; ++i)
      adj[i] = new LinkedList<>();
  }

  //добавляем дугу графа
  void addEdge(int v, int w) {
    adj[v].add(w);
  }

  //добавляем препятствия
  void addBarriers(int nodeNum) {
    visited[nodeNum] = true;
  }

  // BFS алгоритм
  List<Integer> BFS(int start, int target) {
    //Создаем список
    LinkedList<Integer> queue = new LinkedList<>();
    Map<Integer, Integer> parentNodes = new HashMap<>();
// обозначаем текущий нод посещенным и добавляем в список
    visited[start] = true;
    queue.add(start);

    while (queue.size() != 0) {
      start = queue.poll();
      //смежные вершины
      for (int i = 0; i < adj[start].size(); i++) {
        if (!visited[adj[start].get(i)]) {
          visited[adj[start].get(i)] = true;
          parentNodes.put(adj[start].get(i), start);
          queue.add(adj[start].get(i));
        }
        if (adj[start].get(i) == target) {
          List<Integer> shortestPath = new ArrayList<>();
          Integer node = target;
          while (node != null) {
            shortestPath.add(node);
            node = parentNodes.get(node);
          }
          Collections.reverse(shortestPath);
          shortestPath.remove(0);
          return shortestPath;
        }
      }
    }
    return new ArrayList<>();
  }
}