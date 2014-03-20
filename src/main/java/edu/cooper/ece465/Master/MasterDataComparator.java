package edu.cooper.ece465.Master;

import java.util.Comparator;

public class MasterDataComparator implements Comparator<MasterData> {

    @Override
    public int compare(MasterData x, MasterData y){
        if (x.getScore() < y.getScore()) {
            return -1;
        }
        else if (x.getScore() > y.getScore()) {
            return 1;
        }
        return 0;
    }
}
