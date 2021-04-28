# AI-Final-Project
Repository for our final project in Artificial Intelligence

Old School Runescape Reinforcement Learning Agent Project
Authors: Declan Jeffrey, Shane Phillips, Wren Maybury

**Overview:**

The code within this repository was written during a project that makes use of OSBot's ability to interface with Old School Runescape's game client and can control a player character with scripts written in Java using the OSBot API. With that tool we were originally going to create an artificial intelligence agent that made use of Q-Learning in order to learn how to gather several kinds of resources from the Old School Runescape world including logs, ores, and fish. Unfortunately due to time constraints we were only able to implement Q-Learning for an agent that learns how to chop down trees and store the logs it gathers in its bank, while saving its learned data so that the agent does not need to completey restart the learning process every time our script is run. While we found that our agent, at least with limited training, is commonly less effecient in terms of time spent filling its entire inventory with resources than an agent that simply gathers from the closest resource nodes, the Old School Runescape banning system designed to stop cheaters from exploiting automated game controllers takes considerably longer to detect our learning agent than the faster, simpler one. 

**Files:**

 The script for the learning agent makes use of several java class files in order to interact with the game client and learn how to gather resources through the OSBot software, and one text file to store any learned information about states accumulated during runtime:
 
 **Artificial_Intelligence_Project.java** - This file is the main file used by the script, containing all functionality and code that interacts with the OSBot client to gather information from the game space. This file also contains toggeable debugging information displays both actively updating on screen and in the OSBot client's logger. Determining legal actions, executing selected actions, calculation of states given actions, as well as keeping track of the length of gathering episodes all occur in this class.
 
 ADD SOME FUNCTIONS
 
 **QLearning_Agent.java** - This class is responsible for storing learned Q-values in a hash map making use of positions in Old School Runescape and action labels to represent states. When starting without data, this class will handle creation of the hashmap to hold "blank" values for every position in the area an agent is restricted to. When data from previous learning is detected it is used to overwrite appropriate state information for positions recorded in the data. Calculation of optimal actions in a given state, actions chosen when the exploration rate comes into play, as well as updating of Q-values is handled in this class.

ADD SOME FUNCTIONS

**State.java** - This class is referenced in both other class files of this project for storing and retrieving simple information about the agent when it is in a given state. The main functionality outside of storing information is creation of the area the aagent is allowed to learn within in the game space (this is important because a vital aspect of our project relied on restricting the scope of the learning environment).

**trainingData.txt** - This file stores any information learned by the agent over the duration of the script including: Q-values of individual states, current exploration rate (the value decreases over time), and times for any completed gathering runs. The text file is read from in the beginning of the Artificial_Intelligence_Project.java class after the agent has reached to position all learning episodes start at. Information learned during the running of the script is stored into this file when the script is stopped. 


**Meeting Information:**

Meeting # 1 (3/21/2021)
Notes:
  Start with tree cutting, Q-learning agent. 
  Posisbly move to more advance task, like combat if possible. 
  Learn how to read/write to file so you can save weights/values of states (or do we?)
  Other forms of RL? 
  Possibility: Combing tasks to optimize getting gold or exp
  Show progress from:
    Basic AI
    Basic AI that performs tasks
    Basic AI with RL to improve certain tasks and optimize getting materias
    RL AI that can perform multiple tasks and optimize something general like getting gold or exp
    RL AI that can perform combat, optimizing living, defeating enemies, gaining exp
    (May not get a fully functional combat AI, but show progress and difficulties in report )
  Take into account leveling up. Some tree/rocks/other materials can only be mined at a certain level but give more exp.
  Restrict search area to smaller section so he doesn't wonder the whole map

