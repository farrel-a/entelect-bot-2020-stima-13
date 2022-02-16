package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        //Setup Player State
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        List<Lane[]> map = gameState.lanes;

        //Getter info
        //myCar.position.lane = row/y
        //myCar.position.block = column/x

        /*---------------------------------------------------------------------------*/

        //Bot visible map
        Lane[] laneStraight;
        Lane[] laneRight;
        Lane[] laneLeft;

        List<Object> nextBlock;

        //monitor straight
        laneStraight = map.get(myCar.position.lane-1);

        //also look left and right
        if (myCar.position.lane == 2 || myCar.position.lane == 3)
        {
            laneRight = map.get(myCar.position.lane-1 - 1);
            laneLeft = map.get(myCar.position.lane-1 + 1);
        }

        //also look right
        else if (myCar.position.lane == 1)
        {
            laneRight = map.get(myCar.position.lane-1 - 1);
        }

        //also look left
        else if (myCar.position.lane == 4)
        {
            laneLeft = map.get(myCar.position.lane-1 + 1);
        }

        /*------------------------------------------------------------------------------*/

        //Fix first if too damaged to move
        if (myCar.damage >= 5) {
            return FIX;
        }
        //Accelerate first if going to slow
        if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        //Avoidance Logic
        if (checkObjForward(Terrain.MUD, laneStraight, myCar, myCar.speed) ||
            checkObjForward(Terrain.WALL, laneStraight, myCar, myCar.speed))
        {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups))
            {
                return LIZARD;
            }
            else
            {
                int i = random.nextInt(directionList.size());
                return directionList.get(i);
            }
        }

        //Basic improvement logic
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        //Basic aggression logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        return ACCELERATE;
    }

    private Boolean hasPowerUp(PowerUps powerUpCheck, PowerUps[] inventory) {
        for (PowerUps powerUp: inventory) {
            if (powerUp.equals(powerUpCheck)) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkObjForward(Terrain obj, Lane[] lane, Car myCar, int distance)
    {
        //check certain terrain object forward in a distanc
        int startBlock = lane[0].position.block;
        int start = max( (myCar.position.block - startBlock) , 0);
        for (int j = start ; j <= start+distance && j < lane.length && lane[j] != null ; j++)
        {
            if (lane[j].terrain.equals(obj))
            {
                return true;
            }
        }
        return false;
    }

    private Boolean checkObjBackward(Terrain obj, Lane[] lane, Car myCar)
    {
        //check certain terrain object backward
        int start = myCar.position.block;
        for (int j = start ; j >= 0 && lane[j] != null ; j--)
        {
            if (lane[j].terrain.equals(obj))
            {
                return true;
            }
        }
        return false;
    }



//    /**
//     * Returns map of blocks and the objects in the for the current lanes, returns
//     * the amount of blocks that are visible
//     **/
//    private List<Object> getBlocksFront(int i, int j, GameState gameState, Car myCar) {
//        // Current Visible Map
//        List<Lane[]> map = gameState.lanes;
//
//        // List of Blocks ahead at lane i
//        List<Object> blocks = new ArrayList<>();
//
//        // lane at i
//        Lane[] laneList = map.get(i - 1);
//        int startBlock = map.get(i - 1)[0].position.block;
//
//        /* lanelist is visible map at lane-i, current pos at lanlist doesn't always start from col index 0
//         because it can look back */
//        int a = max(j-startBlock, 0);
//
//        while (laneList[a] != null || laneList[a].terrain != Terrain.FINISH)
//        {
//            blocks.add(laneList[a].terrain);
//            a++;
//        }
//        return blocks;
//    }
//
//    private List<Object> getBlocksBack(int i, int j, GameState gameState, Car myCar) {
//        // Current Visible Map
//        List<Lane[]> map = gameState.lanes;
//
//        // List of Blocks ahead at lane i
//        List<Object> blocks = new ArrayList<>();
//
//        // lane at i
//        Lane[] laneList = map.get(i - 1);
//        int startBlock = map.get(i - 1)[0].position.block;
//
//        /* lanelist is visible map at lane-i, current pos at lanlist doesn't always start from col index 0
//         because it can look back */
//        int a = j-startBlock;
//
//        while (a>=0 && laneList[a] != null)
//        {
//            blocks.add(laneList[j].terrain;
//            a--;
//        }
//        return blocks;
//    }

}
