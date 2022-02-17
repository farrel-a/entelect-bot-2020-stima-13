package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.State;
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

        /*Getter info
        myCar.position.lane = row/y
        myCar.position.block = column/x
        */

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
        else if (y == 4)
        {
            laneLeft = map.get(y-1 - 1);
        }
        /*---------------------------------------------------------------------------------------------*/

        return decide(laneStraight, laneRight, laneLeft, x, y, myCar, opponent);
    }

    public Command decide(Lane[] laneStraight, Lane[] laneRight, Lane[] laneLeft, int x, int y, Car myCar, Car opponent)
    {
        /*---------------------------------------------------------------------------------------------*/
        // BOT DECISION LOGIC

        /* SETUP ---------------------------------------------------------------------------------------*/
        boolean clearStraight = laneObstacleClear(laneStraight, x, myCar.speed);

        boolean haveTweet = available(PowerUps.TWEET, myCar.powerups);
        boolean haveOilPow = available(PowerUps.OIL, myCar.powerups);
        boolean haveEMP = available(PowerUps.EMP, myCar.powerups);
        boolean haveBoost = available(PowerUps.BOOST, myCar.powerups);
        boolean haveLizard = available(PowerUps.LIZARD, myCar.powerups);

        int x_op = opponent.position.block;
        int y_op = opponent.position.lane;
        /* SETUP ---------------------------------------------------------------------------------------*/


        /* BOOST LOGIC-----------------------------------------------------------------------------------*/
        if (haveBoost) {
            return BOOST;
        }
        /* BOOST LOGIC-----------------------------------------------------------------------------------*/

        /* DAMAGE LOGIC ---------------------------------------------------------------------------------*/
        if (myCar.damage >= 5) {
            return FIX;
        }
        else if (myCar.damage == 4 && myCar.speed > 6 ){
            return FIX;
        }
        else if (myCar.damage == 3 && myCar.speed > 8){
            return FIX;
        }
        /* DAMAGE LOGIC ---------------------------------------------------------------------------------*/

        /* STUCK FORCE ACCELERATE------------------------------------------------------------------------*/
        try
        {
            if (myCar.state.equals(State.NOTHING))
            {
                if(myCar.damage >= 5)
                    return FIX;
                else
                    return ACCELERATE;
            }
        }
        catch (Exception e)
        {
            if(myCar.damage >= 5)
                return FIX;
            else
                return ACCELERATE;
        }
        /* STUCK FORCE ACCELERATE------------------------------------------------------------------------*/

        /* AVOIDANCE LOGIC-------------------------------------------------------------------------------*/
        if (!clearStraight)
        {
            if (haveLizard)
            {
                return LIZARD;
            }
            else
            {
                if (y == 2 || y==3)
                {
                    boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
                    boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
                    boolean powerUpLeft = laneHasPowerUp(laneLeft, x, myCar.speed);
                    boolean powerUpRight = laneHasPowerUp(laneRight, x, myCar.speed);
                    if (clearRight && powerUpRight)
                    {
                        return TURN_RIGHT;
                    }
                    else if (clearLeft && powerUpLeft)
                    {
                        return TURN_LEFT;
                    }
                    else if (clearRight)
                    {
                        return TURN_RIGHT;
                    }
                    else if (clearLeft)
                    {
                        return TURN_LEFT;
                    }
                }

                if (y==1)
                {
                    boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
                    if (clearRight){return TURN_RIGHT;}
                }

                if (y==4)
                {
                    boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
                    if (clearLeft){return TURN_LEFT;}
                }

                //cannot turn both are not clear
                return ACCELERATE;
            }
        }
        /* AVOIDANCE LOGIC-------------------------------------------------------------------------------*/



        /* OFFENSIVE LOGIC-------------------------------------------------------------------------------*/
        if ((haveTweet|| haveOilPow) && x > x_op)
        {
            if (y == y_op)
            {
                if (haveTweet)
                {
                    return new TweetCommand(y, x);
                }
                else
                {
                    return OIL;
                }
            }
            else
            {
                //tailing car behind
                if (y < y_op)
                {
                    boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
                    if (clearRight) {return TURN_RIGHT;}
                }
                else
                {
                    boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
                    if (clearLeft){return TURN_LEFT;}
                }
            }
        }

        if (haveEMP && x < x_op)
        {
            //if aimed correctly
            if (y == y_op)
            {
                return EMP;
            }
            //else, correct aim
            else
            {
                if (y == y_op-1)
                {
                    boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
                    if (clearRight){return TURN_RIGHT;}
                }
                else if(y== y_op+1)
                {
                    boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
                    if (clearLeft){return TURN_LEFT;}
                }
                else
                    return ACCELERATE;
            }
        }
        /* OFFENSIVE LOGIC-------------------------------------------------------------------------------*/

        /* CATCH POWER UP LOGIC---------------------------------------------------------------------------*/
        if (y == 1)
        {
            boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
            boolean powerUpRight = laneHasPowerUp(laneRight, x, myCar.speed);
            boolean clearFront = laneObstacleClear(laneStraight,x,myCar.speed);
            boolean powerUpFront = laneHasPowerUp(laneStraight,x,myCar.speed);
            if (clearFront && powerUpFront)
            {
                return ACCELERATE;
            }
            else if (clearRight && powerUpRight){return TURN_RIGHT;}
        }
        if (y == 4)
        {
            boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
            boolean powerUpLeft = laneHasPowerUp(laneLeft, x, myCar.speed);
            boolean clearFront = laneObstacleClear(laneStraight,x,myCar.speed);
            boolean powerUpFront = laneHasPowerUp(laneStraight,x,myCar.speed);
            if (clearFront && powerUpFront)
            {
                return ACCELERATE;
            }
            else if (clearLeft && powerUpLeft)
            {
                return TURN_LEFT;
            }
        }

        if (y==2 || y==3)
        {
            boolean clearLeft = laneObstacleClear(laneLeft, x, myCar.speed);
            boolean clearRight = laneObstacleClear(laneRight, x, myCar.speed);
            boolean clearFront = laneObstacleClear(laneStraight,x,myCar.speed);
            boolean powerUpFront = laneHasPowerUp(laneStraight,x,myCar.speed);
            boolean powerUpLeft = laneHasPowerUp(laneLeft, x, myCar.speed);
            boolean powerUpRight = laneHasPowerUp(laneRight, x, myCar.speed);
            boolean hasBoostFront = laneHasBoost(laneStraight,x,myCar.speed);
            boolean hasBoostLeft = laneHasBoost(laneLeft,x,myCar.speed);
            boolean hasBoostRight = laneHasBoost(laneRight,x,myCar.speed);


            if (powerUpFront && clearFront)
            {
                // prioritize boost powerup
                if (hasBoostFront){
                    return  ACCELERATE;
                }
                else if (hasBoostLeft && clearLeft && !hasBoostFront && !hasBoostRight){
                    return TURN_LEFT;
                }
                else if (hasBoostRight && clearRight && !hasBoostFront && !hasBoostLeft){
                    return TURN_RIGHT;
                }
                else{
                    return ACCELERATE;
                }
            }
            else{
                if (clearRight && powerUpRight)
                {
                    return TURN_RIGHT;
                }
                else if (clearLeft && powerUpLeft)
                {
                    return TURN_LEFT;
                }
            }
        }
        /* CATCH POWER UP LOGIC-------------------------------------------------------------------------*/

        // DEFAULT LOGIC
        return ACCELERATE;

        /*---------------------------------------------------------------------------------------------*/
    }

    private Boolean available(PowerUps powerUpCheck, PowerUps[] inventory) {
        //check whether powerUpCheck is in inventory
        for (PowerUps powerUp: inventory)
        {
            if (powerUp.equals(powerUpCheck))
            {
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

    private int countObstacleForward(Terrain obj, Lane[] lane, int x, int distance)
    {
        //count certain obstacle
        int c = 0;
        int startBlock = lane[0].position.block;
        int start = max( (x - startBlock) , 0);
        for (int j = start ; j <= start+distance && j < lane.length && lane[j] != null ; j++)
        {
            if (lane[j].terrain.equals(obj))
            {
                c++;
            }
        }
        return c;
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

    private Boolean laneHasBoost(Lane[] lane, int x, int distance)
    {
        return  checkObjForward(Terrain.BOOST,lane,x,distance);
    }
}

