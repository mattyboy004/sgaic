#!/bin/bash
min=1
max=100
javac MyBot.java
rm -f results
for i in `seq $min $max`; do
	echo 'Game number' $i '...'
	echo 'Game number' $i '...' >> results
	java -jar tools/PlayGame.jar maps/map$i.txt 1000 200 log.txt "java -cp . MyBot" "java -cp ./versao6 MyBot" 2>> results > /dev/null
	tail -1 results
done;
echo '- - - - - - - - - - - '
echo 'Total number of games:'
echo $(($max-$min+1))
echo 'Number of Player 1 wins:'
grep results -e 'Player 1' -c
echo 'Number of Player 2 wins:'
grep results -e 'Player 2' -c


#javac MyBot.java  && java -jar tools/PlayGame.jar maps/map21.txt 1000 200 log.txt "java -cp . MyBot" "java -cp ./versao5 MyBot" | java -jar tools/ShowGame.jar
##"java -jar example_bots/DualBot.jar"
##"java -cp ./example_bots MyBot"
##mapas 7,21,44,6,64
# 50 4532
