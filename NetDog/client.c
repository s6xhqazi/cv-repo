#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>

int main(int argc, char ** argv) {
// Check if all the parameters are given.
    if (argc < 3){
        printf("Hey yo, NetDog here! \n Bra you use the client by writing: \n ./NetGog c <ipv4_address> <Port> \n Peace! Ciao!\n");
        return -1;
    }
    // Variables for the connection
    int port = htons(atoi(argv[2]));
    int sock;
    struct sockaddr_in server;
    char buf[256];
    in_addr_t ip_server = inet_addr(argv[1]);
    // COnnect the socket
    if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    {
        printf("Yo Doug... I got bad news Bruh... Your socket is broken.\n");
        return 1;
    }
    // Get the server bound
    server.sin_family      = AF_INET;
    server.sin_port        = port;
    server.sin_addr.s_addr = ip_server;
    // Get input and send
    while(1){
        if(fgets(buf, sizeof buf, stdin) != NULL)
        {
        printf("Doug I sent this: %s\n", buf);
            if (sendto(sock, buf, (strlen(buf)+1), 0,
                       (struct sockaddr *)&server, sizeof(server)) < 0)
            {
                printf("Dooouugg... I lost your message! F0€G\n");
                return 2;
            }
        }
    }
}

