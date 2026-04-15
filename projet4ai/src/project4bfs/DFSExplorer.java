package project4bfs;

import java.util.*;

public class DFSExplorer {

    public static class SearchResult {   //inner class
        private final boolean found; //Est-ce qu'on a trouvé une sortie ?
        private final List<Node> path;   //Le chemin final (du départ → sortie)
        private final List<Node> exploredOrder;//ordre d'exploration des cases
        private final int exploredStates;  //
        private final int distance; //nombre de pas du chemin
        private final Node reachedExit;  //la sortie atteinte

        public SearchResult(boolean found, List<Node> path, List<Node> exploredOrder,
                            int exploredStates, int distance, Node reachedExit) {
            this.found = found;
            this.path = path;
            this.exploredOrder = exploredOrder;
            this.exploredStates = exploredStates;
            this.distance = distance;
            this.reachedExit = reachedExit;
        }

        public boolean isFound() {
            return found;
        }

        public List<Node> getPath() {
            return path;
        }

        public List<Node> getExploredOrder() {
            return exploredOrder;
        }

        public int getExploredStates() {
            return exploredStates;
        }

        public int getDistance() {
            return distance;
        }

        public Node getReachedExit() {
            return reachedExit;
        }
    }

    private final BuildingMap buildingMap;

    public DFSExplorer(BuildingMap buildingMap) {
        this.buildingMap = buildingMap;
    }

    public SearchResult search() {
        Node start = buildingMap.findStart(); //depart
        List<Node> exits = buildingMap.findExits(); //sortie

        if (start == null || exits.isEmpty()) {
            return new SearchResult(false, Collections.emptyList(), Collections.emptyList(), 0, -1, null);
        }

        Stack<Node> stack = new Stack<>(); // Stack LIFO → base du DFS
        Set<Node> visited = new HashSet<>(); // Pour éviter de revisiter les cases
        Map<Node, Node> parentMap = new HashMap<>(); // Pour reconstruire le chemin
        Map<Node, Integer> distanceMap = new HashMap<>(); // Stocke la distance depuis le départ
        List<Node> exploredOrder = new ArrayList<>(); // Ordre d'exploration (pour affichage)

        stack.push(start);
        visited.add(start);
        distanceMap.put(start, 0);

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            exploredOrder.add(current);

            if (buildingMap.isExit(current.getRow(), current.getCol())) {
                List<Node> path = reconstructPath(current, parentMap);
                return new SearchResult(
                        true,
                        path,
                        exploredOrder,
                        exploredOrder.size(),
                        distanceMap.get(current),
                        current
                );
            }

            for (Node neighbor : buildingMap.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor); // enregistre le neighbor comme visité
                    parentMap.put(neighbor, current); // node, son parent
                    distanceMap.put(neighbor, distanceMap.get(current) + 1); // distance du neighbor = distance du current + 1
                    stack.push(neighbor);
                    /*Si pas encore visitée :
                        on la marque visitée
                        on enregistre son parent
                        on calcule la distance
                        on l'ajoute à la stack*/
                }
            }
        }

        return new SearchResult(false, Collections.emptyList(), exploredOrder, exploredOrder.size(), -1, null);
    }

    private List<Node> reconstructPath(Node goal, Map<Node, Node> parentMap) {
        List<Node> path = new ArrayList<>();
        Node current = goal; // quand cette case est une sortie elle devient le goal

        while (current != null) { // quand on retourne au start le parent sera null
            path.add(current);
            current = parentMap.get(current); // Ici, on remplace current par son parent.
        } // E → C → B → A → S

        Collections.reverse(path); // On inverse le chemin pour avoir l'ordre du départ à la sortie S → A → B → C → E
        return path;
    }
}

