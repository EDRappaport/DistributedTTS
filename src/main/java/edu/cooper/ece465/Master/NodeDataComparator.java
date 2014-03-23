package edu.cooper.ece465.Master;

import java.util.Comparator;

public class NodeDataComparator implements Comparator<NodeData> {

    @Override
    public int compare(NodeData x, NodeData y){
        if (x.getScore() < y.getScore()) {
            return -1;
        }
        else if (x.getScore() > y.getScore()) {
            return 1;
        }
        return 0;
    }
}
