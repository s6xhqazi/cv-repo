#!/usr/bin/env python3
import argparse


def debug(string):
    print("DEBUG: ", string)


def cidr(ip):
    # Read the length of the Prefix
    prefixlen = int(ip.split("/", 1)[1], 10)
    # Read the Net Address
    ip = ip.split("/", 1)[0]
    ipnums = ip.split(".")
    ipbins = []
    # Fill the rest of the Net Address with 0
    prefix = bin(0)[2:].zfill(prefixlen)
    for num in ipnums:
        ipbins.append(bin(int(num, 10))[2:].zfill(8))
    # separate the given numbers, create a list
    for i in range(0, prefixlen):
        prefix = prefix[0:i] + ipbins[int((i - i % 8) / 8)][i % 8]
    prefixbins = []
    bins = ""
    # append each binary representation to the list
    for i in range(0, prefixlen):
        bins = bins + prefix[i]
        if (i + 1) % 8 == 0:
            prefixbins.append(bins)
            bins = ""
    # Add the last one if given
    if len(bins) > 0:
        prefixbins.append(bins + "0" * (8 - len(bins)))
    # Fill the rest with 0
    while len(prefixbins) < 4:
        prefixbins.append("0"*8)
    # Convert the data to the readable version of Net Address
    NETADDR = ""
    for num in prefixbins:
        NETADDR = NETADDR + str(int(num, 2)) + "."
    NETADDR = NETADDR[:-1]
    solution("NETADDR " + NETADDR)
    # Convert the data to the readable version of Net Mask
    netmask = "1" * prefixlen + "0" * (32 - prefixlen)
    NETMASK = ""
    for i in range(0,4):
        NETMASK = NETMASK + str(int(netmask[8*i:8*i+8],2)) + "."
    NETMASK = NETMASK[:-1]
    solution("NETMASK " + NETMASK)
    # Convert the data to the readable version of Broadcast Address
    broadcast = prefix + "1" * (32 - prefixlen)
    BROADCAST = ""
    for i in range(0,4):
        BROADCAST = BROADCAST + str(int(broadcast[8*i:8*i+8],2)) + "."
    BROADCAST = BROADCAST[:-1]
    solution("BROADCAST " + BROADCAST)
    # calculate and print Host Count
    solution("HOST_COUNT " + str(2**(32-prefixlen) - 2))
    # calculate and print MIn_Host
    MIN_HOST = NETADDR[:-1] + "1"
    solution("MIN_HOST " + MIN_HOST)
    # calculate and print Max_Host
    solution("MAX_HOST " + BROADCAST[:-1] + str(int(BROADCAST[-1:],10) - 1))


def solution(string):
    print("SOLUTION: ", string)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('command', type=str, help='The command to execute')
    parser.add_argument('ip', type=str, help='The IP to analyze')
    args = parser.parse_args()
    debug("Running command: " + args.command)
    if args.command == "cidr":
        cidr(args.ip)
    else:
        debug("Invalid command")

