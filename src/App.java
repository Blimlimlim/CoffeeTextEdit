// Java imports
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
// Lanterna imports
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
// import com.googlecode.lanterna.TextColor;
// import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;


public class App {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Needs a file name to run");
            return;
        }

        File textFile = new File(args[0]); // create File object using the path and name argument given when running the program
        // File textFile = new File("mytext.txt");
        Terminal terminal = null;
        // Create an ArrayList of ArrayLists of Characters
        ArrayList<ArrayList<Character>> textBuffer = new ArrayList<>();

        if (textFile.exists()) { // continue if a file with textfile's path exists

            // Copy file contents into our ArrayList
            FileOperator.readFileContents(textFile, textBuffer);

            // Create terminal
            terminal = TerminalOperator.createTerminal();

            // Enter private mode
            terminal.enterPrivateMode();

            final TextGraphics textGraphics = terminal.newTextGraphics();

            // Change Colors

            //Feel free to use these instead or whatever if your terminal supports it. Don't forget to uncomment imports
            // TextColor.RGB backgroundColor = new TextColor.RGB(36, 20, 1);
            // TextColor.RGB foregroundColor = new TextColor.RGB(255, 253, 208);

            // textGraphics.setBackgroundColor(backgroundColor);
            // textGraphics.setForegroundColor(foregroundColor);
            // terminal.setBackgroundColor(backgroundColor);
            // terminal.setForegroundColor(foregroundColor);
            
           
            // save the cursor's current position
            TerminalPosition cursorPosition = terminal.getCursorPosition();
        
            // Draw a big rectangle to fill up the screen with desired colors
            TerminalSize backdropSize = new TerminalSize(500, 500);
            textGraphics.fillRectangle(cursorPosition.withRow(0).withColumn(0), backdropSize, ' ');
            
            terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0));
            cursorPosition = terminal.getCursorPosition();
            // Display the buffer's text in the terminal
            TerminalOperator.displayText(terminal, textBuffer);

            // Hold the position of the cursor in the terminal
            cursorPosition = terminal.getCursorPosition();

            // Move cursor position to top left
            terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0));
            cursorPosition = terminal.getCursorPosition(); // save current position
            terminal.flush(); // make changes visible

            int previousRowSize; // helpful later for dealing with some transitions between rows
            int upDownColumnSaver = cursorPosition.getColumn(); // used for returning cursor to column when moving from a longer to shorter to longer line. Set with keypresses that move cursor except up/down arrows. Used in up/down arrows to control cursor behavior
            
            // Wait for a key to be pressed. readInput pauses execution until a key is pressed
            KeyStroke keyStroke = terminal.readInput();

            /*
             * Enter main loop to handle key presses. Takes another input after handling
             * press f1 to save your changes
             * press esc to exit program
             */
            while (keyStroke.getKeyType() != KeyType.Escape) {

                if      (keyStroke.getKeyType() == KeyType.Character)  { // type a character and move cursor forward
                    // Insert character into text buffer at the cursor position indexes
                    textBuffer.get(cursorPosition.getRow()).add(cursorPosition.getColumn(), keyStroke.getCharacter());
                    
                    // Move cursor right
                    terminal.setCursorPosition(cursorPosition.withRelativeColumn(1));
                    cursorPosition = terminal.getCursorPosition(); // remember new position
                    upDownColumnSaver = cursorPosition.getColumn();
                    // refresh terminal display to show new contents
                    // terminal.clearScreen(); // clear terminal
                    textGraphics.fillRectangle(cursorPosition.withRow(0).withColumn(0), backdropSize, ' '); //redraw backdrop

                    terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0)); // put cursor at start
                    TerminalOperator.displayText(terminal, textBuffer); // display the textBuffer
                    terminal.setCursorPosition(cursorPosition); // move cursor back to saved position
                }
                
                else if (keyStroke.getKeyType() == KeyType.Backspace)  { // remove previous character and move cursor backwards
                    // if cursor is not at beginning of text buffer (Backspace should do nothing at the beginning)
                    if ((cursorPosition.getRow() != 0) || (cursorPosition.getColumn() != 0)) {
                        // if the cursor is at the beginning of the current row
                        if (cursorPosition.getColumn() == 0) {
                            previousRowSize = textBuffer.get(cursorPosition.getRow() - 1).size();
                            // remove the last character (newline) from the end of previous column
                            textBuffer.get(cursorPosition.getRow() - 1).remove(previousRowSize -1);
                            // save cursor position to end of previous row
                            terminal.setCursorPosition(cursorPosition.withColumn(previousRowSize - 1).withRelativeRow(-1));
                            cursorPosition = terminal.getCursorPosition();
                            upDownColumnSaver = cursorPosition.getColumn();
                            // combine the two rows by adding the second to the first
                            textBuffer.get(cursorPosition.getRow()).addAll(textBuffer.get(cursorPosition.getRow() + 1));
                            // remove the second (no longer current) row
                            textBuffer.remove(cursorPosition.getRow() + 1);
                        }
                        else {
                            // Remove chacter from text buffer at cursor position indexes (one prior column)
                            textBuffer.get(cursorPosition.getRow()).remove(cursorPosition.getColumn()-1);
                            // Move cursor left
                            terminal.setCursorPosition(cursorPosition.withRelativeColumn(-1));
                            cursorPosition = terminal.getCursorPosition(); // remember new position
                            upDownColumnSaver = cursorPosition.getColumn();
                        }
                        // refresh terminal display to show new contents
                        // terminal.clearScreen(); // clear terminal
                        textGraphics.fillRectangle(cursorPosition.withRow(0).withColumn(0), backdropSize, ' '); // redraw backdrop
                        terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0)); // put cursor at start
                        TerminalOperator.displayText(terminal, textBuffer); // display the textBuffer
                        terminal.setCursorPosition(cursorPosition); // move cursor back to saved position
                    }
                }
                
                else if (keyStroke.getKeyType() == KeyType.ArrowRight) { // move cursor right
                    // if the cursor is not at the end of the row
                    if (cursorPosition.getColumn() < textBuffer.get(cursorPosition.getRow()).size() - 1) {
                        terminal.setCursorPosition(cursorPosition.withRelativeColumn(1)); // move cursor right
                    }
                    // if the cursor is at the bottom row (and end of row)
                    else if (cursorPosition.getRow() >= textBuffer.size() - 1) {
                        // if the cursor is at the last item in bottom row
                        if (cursorPosition.getColumn() < textBuffer.get(cursorPosition.getRow()).size()) {
                            // let cursor move one space past the end
                            terminal.setCursorPosition(cursorPosition.withRelativeColumn(1)); // move cursor right
                        }
                        else { // cursor is past the last item in bottom row
                            // do nothing
                        }
                    }
                    else {
                        // move cursor to start of next row
                        terminal.setCursorPosition(cursorPosition.withColumn(0).withRelativeRow(1));
                    }
                    cursorPosition = terminal.getCursorPosition(); // remember new position
                    upDownColumnSaver = cursorPosition.getColumn();
                }
                
                else if (keyStroke.getKeyType() == KeyType.ArrowLeft)  { // move cursor left
                    // if the cursor is not at the start of the row
                    if (cursorPosition.getColumn() != 0) {
                        terminal.setCursorPosition(cursorPosition.withRelativeColumn(-1)); // move cursor left
                    }
                    // if the cursor is at the top row (and start of row)
                    else if (cursorPosition.getRow() <= 0) {
                        // do nothing
                    }
                    else {
                        previousRowSize = textBuffer.get(cursorPosition.getRow() - 1).size();
                        // move cursor to end of previous row
                        terminal.setCursorPosition(cursorPosition.withColumn(previousRowSize - 1).withRelativeRow(-1));
                    }
                    cursorPosition = terminal.getCursorPosition(); // remember new position
                    upDownColumnSaver = cursorPosition.getColumn();
                }
                
                else if (keyStroke.getKeyType() == KeyType.ArrowDown)  { // move cursor down a row
                    // if the current row is not the last row
                    if (cursorPosition.getRow() < textBuffer.size() - 1) {
                        terminal.setCursorPosition(cursorPosition.withRelativeRow(1).withColumn(upDownColumnSaver)); // move cursor down
                    }
                    else { // otherwise move cursor to end of row
                        terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(textBuffer.size() - 1).size()));
                    }
                    cursorPosition = terminal.getCursorPosition(); // remember new position
                    // if new position is higher than the last character in the row
                    if (cursorPosition.getColumn() >= textBuffer.get(cursorPosition.getRow()).size()) {
                        // if not last row
                        if (cursorPosition.getRow() < textBuffer.size() - 1) {
                            // move cursor to end of row
                            terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size() - 1));
                        }
                        else {
                            // move cursor to end of row plus 1
                            terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size()));
                        }
                        cursorPosition = terminal.getCursorPosition();
                    }
                }
                
                else if (keyStroke.getKeyType() == KeyType.ArrowUp)    { // move cursor up a row
                    // if the current row is not the first row
                    if (cursorPosition.getRow() > 0) {
                        terminal.setCursorPosition(cursorPosition.withRelativeRow(-1).withColumn(upDownColumnSaver)); // move cursor up
                    }
                    else { // otherwise move cursor to beginning of row
                        terminal.setCursorPosition(cursorPosition.withColumn(0));
                    }
                    cursorPosition = terminal.getCursorPosition(); // remember new position
                    // if new position is higher than the last character in the row
                    if (cursorPosition.getColumn() >= textBuffer.get(cursorPosition.getRow()).size()) {
                        // if not last line
                        if (cursorPosition.getRow() < textBuffer.size() - 1) {
                            // move cursor to end of row
                            terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size() - 1));
                        }
                        else {
                            // move cursor to end of row  plus 1
                            terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size()));
                        }
                        cursorPosition = terminal.getCursorPosition();
                    }
                }
                
                else if (keyStroke.getKeyType() == KeyType.Enter)      { // enter a newline character
                    // add a newline character to spot in buffer
                    textBuffer.get(cursorPosition.getRow()).add(cursorPosition.getColumn(), '\n');

                    // Move row contents after newline to new sublist and insert the new sublist after the old
                    ArrayList<Character> newRow = new ArrayList<>();
                    // use addAll to add the old rows contents starting at one after the newline's index
                    ArrayList<Character> oldRow = textBuffer.get(cursorPosition.getRow());
                    newRow.addAll(oldRow.subList(cursorPosition.getColumn() + 1, oldRow.size()));
                    // remove those elements from the old row
                    oldRow.subList(cursorPosition.getColumn() + 1, oldRow.size()).clear();
                    // insert sublist at original row's index + 1
                    textBuffer.add(cursorPosition.getRow() + 1, newRow);
                    // Move cursor to start of the new row (next from current position)
                    terminal.setCursorPosition(cursorPosition.withColumn(0).withRelativeRow(1));
                    cursorPosition = terminal.getCursorPosition();
                    upDownColumnSaver = cursorPosition.getColumn();
                    // refresh terminal contents
                    // terminal.clearScreen(); // clear terminal
                    textGraphics.fillRectangle(cursorPosition.withRow(0).withColumn(0), backdropSize, ' '); // redraw backdrop
                    terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0)); // put cursor at start
                    TerminalOperator.displayText(terminal, textBuffer); // display the textBuffer
                    terminal.setCursorPosition(cursorPosition); // move cursor back to saved position
                }
                
                else if (keyStroke.getKeyType() == KeyType.Tab)        {} // TODO
                
                else if (keyStroke.getKeyType() == KeyType.Delete)     { // Remove forward characters
                    // if not at end of last row (very end)
                    if (cursorPosition.getColumn() != textBuffer.get(cursorPosition.getRow()).size()) { // at end of last row, cursor is 1 index out of bounds
                        // remove character under cursor
                        textBuffer.get(cursorPosition.getRow()).remove(cursorPosition.getColumn());
                        // if not on last row and if cursor was at last index of row (one higher because of deleted newline)
                        if ((cursorPosition.getRow() < textBuffer.size()-1) && (cursorPosition.getColumn() == textBuffer.get(cursorPosition.getRow()).size())) {
                            // add next row contents to current row
                            textBuffer.get(cursorPosition.getRow()).addAll(textBuffer.get(cursorPosition.getRow()+1));
                            // remove next line
                            textBuffer.remove(cursorPosition.getRow()+1);
                        }
                        // refresh terminal display to show new contents
                        // terminal.clearScreen(); // clear terminal
                        textGraphics.fillRectangle(cursorPosition.withRow(0).withColumn(0), backdropSize, ' '); // redraw backdrop
                        terminal.setCursorPosition(cursorPosition.withColumn(0).withRow(0)); // put cursor at start
                        TerminalOperator.displayText(terminal, textBuffer); // display the textBuffer
                        terminal.setCursorPosition(cursorPosition); // move cursor back to saved position
                    }
                }

                else if (keyStroke.getKeyType() == KeyType.Home)       { // move cursor to start of line 
                    terminal.setCursorPosition(cursorPosition.withColumn(0));
                    cursorPosition = terminal.getCursorPosition();
                    upDownColumnSaver = 0;
                }

                else if (keyStroke.getKeyType() == KeyType.End)        { // move cursor to end of row
                    // if not last row
                    if (cursorPosition.getRow() < textBuffer.size() - 1) {
                    // Move cursor to end of line
                    terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size() - 1));
                    }
                    else {
                        terminal.setCursorPosition(cursorPosition.withColumn(textBuffer.get(cursorPosition.getRow()).size()));

                    }
                    cursorPosition = terminal.getCursorPosition();
                    upDownColumnSaver = cursorPosition.getColumn();
                }

                else if (keyStroke.getKeyType() == KeyType.PageUp)     {} // TODO

                else if (keyStroke.getKeyType() == KeyType.PageDown)   {} // TODO

                else if (keyStroke.getKeyType() == KeyType.F1)         {  // save file TODO add save-as
                    FileOperator.writeFileContents(textFile, textBuffer);
                }

                
                
                // TODO add condition for tab.
                // TODO 
                terminal.flush(); // make changes to terminal screen visible
                keyStroke = terminal.readInput(); // read next input
            }
        }
        else {
            System.err.println(args[0] +" was not found");
        }

        // Close terminal (also exits private mode)
        if (terminal != null) {
            terminal.close();
        }
    }

    /**
     * A class for some repetitive terminal related functions
     */
    static class TerminalOperator {
    
        static Terminal createTerminal() throws IOException {
            DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
            Terminal terminal = defaultTerminalFactory.createTerminalEmulator();
            return terminal;
        }
        
        static void displayText(Terminal terminal, ArrayList<ArrayList<Character>> listOfCharList) throws IOException {
            // loop through main list of Character lists
            for (ArrayList<Character> subList : listOfCharList) {
                // loop through sublist list of characters in current row
                for (Character character : subList) {
                    // System.out.println((int)character);
                    terminal.putCharacter(character.charValue());
                }
            }
            terminal.flush();
        }    
    }


