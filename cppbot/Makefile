all: bot

clean:
	rm -f bot *.o

bot: main.o mybot.o HandEval.o
	$(CC) -o $@ $(CFLAGS) $^

HandEval.o: HandEval.c HandEval.h
	$(CC) -c -o $@ $(CFLAGS) $<

main.o: main.c mybot.h HandEval.h
	$(CC) -c -o $@ $(CFLAGS) $<

mybot.o: mybot.c HandEval.h mybot.h
	$(CC) -c -o $@ $(CFLAGS) $<
	
