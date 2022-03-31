CC = clang
CFLAGS=-Wall -Wpedantic -g -std=gnu99  -fsanitize=address
LDFLAGS=-Wall -Wpedantic -g -std=gnu99 -fsanitize=address
RM = rm -f

OBJECTC = client.o
OBJECTS = server.o

%.o: %.c client.c server.c
	$(CC) -c -o $@ $< $(CFLAGS)


client: $(OBJECTC)
	$(CC) -o $@ $^ $(LDFLAGS)
server: $(OBJECTS)
	$(CC) -o $@ $^ $(LDFLAGS)


.PHONY: clean
clean:
	$(RM) -r *.o client server dogspost*
