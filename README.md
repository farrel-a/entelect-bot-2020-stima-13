# Entelect Challenge 2020 Bot Tugas Besar Strategi Algoritma IF2211 Tahun 2022

## 13520110 - Farrel Ahmad
## 13520143 - Muhammad Gerald Akbar Giffera
## 13520148 - Fikri Ihsan

<br>

## Introduction
This bot is implemnted using Greedy approach algorithm. The greedy algorithm is divided into 5 different greedy types. These are greedy by speed, greedy by damage, greedy by obstacle, greedy by power up pick up, and greedy by offensive pick up. Each of this type has their different approach but towards the same goal, to get to the finish line as fast as possible.

- Greedy by speed : reach the speed as high as possible
- Greedy by damage : minimize fixing the car but before it breaks the car
- Greedy by obstacle : avoid as much obstacle as possible
- Greedy by power up pick up : pick up as much power up as possible
- Greedy by offensive power up : use offensive power up as much as possible to slow down the enemy

<br>

## Requirements
1. Intellij Idea IDE
2. Java (Java 8 Minimum)

<br>

## Compile and Execution
1. Download the starter-pack from Entelect 2020 Challenge gitub repository https://github.com/EntelectChallenge/2020-Overdrive
2. Clone this repository
3. Copy `PanturaRezing-bot`to the starter-pack directory
4. Open Intellij Idea IDE from your computer and open the starter-pack
5. Find `pom.xml` inside the `PanturaRezing-bot` directory
6. Open the file and right click on the code at any line
7. Click Add as maven project
8. Right click at the file and run maven -> plugins -> maven assembly plugin -> assembly:assembly
9. This will compile and make the jar executable file needed
10. Find game-runner-config.json and change the starter-bot player directory to PanturaRezing bot directory, that is `./PanturaRezing-bot/java`. The line will look like this.
```
  "player-a": "./PanturaRezing-bot/java",
  "player-b": "./reference-bot/java",
```
11. Run the game by running `run.bat` file.
12. Have fun !