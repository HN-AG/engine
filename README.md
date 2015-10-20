# Hashbuddy engine
This is Hashbuddy trading engine, it is intended to be a framework for you to use to build your own bot.
It is batteries included and has many features for advanced traders to use.

If you're looking at this project it's very likely that you have been trading mining hash power for awhile now and you're tired of trying to babysit your positions all day.
You have some skill making spreadsheets, you've probably even built a website before, but the APIs to build a bot are just confusing and very boring to have to deal with.

If that's you, then you've come to the right place!

This is probably the framework you've been looking for.  We've gone to the trouble of extracting away all the complex APIs for...
Hashnest, Antpool, BitcoinAverage and more.

We've created a very simple, easy to use and consistent interface and the best part is...
If you've ever built a webpage, you already know how to build your own bot using this framework.

That's right! The default strategy execution engine is just plain ol' JavaScript.

We've seperated the strategies over into their own repo.
That repo is over here...
https://github.com/hashbuddy/strategies

You can use those as a guide to building your own bot and you should probably take a close look at them the get a feel for what it takes.
The plain and simple truth is a strategy is ANY file that provides the following functions..

boolean onInit() //Used for initialization and setup

void onTick() //Application and trading logic entry point

boolean onExit() //Used for clean up, such as emailing a summary

Since the framework is built on top of the Java 8 scripting framework, you can write your strategies in any language supported by Java 8 and there are alot of them.
A reasonably current list is located here...
https://en.wikipedia.org/wiki/List_of_JVM_languages

If you decide to use a different JVM language you should pass the --engine="WHATEVER" flag otherwise you'll find that your script doesn't run.
It is beyond the scope of this document to tell you how to adapt or use those languages, but as long as your strategy file includes at least the methods mentioned earlier, it should just work.

We are going to focus on JavaScript here because it's by far the most widely understood language.
The specific variant we use is the Java 8 "Nashorn" engine.  This language is not your browser's javascript, but it's close enough.
It's also not Node.js, but again it's close enough.

More detail on creating and testing strategies is posted on the strategies wiki, as well as out website hashbuddy.io

For now let's focus on getting you setup with the engine.

The very first thing you need is Java 8 this is not included by default in many operating systems.
It is extremely important that your version of Java is at least Java 8, or nothing will run properly, Nashorn is NOT Rhino.
So step by step here is how you get setup...

