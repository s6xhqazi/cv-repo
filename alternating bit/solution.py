import socket
import argparse


def listen():
    # declare the socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # connect to the given website
    sock.connect(('dev-net-4.cs.uni-bonn.de', 9999))
    # set a timeout, given value was 5 sec
    sock.settimeout(5)
    # initialize needed values, variables
    message = ''
    receiving = True
    received = ""
    alternating = '0'
    last_alternating = '-1'
    while receiving:
        # data received should be wrapped in a try-except block
        try:
            # receive and print the data
            data = sock.recv(16)
            print(str(data)[2:-3])
        except:
            # resend if no response arrived
            sock.sendall(bytes(message, 'UTF-8'))
            # print the message sent
            print(message)
            continue
        if data == b'' or data.__contains__(bytes('\\0', 'UTF-8')):
            # if the NULL-Byte reached stop
            break
        # read the alternating byte
        alternating = chr(data[-2])
        if data.__contains__(bytes('BROKEN', 'UTF-8')):
            # if broken report
            message = "NACK|%s" % alternating + "\n"
            sock.sendall(bytes(message, 'UTF-8'))
            print(message)
            continue
        if alternating == last_alternating:
            # if repetition, resend confirmation
            message = "ACK|%s" % last_alternating + "\n"
            sock.sendall(bytes(message, 'UTF-8'))
            print(message)
            continue
        if alternating != last_alternating:
            # if new add to string and confirm
            message = "ACK|%s" % alternating + "\n"
            sock.sendall(bytes(message, 'UTF-8'))
            print(message)
            last_alternating = alternating
            received = received + chr(data[0])
    print(received)
    return received


def debug(string):
    print("DEBUG: ",  string)


def solution(string):
    print("SOLUTION: ",  string)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('command', type=str, help='the command to execute')

    args = parser.parse_args()
    debug("Running command: " + args.command)
    if args.command == "alternating":
        solutionstring = "Connection Failed"
        try:
            solutionstring = listen()
        except:
            pass
        solution(solutionstring)
    else:
        debug("Invalid command")

