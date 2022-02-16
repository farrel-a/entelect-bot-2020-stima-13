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
    private static final int SPEED_LEVEL_1 = 3;
    private static final int SPEED_LEVEL_2 = 6;
    private static final int SPEED_LEVEL_3 = 9;
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

        /*---------------------------------------------------------------------------------------------*/
        //BOT STATE SETUP

        //Setup Player State
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        List<Lane[]> map = gameState.lanes;

        //Getter info
        //myCar.position.lane = row/y
        //myCar.position.block = column/x



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
            laneRight = map.get(myCar.position.lane-1 + 1);
            laneLeft = map.get(myCar.position.lane-1 - 1);
        }

        //also look right
        else if (myCar.position.lane == 1)
        {
            laneRight = map.get(myCar.position.lane-1 + 1);
        }

        //also look left
        else if (myCar.position.lane == 4)
        {
            laneLeft = map.get(myCar.position.lane-1 - 1);
        }

        /*---------------------------------------------------------------------------------------------*/

        /*---------------------------------------------------------------------------------------------*/
        // BOT DECISION LOGIC


        boolean haveMud = checkObjForward(Terrain.MUD, laneStraight, myCar, myCar.speed);
        boolean haveWall = checkObjForward(Terrain.WALL, laneStraight, myCar, myCar.speed);
        boolean haveOil = checkObjForward(Terrain.OIL_SPILL, laneStraight,myCar,myCar.speed);

        //Damage Logic
        if (myCar.damage >= 5) {
            return FIX;
        }
        else if (myCar.damage == 3 && myCar.speed > 6 ){
            return FIX;
        }
        else if (myCar.damage == 2 && myCar.speed > 8){
            return FIX;
        }


        //Avoidance Logic
        if (
                checkObjForward(Terrain.MUD, laneStraight, myCar, myCar.speed) ||
                checkObjForward(Terrain.WALL, laneStraight, myCar, myCar.speed)
           )
        {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups))
            {
                return LIZARD;
            }
            else
            {
                // ada damage blom dipertimbangkan :(
                if (myCar.speed == 3 && haveMud){
                    // ini harusnya dicek di kanan/kiri ada wall ato ga, klo ada br accelerate
                    return ACCELERATE;
                }
                // branch ini ttg kapan harus ganti arah
                else if (myCar.speed > 3 && (haveMud || haveOil || haveWall)){
                    // ini harusnya juga di cek kanan/kiri mana yang lebih beneficial
                    if (myCar.position.lane == 1){
                        return TURN_RIGHT;
                    }
                    else if (myCar.position.lane == 4){
                        return TURN_LEFT;
                    }
                    else{
                        int i = random.nextInt(directionList.size());
                        return directionList.get(i);
                    }
                }
                else {
                    return ACCELERATE;
                }
            }
        }

        //Improvement logic
        if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        //Offensive logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        //default logic
        return ACCELERATE;

        /*---------------------------------------------------------------------------------------------*/
    }

    private Boolean hasPowerUp(PowerUps powerUpCheck, PowerUps[] inventory) {
        //check whether powerUpCheck is in inventory
        for (PowerUps powerUp: inventory) {
            if (powerUp.equals(powerUpCheck)) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkObjForward(Terrain obj, Lane[] lane, Car myCar, int distance)
    {
        //check certain terrain object forward in a distance
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
}
