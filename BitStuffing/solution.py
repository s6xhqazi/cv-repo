import argparse
import socket


def debug(string):
    print("DEBUG: ",  string)


def solution(string):
    print("SOLUTION: ",  string)

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
    
def xorString(str1,str2):
    res = ""
    length = len(str1)
    j = 0;
    
    if len(str1) > len(str2):
        return str2

    for i in range(length):
        res = res + str(int(str1[i]) ^ int(str2[i]))
    
    while (len(res) != 0) and (res[j] == "0"):
        res = res[1:]

    if len(res) == 0:
        res = "0"

    return res


def calcRemainder(polynom, message):
    length = len(polynom)
    diff = 0
    m = message
    res = m[:length]
    m = m[length:]

    while True:

        if (len(m) == 0) and (len(res) < length):
            break
        if len(res) < length:
            diff = length - len(res)
            res = res + m[:diff]
            m = m[diff:]

        res = xorString(polynom, res)

    return res


def crc(polynom, nutzdaten):

    message = nutzdaten + (len(polynom) - 1) * "0"

    remainder = calcRemainder(polynom, message)

    if (len(remainder) != (len(polynom) - 1)):
        diff = len(polynom) - len(remainder) - 1
        remainder = diff * "0" + remainder

    result = nutzdaten + remainder

    return result


def crccheck(polynom, nutzdaten):

    remainder = calcRemainder(polynom, nutzdaten)


    if int(remainder) == 0:
        return "OK"
    else:
        return "NOTOK"



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('command', type=str, help='the command to execute')
    parser.add_argument('polynom', type=str, help='Polynom Bitstring')
    parser.add_argument('message', type=str, help='Nutzdaten Bitstring')

    args = parser.parse_args()
    debug("Running command: " + args.command)

    if args.command == "crc":
        solution(crc(args.polynom, args.message))
    elif args.command == "crccheck":
        solution(crccheck(args.polynom, args.message))
    elif args.command== "alternating":
        solutionstring = "Connection Failed"
        try:
            solutionstring = listen()
        except:
            pass
        solution(solutionstring)
    else:
        debug("IllegalArgumentException")

