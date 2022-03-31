import argparse


def debug(string):
    print("DEBUG: ",  string)


def solution(string):
    print("SOLUTION: ",  string)


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
    else:
        debug("IllegalArgumentException")

