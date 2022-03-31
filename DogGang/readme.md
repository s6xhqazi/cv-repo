Our designed program was NetDog. Since we foresaw(Yes Dr. Who and Dr. Starnge were our students, that's why they can see the future too!) the issue we designed the program to be able to run a client and server separately. 
Broken down the server only needs to use stdout while the client uses stdin and stdout, with the second being optional.
The important part is that the server doesn't need stdin after it starts, hence it can run in the background and pipe its stdout to the foreground, while the client gets the main stage.

In our Bash implemetation(more of a jump starter then an implementation) the server is sent to the Background by using the "&" command, which let's one program run in the Background and connects the stdout of that program with the current stdout.

Other ways to implement this non-blocking IO would be to manually start the client and server by a third program and connect both stdouts to fifo, which can be usually be read by a thread(listener), while the stdin of the client would be connected to the user input.
A more concrete way of solving the problem would be to start the server with popen() in non blocking read mode inside the client.
Another not so efficient way would be to have a program spawn a child using fork() then start the server in the child and the client in the main program. The stdout would be connected to the child and stdin to the parent, since we are dealing with two processes there is no deadlock or blocking.

Note: In our case, the way the program was written, it would be more difficult writing and reading sequentially since the program wasn't design to read and write single lines. 
