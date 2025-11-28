import java.util.Scanner;

public class CLI
{
    private FlashcardSet set;
    public CLI()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Flashcard101 CLI\n");

        set = new FlashcardSet("Cards/cards.csv");
        set.displayTerm(0);
        // set.remove("twinkle");
        set.displayInfo("amreen");
        set.saveData("Cards/cards.csv");
        
    }

    public static void main(String[] args)
    {
        new CLI();
    }
}
