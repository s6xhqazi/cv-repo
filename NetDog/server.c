#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>

int main(int argc, char ** argv) {
// Check if all the needed arguments are given
    if (argc < 2){
        printf("Hey yo, NetDog here! \n Bra you use the server by writing: \n ./NetGog s <Port> \n Peace! Ciao!\n");
        return -1;
    }
    // Initialize some variables needed for the programm
    int port = htons(atoi(argv[1]));
    int sock;
    struct sockaddr_in client;
    struct sockaddr_in server;
    char buf[256];	
    // Start a socket and connect
    if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    {
        printf("Yo Doug... I got bad news Bruh... Your servers socket is broken.\n");
        return 1;
    }
    // bind the binary to the socket
    server.sin_family      = AF_INET;
    server.sin_port        = port;
    server.sin_addr.s_addr = INADDR_ANY;
    if (bind(sock, (struct sockaddr *)&server, sizeof(server)) < 0)
    {
        printf("I'm so sorry Doug... Couldn't find the Post Office. NetDog is lost damnit!\n");
        return 2;
    }
    socklen_t client_size = sizeof(client);
    // Get messages and print them.
    while(1){
        if(recvfrom(sock, buf, sizeof(buf), 0, (struct sockaddr *) &client,
                    &client_size) <0)
        {
            printf("Hey Doug... It's snowing I don't wanna check your damn Mailbox. Send the cat.\n");
            return -1;
        }
        printf("Yoooo Broooo! I got this: %s\n", buf);
    }
}