/**
 * A class for file handling methods
 * Static class doesn't need to be instanciated
 */
    static class FileOperator {        

        /**
        * Makes a list of sublists of characters. Each sublist represents a row in the text file. 
        * Newline characters go at the end of a sublist and mark when a new sublist should be created 
        */
        static void readFileContents (File file, ArrayList<ArrayList<Character>> textBuffer) throws Exception {
            BufferedReader Reader = new BufferedReader(new FileReader(file));

            int ch = Reader.read(); // read first char
            if (ch != -1) // make sure we have not already reached the end of the file (empty file)
            {
                // create first sublist
                ArrayList<Character> currentSubList = new ArrayList<>();
                textBuffer.add(currentSubList); // add sublist to main list
                do { // handle it

                    if (ch != '\r') { // not carriage return. The loop will just skip these so that \r\n in some text files will just be \n
                        if (ch != '\n') { // not newline
                        // add to sublist
                        currentSubList.add((char)ch);
                        }
                        else { // is newline
                            currentSubList.add((char)ch);
                            currentSubList = new ArrayList<>(); // create new sublist
                            textBuffer.add(currentSubList); // add it to main list
                        }
                    }
                }
                while ((ch = Reader.read()) != -1); // read next char (as int) and compare it to -1 which marks the end
            }
                
            Reader.close();
        }
        
        /** Saves an ArrayList of ArrayLists of Characters to a file */
        static void writeFileContents (File file, ArrayList<ArrayList<Character>> textBuffer) throws IOException {
            BufferedWriter Writer = new BufferedWriter(new FileWriter(file));

            // visit each sub list
            for (ArrayList<Character> sublist : textBuffer) {
                // visit each Character in the sublist
                for (Character character : sublist) {
                    // write character to end of file
                    Writer.append(character);
                }
            }
            // flush contents and close writer
            Writer.flush();
            Writer.close();
        }
    }

}