Progress:
  Watched videos to create our own general AI that can navigate the environment and perform basic tasks
  Created Final Report copy in open leaf, and started editing
  Coded the ability to find the closest trees and tell us what it is and where it is
  
  Meeting #2 (3/28/2021)
  Progress:
    -Made agent find the nearest tree and go cut it down. 
    *How do we make the agent go to the best tree, not necessarily the closest. (has to go around walls and take a longer path, while passing other trees)
    -Include some sort of epsilon? Where he may chose to explore and go to a random tree, or randomly deposit the resources. 
    
  TO DO:
    -Conditions for being high enough level, having an axe, someone else interacting with the tree
    -Finding the real closest tree , not having to go around walls 
    -Q-Learning : 
      + Actions : (go to tree, chop tree, deposit)
      + Rewards : (gold , amount of lumber, experience)
      + Punishments : (time, distance)
    -Select starting space and create episode of cutting trees and depositing, learn based on how good they are. 
  
  Meeting #3 (4/4/2021)
  Progress:
    -Finished polishing off the basic lumber collecter. 
      *Jumping off point. Something to gain info from, gain base data. 
    -Started trying to implement Q-learning into the agent.
      *Do we need a reinforcement learning agent class? Or can we just implement a Q-learning class and have all the necessary info in there?

  TO DO/ CONSIDER: 
    -Q-learning class: evalue state action pairs. Determine the best action based on those state, action pairs. 
      *Learning rate, epsilon, discount
      *When determing the "Closest" tree, don't base off of something generic like manhattan distance, but the actualy path length to get there.
    -3 basic agents: Lumber, Mining, Fishing
      *Fishing = Probability of getting different fish, so that would affect the exp/gold gained. 
      *Restrict search area/ Allow for the use of additional banks 
      *Episodes can be based on starting -> getting resources -> and banking. 
       Our final agent will ideally not just bank when his inventory is full,  but when it is the most optimal action (or randomly/epsilon). 
       The performance of the Q-learning agent should then be based on how quickly he was able to collect those resources/gold/exp and bank them. 
       Whereas the performance of the baseline agents is just based on how quickly they can fill up their inventory and bank it. 
      *Consider respawn rate of resources, how would that work? 
    -3 Q-learning agents: Lumber, Mining, Fishing
    -POSSIBLY: Combat. See how the other 3 go, and maybe try and dabble with the combat a little
    -1 basic agent that can collect all 3 resources
    -1 Q-learning agent that can optimize gold and exp while collecting all the resources. 
    -Consider epsilon, and the idea of exploration. Not necessarily just always cutting the cloest tree. 
    -Text File with learned Q-values , And another to store the baseline so we can re learn when we want to 
    
 Meeting #4 (4/9/2021)
 Progress:
  - Got the baseline agent finalized. With GUI displaying path to tree agent is going to, as well as displaying time taken in each episode. 
  - Also fixed pathing issue of finding closest tree but wasnt really (closest manhattan distance)
  - Worked on report, got a good baseline for that as well. Set up tables to store information of q-learning agents episodes. 
 TO DO/Consider
  -Make other 2 baseline agents (shouldn't be too bad)
  -Start implementing Q-learning. 
    + List of actions should include not just closest tree, but a list of the top 10-20 cloest trees. 
    + There should then be some epsilon, meaning a random chance to not go to the seemingly optimal solution but explore a different close tree. 
    + By going to a random tree, the agent learns the value of that action. Is it going to be the new optimal action? Or is it not worth it?
    + How many runs do we need? 5, 10, 50, 100? -> Is there a limit, and does OSBOT stop you from running that many times. 

Meeting #5 (4/16/2021)

Progress:
  + Even more problems with getting OS BOT working on Declan's computer. Keeps giving us trouble when trying to set up java, not sure why. 
  + Started implementing Q-learning into the wood cutting agent
  + Base file = agent class (Woodcutter, fishing, mining)
  + AI FINAL PROJECT file = reinforcement learning agent class 
  + Q learning file = q learning agent file
  + State class -> Class to store the state information (player position, player health, list of close trees, list of close enemies, list of close player)
  + Actions -> Probably good to just store in a data structre -> (go to tree, chop the tree/mine, deposit materials, movement)
  + Do we want to store learned values in a txt file? That would mean he does not start from square zero when learning on each start of the learning. 
  + State Class:
    - Things to consider in state: Playe position, trees aroung that position, enemies around that position, players health  
    - Epsilon -> Going to never before seen trees 
  
 TODO/Consider
  + Finish Q-learning for wood cutter
  + Is it too comlicated? Or can we apply it to other tasks. 
  
Meeting #6 (4/18/2021)

Progress:
  + Each individual **state** would be each individual tile. 
  + **Actions**: North, east, south, west, cut tree, deposit  
  + **Legal action**s: restrict directions depending on surrounding environment (and considered range/total range), cut tree only legal when by tree, deposit only legal when by bank 
    - additional checks to make sure that: if inventory is full you cannot chop a tree, if inventory is not empty you can deposit. 
  + Creating **agent bounds** (max are the agent should go to -> limit scope of game, because otherwise state space would be massive) 
  + The agent also has a **conisdered range**: within the bounds of the game, only consider trees/materials so close.(too unoptimal to go too far, but can still happen to an extent)
  + Creating **Actions Class** to store information about the actions and how to use them
  + Got **Get Legal Actions** working, but had to place it in the main (AI project) -> Problems there??
    - Had to get specific with chop wood and bank, make sure inventory is full/empty and make sure we are by a tree to chop
  + *KEEP IN MIND* Do we want to turn off auto retaliate?

Meeting #7 (4/19/2021)

Progress: 
  + Have to do our action stuff in the main agent file instead of being able to create a supporting action file. This is due to how the osbot domain works, functional scripts have access to more commands than supporting scripts. And making actions as its own functional script would make no sense. 
  + Creating nested hash map to store q values for state and actions 
 
Consider: 
  + Storing Q-values. Dictionary type storage with (state,action) as key. Then state values are calculated from all possible q value
  + Create feature values for important state space info like: closest tree, closest enemy, distance to bank. Better help learning process
  + Negative rewards (punishments) , to better evaluate state, acition pairs. Time, enemies, other players.
  + Make the agent chop the tree if it has never evaluated that state before. (needs to learn reward) 

Meeting #8 (4/21/2021)

Progress:
  + change around some stuff: banking is only action when inventory is full (maybe mess with), Change starting posisition so he is close to trees to allow him to learn faster
  + Start with empty map of maps and initialize each state as you get to it.
  + Had to create copies to ensure we were not changing each states q values
  + **Created states with q values initialized to zero**
  + **Epsilon greedy**: Had to use integer to use as chance for taking random action

Consider: 
  + starting with a high epsilon and decreasing over episodes to avoid taking the same path over and over 
  + If we want to store q values in a file, we would want to keep track of the last epsilon value used
  + We need reference to legal actions in the current state. (sending a list of legal actions to the state when we create it, so we can reference it)

Meeting #9 (4/22/2021)

Progress:
  + **Got Q learning working!** For the most part..
  + Visualzation of state values within the environment   

Consider:
  + Cutting tree gives double reward, change how we award points --> **RESOLVED**
  + Reward for action of cutting tree in current state stacks meaning cutting a tree multiple times would make a state more valuable. (mess with that??)
  + Maybe mess around with the negative punishment of staying alive , to deter waiting around for a tree
  + Focus on storing Q values in a file, so we can use them and better understand how it learns overtime

Meeting #10 (4/23/2021)

Progress:
  + Finished up the slides for our presentation, did our presentation.
  + Ran our agent a couple time to get some results

Meeting #11 (4/24/2021)

Progress:
  + Started working on using a text file to pull and push q values into 

Meetin #12 (4/24/2021)

Progress:
  + Manged to get saving q values and epsilon to a file working!!
  + Started training the agent intensly and hoping to find some good results
  
Consider:
  + More colors for GUI for state values. Tweak values for each color in the GUI
  
  
Meeting #12 (4/25/21-4/26/21)

Progress:
  + Written report, citations, training agent.
  
Meeting #13 (4/27/21)
 
Progress: 
  + Tweaked some visualization. Changed values and colors of GUI for coloring states based on values. 
  + Trained the agent more
  + Worked on the report more
  
