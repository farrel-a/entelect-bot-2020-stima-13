package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int MUD_DAMAGE = 1;
    private static final int OIL_DAMAGE = 1;
    private static final int WALL_DAMAGE = 2;
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
        int x = myCar.position.block;
        int y = myCar.position.lane;

        //Getter info
        //myCar.position.lane = row/y
        //myCar.position.block = column/x



        //Bot visible map
        Lane[] laneStraight;
        Lane[] laneRight = new Lane[1];
        Lane[] laneLeft = new Lane[1];
        List<Object> nextBlock;

        //monitor straight
        laneStraight = map.get(myCar.position.lane-1);

        //also look left and right
        if (y == 2 || y == 3)
        {
            laneRight = map.get(y-1 + 1);
            laneLeft = map.get(y-1 - 1);
        }

        //also look right
        else if (y == 1)
        {
            laneRight = map.get(y-1 + 1);
        }

        //also look left
        else if (myCar.position.lane == 4)
        {
            laneLeft = map.get(y-1 - 1);
        }

        /*---------------------------------------------------------------------------------------------*/

        /*---------------------------------------------------------------------------------------------*/
        // BOT DECISION LOGIC


        boolean haveMud = checkObjForward(Terrain.MUD, laneStraight, x, myCar.speed);
        boolean haveWall = checkObjForward(Terrain.WALL, laneStraight, x, myCar.speed);
        boolean haveOil = checkObjForward(Terrain.OIL_SPILL, laneStraight,x,myCar.speed);

        //Damage Logic
        if (myCar.damage >= 5) {
            return FIX;
        }
        else if (myCar.damage == 4 && myCar.speed > 6 ){
            return FIX;
        }
        else if (myCar.damage == 3 && myCar.speed > 8){
            return FIX;
        }
        if (available(PowerUps.BOOST, myCar.powerups) && (!haveMud && !haveWall && !haveOil)) {
            return BOOST;
        }

        //Avoidance Logic
        if (haveMud || haveWall || haveOil)
        {
            if (available(PowerUps.LIZARD, myCar.powerups))
            {
                return LIZARD;
            }
            else
            {
                if (myCar.speed >= 3)
                {
                    if (y==1 && laneObstacleClear(laneRight, x, myCar.speed))
                    {
                        return TURN_RIGHT;
                    }
                    else if (y==4 && laneObstacleClear(laneLeft, x,myCar.speed))
                    {
                        return TURN_LEFT;
                    }
                    else if (y==2 || y==3)
                    {
                        if (laneObstacleClear(laneLeft,x,myCar.speed) && laneHasPowerUp(laneLeft, x,myCar.speed))
                        {
                            return TURN_LEFT;
                        }
                        else if (laneObstacleClear(laneRight,x,myCar.speed) &&
                                laneHasPowerUp(laneRight,x,myCar.speed))
                        {
                            return TURN_RIGHT;
                        }
                        else if (laneObstacleClear(laneLeft,x,myCar.speed))
                        {
                            return TURN_LEFT;
                        }
                        else if (laneObstacleClear(laneRight,x,myCar.speed))
                        {
                            return TURN_RIGHT;
                        }
                    }
                }

                else
                {
                    if (haveMud || haveOil && !haveWall)
                    {
                        if (myCar.damage + MUD_DAMAGE < 5) {
                            return ACCELERATE;
                        }
                    }
                    else if (!(haveMud || haveOil) && haveWall)
                    {
                        if (myCar.damage + WALL_DAMAGE < 5) {
                            return ACCELERATE;
                        }
                    }
                    else if ((haveMud||haveOil) && haveWall)
                    {
                        if (myCar.damage + WALL_DAMAGE + OIL_DAMAGE < 5)
                        {
                            return ACCELERATE;
                        }
                    }
                    return FIX;
                }
            }
        }

        if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        //Improvement logic coba di improve lagi wkwkwk
        //Improvement logic

        //Offensive logic
        if (myCar.speed == maxSpeed) {
            if (available(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (available(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        //default logic
        return ACCELERATE;

        /*---------------------------------------------------------------------------------------------*/
    }

    private Boolean available(PowerUps powerUpCheck, PowerUps[] inventory) {
        //check whether powerUpCheck is in inventory
        for (PowerUps powerUp: inventory) {
            if (powerUp.equals(powerUpCheck)) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkObjForward(Terrain obj, Lane[] lane, int x, int distance)
    {
        //check certain terrain object forward in a distance
        int startBlock = lane[0].position.block;
        int start = max( (x - startBlock) , 0);
        for (int j = start ; j <= start+distance && j < lane.length && lane[j] != null ; j++)
        {
            if (lane[j].terrain.equals(obj))
            {
                return true;
            }
        }
        return false;
    }

    private Boolean checkObjBackward(Terrain obj, Lane[] lane, int x)
    {
        //check certain terrain object backward
        int start = x-1;
        for (int j = start ; j >= 0 && lane[j] != null ; j--)
        {
            if (lane[j].terrain.equals(obj))
            {
                return true;
            }
        }
        return false;
    }

    private Boolean laneObstacleClear(Lane[] lane, int x, int distance)
    {
        return !checkObjForward(Terrain.OIL_SPILL, lane, x, distance) &&
        !checkObjForward(Terrain.WALL,lane, x, distance) &&
        !checkObjForward(Terrain.MUD,lane, x, distance);
    }

    private Boolean laneHasPowerUp(Lane[] lane, int x, int distance)
    {
        return checkObjForward(Terrain.TWEET, lane, x, distance) ||
                checkObjForward(Terrain.EMP,lane, x, distance) ||
                checkObjForward(Terrain.BOOST,lane, x, distance)||
                checkObjForward(Terrain.LIZARD,lane, x, distance)||
                checkObjForward(Terrain.OIL_POWER,lane, x, distance);
    }
}
