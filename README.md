# Overview

CoffeeTextEdit is a simple command line text editor That I made in Java. It uses an external library, Lanterna, to manipulate a terminal to use as the gui. It can be used to open, edit and save a text file. 

Because I developed this on Windows, which unfortunaltely does not have a compatible terminal, a terminal emulator is used. If you think you may have a compatible terminal from which you want to run CTE see the program usage section.

I wanted to make my own command line text editor ever since learning about old school text editors. I thought it would be a good project to push myself to practice problem solving and learn a new language. Plus it could eventually become a tool I'll actually use.

[CoffeeText Demo](https://youtu.be/eIsBe_F7zCg)

## Program Usage
### Enter these comands from directory with src, lib, and bin folders to compile and run source code from command line

#### Compile source

javac -cp lib\lanterna-3.2.0-alpha1.jar -d bin src\App.java

#### Run 

java -cp bin;lib\lanterna-3.2.0-alpha1.jar App example.txt

- replace "example.txt" with the file you want to edit

### General usage notes
CTE currently doesn't have properly defined behavior for typing lines longer (horizontally) than the window width or for having more lines than fit in the window (vertically). Expanding the window gives you more space but know that you are limited in this capacity.

### Using your own terminal
Some terminals like Windows Command Prompt are not compatible with CTE. CTE uses a terminal emulator bundled with Lanterna. If you think your terminal is compatible or just want to test it, it's as simple as changing one line of code.

1. In "App.java" search for `static class TerminalOperator` and look under the meithod `createTerminal`
2. Change this line `Terminal terminal = defaultTerminalFactory.createTerminalEmulator();` to `Terminal terminal = defaultTerminalFactory.createTerminal();`
3. Save, compile, run

# Development Environment

I wrote my code using vs code and several extentions for Java. 

I used the Free and Open Source implementation of the Java development kit, OpenJDK to test and compile the source code

I used a curses-like library, Lanterna which I am not the author of. See below for a link to its github page.
# Useful Websites

- [Lanterna](https://github.com/mabe02/lanterna/tree/master)
- [JDK documentation](https://docs.oracle.com/en/java/javase/22/docs/api/index.html)

# Future Work

- Add support for longer, multi-page files
- Define better behavior for long lines of text
- Add functionality for additional keys (tab, pg up, pg down)
- Add additional functionality such as copy/paste, un/redo, etc.
