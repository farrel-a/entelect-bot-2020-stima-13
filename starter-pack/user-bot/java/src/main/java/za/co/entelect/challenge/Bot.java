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

        //Getter info
        //myCar.position.lane = row/y
        //myCar.position.block = column/x

        /*---------------------------------------------------------------------------*/

        //Bot map
        List<Object> blocksFrontStraight;
        List<Object> blocksFrontRight;
        List<Object> blocksFrontLeft;

        List<Object> blocksBackStraight;
        List<Object> blocksBackRight;
        List<Object> blocksBackLeft;

        List<Object> nextBlock;

        //monitor straight
        blocksFrontStraight = getBlocksFront(myCar.position.lane, myCar.position.block,
                gameState, myCar);
        blocksBackStraight = getBlocksBack(myCar.position.lane, myCar.position.block,
                gameState, myCar);
        nextBlock = blocksFrontStraight.subList(0,1);

        //also look left and right
        if (myCar.position.lane == 2 || myCar.position.lane == 3)
        {
            blocksFrontLeft = getBlocksFront(myCar.position.lane-1, myCar.position.block,
                    gameState, myCar);
            blocksFrontRight = getBlocksFront(myCar.position.lane+1, myCar.position.block,
                    gameState, myCar);

            blocksBackLeft = getBlocksBack(myCar.position.lane-1, myCar.position.block,
                    gameState, myCar);
            blocksBackRight = getBlocksBack(myCar.position.lane+1, myCar.position.block,
                    gameState, myCar);
        }

        //also look right
        else if (myCar.position.lane == 1)
        {
            blocksFrontRight = getBlocksFront(myCar.position.lane+1, myCar.position.block,
                    gameState, myCar);
            blocksBackRight = getBlocksBack(myCar.position.lane+1, myCar.position.block,
                    gameState, myCar);
        }

        //also look left
        else if (myCar.position.lane == 4)
        {
            blocksFrontLeft = getBlocksFront(myCar.position.lane-1, myCar.position.block,
                    gameState, myCar);
            blocksBackLeft = getBlocksBack(myCar.position.lane-1, myCar.position.block,
                    gameState, myCar);
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

        //Basic avoidance logic
        if (blocksFrontStraight.contains(Terrain.MUD) || nextBlock.contains(Terrain.WALL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            if (nextBlock.contains(Terrain.MUD) || nextBlock.contains(Terrain.WALL)) {
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

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that are visible
     **/
    private List<Object> getBlocksFront(int i, int j, GameState gameState, Car myCar) {
        // Current Visible Map
        List<Lane[]> map = gameState.lanes;

        // List of Blocks ahead at lane i
        List<Object> blocks = new ArrayList<>();

        // lane at i
        Lane[] laneList = map.get(i - 1);
        int startBlock = map.get(i - 1)[0].position.block;

        /* lanelist is visible map at lane-i, current pos at lanlist doesn't always start from col index 0
         because it can look back */
        int a = max(j-startBlock, 0);

        while (laneList[a] != null || laneList[a].terrain != Terrain.FINISH)
        {
            blocks.add(laneList[a].terrain);
            a++;
        }
        return blocks;
    }

    private List<Object> getBlocksBack(int i, int j, GameState gameState, Car myCar) {
        // Current Visible Map
        List<Lane[]> map = gameState.lanes;

        // List of Blocks ahead at lane i
        List<Object> blocks = new ArrayList<>();

        // lane at i
        Lane[] laneList = map.get(i - 1);
        int startBlock = map.get(i - 1)[0].position.block;

        /* lanelist is visible map at lane-i, current pos at lanlist doesn't always start from col index 0
         because it can look back */
        int a = j-startBlock;

        while (a>=0 && laneList[a] != null)
        {
            blocks.add(laneList[j].terrain;
            a--;
        }
        return blocks;
    }

}