Step 1:  Check your java version.  "java -version" if it doesn't say something like {java version "1.8.0_60"} then you have problems and need to google for a solution.
Step 2:  Install git & maven then check the versions.  git --version && mvn --version  (be sure to check the output of maven it will tell you which JVM it's using, it should match the output of java -version)
Step 3:  Clone this repository... git clone https://github.com/hashbuddy/engine.git
Step 4:  Clone the other repository...  git clone https://github.com/hashbuddy/strategies.git
Step 5:  Change to the engine directory...  cd engine
Step 6:  Create a link between engine and strategies...  ln -s ../strategies strategies
Step 7:  Run the rebuild.sh file
Step 8:  Change the directory to runme...  cd runme

Now you need to stop a minute and do some information gathering.
First of all you're going to need an API key & secret from Hashnest, this is available in your account under settings.

When you create the key make sure to give it all the available permissions, otherwise the engine will tend to fail, it will seem random and lots of people are going to be mad at you for wasting their time chasing a bug because you couldn't be bother to tick a check box.
This is really safe to do because Hashnest does not allow withdrawals via API.
Remember that when you create the key you have to activate it by email, but before you do that you should write down both the key and secret so you have them for later.
While you're at Hashnest, figure out what your user name is by hoping into the trollbox and asking someone, let them know you're setting up a Hashbuddy bot and aren't quite sure.
You can also just set it in settings, either way be advised, it is NOT your email address and it has no @ in it!

Now you have your hashnest API key and secret, you need to go to Antpool and do the same.  Fortunately the process is much the same, your username will be your miner/worker, you may need to set one up if your haven't already.
Eventually you'll get to an account screen, again copy your username, api key & api secret because you will need them all later.

You've now got just about all the information you need in order to setup and configure your bot.
Edit the "testme.sh" file (sorry windows folks, you're on your own). and make sure it has the following information...

Step 9:  Create & or Edit testme.sh  it should look roughly like the following...
java -Djsse.enableSNIExtension=false -jar hashbuddy.jar \
--TICKRATE=300 \
--HASHNESTAPIUSERNAME=yourname \
--HASHNESTAPIKEY=changeme \
--HASHNESTAPISECRET=changeme \
--ANTPOOLAPIKEY=changeme \
--ANTPOOLAPISECRET=changeme \
--ANTPOOLUSERNAME=changeme \
--strategy=strategies/main.js \
--training-wheels="ON"

Allow me to explain this one to you step by step so you understand what's going on here...
By default Java really, really hates whatever certificate authority it is that Hashnest used to sign their SSL certificate.
So we unfortunately have to tell java to get bent.

The next thing is the jar file hashbuddy.jar the character \  just tells bash that the next line is part of the same command and not a seperate command.
--TICKRATE is how many seconds the framework will wait in between calls to your strategies "onTick" method.  The default is 300, which works out to pretty close to 5 minutes.
This is a "clean" time, meaning that the timer starts only after the onTick method returns, so there is nothing to worry about from setting it all the way down to 0
FROM AN ARCHITECTURAL STANDPOINT!
However, if you do set it to 0 or anything less than about 120, there are very good odds that Hashnest will rate limit you if not banning you outright, same thing is true for Antpool.
Don't be a jerk, don't overwhelm the APIs that are giving you this information at no cost to yourself.  Remember you aren't entitled to these APIs, they are just a gift so you don't have to sit there and babysit your computer all day long.
Next are these little lovelies...

--HASHNESTAPIUSERNAME=yourname 
--HASHNESTAPIKEY=changeme 
--HASHNESTAPISECRET=changeme 

This is the information you need to connect up to the Hashnest API.
It should be self explanatory, please use common sense, fill them out with the information you obtained earlier.

Next is the Antpool section, this information will be different than the information from Hashnest, but the concept is the same.
--ANTPOOLAPIKEY=changeme 
--ANTPOOLAPISECRET=changeme 
--ANTPOOLUSERNAME=changeme

Next up is your choice of strategy.
The strategy you choose is entirely up to you, it defaults to main.js which does nothing other than say hello and then go back to sleep.
You can and should use that as a basis for your own strategy, feel free to also pull in any fragments you desire from the strategies/fragments directory, that's what their there for.

If you're not using javascript and have decided something else is more your style, for instance jython.
You should add a  --engine="whatever" flag here or you'll end up with an incredible amount of errors in your console.

Finally is training wheels.

Your testme.sh file should ALWAYS leave the training wheels ON, training wheels allow you to see everything your bot is going to do, but they prevent the API from submitting orders.
Training wheels only effect order submission.  Everything else remains the same.
It is never a good idea to remove them until you are reasonably confident that the strategy you have created is going to execute trades the way you have planned.

For example, you will see (especially in the fragments) a lot of code that looks like.
Number(Number(some) * Number(formula)).toFixed(8)

That code exists because some API endpoint or another sent through a number as a string and Javascript really loves to do things like this...
"5"+5 = "55"

Then when you go to multiply or divide that value suddenly you get the dread NaN.
NaN stands for not a number and it means something evaluated and it wasn't numerical in nature.
NaN is contagious and someone should really consider hunting down whoever decided that this little gem of Javascript made perfect sense...

console.log(Number.isNumber(NaN))
Try it, then come back when your jaw has been picked up off the floor.

Yes folks, that's right NaN, which means NOT A NUMBER evaluates to TRUE!
Now consider what happens when your bot submits that as a sales price for your hash.

Yeah, it's not pretty folks.  
This is one of the major reasons we implemented training wheels and turned then on by default...

Step 10: cp testme.sh runme.sh Doing this will allow you to have a test and a run script
Step 11: Edit runme.sh and set --TRAININGWHEELS="OFF" you might also want to add these 3 lines to the bottom of it

echo "Application encountered an error and will restart in 10s, press ctrl+c to interrupt it"
sleep 10
./hashbuddy.sh

Adding those allows you to have an environment that can handle something strange occuring, such as the JVM being yanked out and/or swapped (I've seen it happen), in the middle of running.
You'll survive a crash and just keep trading.

If you add those lines to testme.sh you'll have great way to reload your bot when you make changes to it.  
Just hit ctrl+c at any time and it will restart in about 10s.


There is a lot of ground to cover over the next few weeks as we standup hashbuddy.io and get the wiki here and in strategies built out.
This really is just the beginning of it.
We want to thank all the community members who have contributed actively to this project.

If this project has been helpful for you, Ash and Bill have setup a coinsplit tip jar.  The proceeds of the tip jar are split evenly among the active contributors.
As we grow more people will be added, at the moment it's Ash, Bill, DJGorilla, k3zzer & Avo who benefit from your donations, with 10% going to a community fund which helps to cover the hosting costs & other misc expenses of the hashbuddy community.
The tipjar address is
#1LpvaYjpphVnbAYMhV5gEbBrVMs2Dxbnk5


Thanks for choosing us and if you need any help, don't hesitate to visit hashbuddy.io (coming soon), or the hashnest trollbox.
Good Luck!

Thank You for taking the time to read this,
Your Friends @ hashbuddy.io



 
