# Notebook App

We got a new app that allows you to take notes.
There are two kinds of notes: simple ones and printable ones.
The app is still work in progress so there might still be some bugs.

Find the bug and exploit it to print the flag.
As always, describe what you are doing with technical details in comments so that we see that you fully understood the vulnerability and the exploit.

Once again, edit the provided `solution` template and explain your approach with meaningful comments!

Hints:

- What happens if you set the printing function to `puts()` and then have heap allocation with the same size before that?
- Have a closer look at the struct `printable_note`

Your solution should execute like this:

```
./solution
FLAG{some letters}
```
