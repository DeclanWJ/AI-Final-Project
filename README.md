# AI-Final-Project
Repository for our final project in Artificial Intelligence

Old School Runescape Reinforcement Learning Agent Project
Authors: Declan Jeffrey, Shane Phillips, Wren Maybury

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
  
