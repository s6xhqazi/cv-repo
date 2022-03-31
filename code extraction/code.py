import gdb
import re

def extractIt(decoded):
    gdb.execute('file extractme', False, True)  # Open the file with gdb.
    gdb.execute('break *main+176', False, True) # Continue until line 176 on main
    gdb.execute('r')
    gdb.execute('si', False, True) # Enter the function called by pointer
    edi = gdb.execute("print *$edi", False, True)   # Check the edi register
    gdb.execute('break *0xf7ffb018')                # There is a line that compares edi to 0, break there
    while edi != 0:                                 # continue until edi reaches 0
        gdb.execute('c', False, True)
        edi = int(re.findall(r'\d+', gdb.execute("print *$edi", False, True))[-1])
    gdb.execute('break *0xf7ffb02e', False, True)   # break to the final function, during which the string is decoded char by char
    gdb.execute('break *main + 193', False, True)   # break at main return, prevent gdb failure
    gdb.execute('c', False, True)
    while True:                                     # Run the loops and read the char at register dl with every iteration
        response= gdb.execute('print $pc', False, True)
        stringss = response.split()
        pcstring = stringss[-1].strip()
        char = gdb.execute('print (char)$bl', False, True)
        decoded = decoded + char[-3]
        if pcstring != '0xf7ffb02e':                # If the loop is dome, return the decoded String
            return decoded
        gdb.execute("c", False, True)               # Next loop iteration


if __name__ == '__main__':
    decodedString = ''
    decodedString = extractIt(decodedString)
    print(decodedString)                            # Print unsanitized solution
    gdb.execute("q", False, True)                   # Quit gdb after the extraction


