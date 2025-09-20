import java.io.*;
import java.util.*;

// ===================== Book Class =====================
class Book implements Serializable, Comparable<Book> {
    private int id;
    private String title;
    private String author;
    private boolean isIssued;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isIssued = false;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isIssued() { return isIssued; }

    public void setIssued(boolean issued) { this.isIssued = issued; }

    @Override
    public int compareTo(Book other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public String toString() {
        return String.format("| %-5d | %-20s | %-15s | %-10s |",
                id, title, author, (isIssued ? "Issued" : "Available"));
    }
}

// ===================== Member Class =====================
class Member implements Serializable {
    private int id;
    private String name;
    private int fine;
    private int[] issuedBooks;
    private int countIssued;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
        this.fine = 0;
        this.issuedBooks = new int[5];
        this.countIssued = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getFine() { return fine; }

    public void addFine(int amount) { fine += amount; }

    public void issueBook(int bookId) {
        if (countIssued < issuedBooks.length) {
            issuedBooks[countIssued++] = bookId;
        }
    }

    public void returnBook(int bookId) {
        for (int i = 0; i < countIssued; i++) {
            if (issuedBooks[i] == bookId) {
                issuedBooks[i] = issuedBooks[countIssued - 1];
                issuedBooks[countIssued - 1] = 0;
                countIssued--;
                break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("| %-5d | %-15s | Fine: %-5d |", id, name, fine);
    }
}

// ===================== Library Class =====================
class Library {
    private HashMap<Integer, Book> books;
    private HashMap<Integer, Member> members;
    private final String fileName = "libraryData.dat";

    public Library() {
        books = new HashMap<>();
        members = new HashMap<>();
        loadData();
    }

    // ---- Book Operations ----
    public boolean addBook(Book b) {
        if (books.containsKey(b.getId())) return false;
        books.put(b.getId(), b);
        saveData();
        return true;
    }

    public boolean removeBook(int id) {
        if (!books.containsKey(id)) return false;
        books.remove(id);
        saveData();
        return true;
    }

    public void searchBookByTitle(String title) {
        List<Book> list = new ArrayList<>(books.values());
        Collections.sort(list);
        int index = Collections.binarySearch(list, new Book(0, title, ""), Comparator.naturalOrder());
        if (index >= 0) {
            System.out.println(list.get(index));
        } else {
            System.out.println("| Book not found |");
        }
    }

    public void listBooks() {
        System.out.println("| ID    | Title                | Author          | Status     |");
        System.out.println("---------------------------------------------------------------");
        for (Book b : books.values()) System.out.println(b);
    }

    // ---- Member Operations ----
    public boolean addMember(Member m) {
        if (members.containsKey(m.getId())) return false;
        members.put(m.getId(), m);
        saveData();
        return true;
    }

    public void listMembers() {
        System.out.println("| ID    | Name            | Fine       |");
        System.out.println("---------------------------------------");
        for (Member m : members.values()) System.out.println(m);
    }

    // ---- Transactions ----
    public void issueBook(int bookId, int memberId) {
        Book b = books.get(bookId);
        Member m = members.get(memberId);
        if (b == null || m == null || b.isIssued()) {
            System.out.println("| Cannot issue book |");
            return;
        }
        b.setIssued(true);
        m.issueBook(bookId);
        saveData();
        System.out.println("| Book issued successfully |");
    }

    public void returnBook(int bookId, int memberId, int lateDays) {
        Book b = books.get(bookId);
        Member m = members.get(memberId);
        if (b == null || m == null || !b.isIssued()) {
            System.out.println("| Cannot return book |");
            return;
        }
        b.setIssued(false);
        m.returnBook(bookId);
        if (lateDays > 0) m.addFine(lateDays * 10);
        saveData();
        System.out.println("| Book returned successfully |");
    }

    // ---- Persistence ----
    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(books);
            out.writeObject(members);
        } catch (Exception e) {
            System.out.println("| Error saving data |");
        }
    }

    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            books = (HashMap<Integer, Book>) in.readObject();
            members = (HashMap<Integer, Member>) in.readObject();
        } catch (Exception e) {
            books = new HashMap<>();
            members = new HashMap<>();
        }
    }
}

// ===================== Main Menu =====================
public class LibraryManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Library library = new Library();
        boolean running = true;

        while (running) {
            System.out.println("\n================= LIBRARY MENU =================");
            System.out.println("1. Add Book");
            System.out.println("2. Add Member");
            System.out.println("3. List Books");
            System.out.println("4. List Members");
            System.out.println("5. Search Book by Title");
            System.out.println("6. Issue Book");
            System.out.println("7. Return Book");
            System.out.println("8. Exit");
            System.out.println("===============================================");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Book ID: ");
                    int bid = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Title: ");
                    String title = sc.nextLine();
                    System.out.print("Enter Author: ");
                    String author = sc.nextLine();
                    if (library.addBook(new Book(bid, title, author)))
                        System.out.println("| Book added successfully |");
                    else
                        System.out.println("| Duplicate Book ID |");
                    break;

                case 2:
                    System.out.print("Enter Member ID: ");
                    int mid = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    if (library.addMember(new Member(mid, name)))
                        System.out.println("| Member added successfully |");
                    else
                        System.out.println("| Duplicate Member ID |");
                    break;

                case 3:
                    library.listBooks();
                    break;

                case 4:
                    library.listMembers();
                    break;

                case 5:
                    System.out.print("Enter Book Title: ");
                    String searchTitle = sc.nextLine();
                    library.searchBookByTitle(searchTitle);
                    break;

                case 6:
                    System.out.print("Enter Book ID: ");
                    int ibid = sc.nextInt();
                    System.out.print("Enter Member ID: ");
                    int imid = sc.nextInt();
                    library.issueBook(ibid, imid);
                    break;

                case 7:
                    System.out.print("Enter Book ID: ");
                    int rbid = sc.nextInt();
                    System.out.print("Enter Member ID: ");
                    int rmid = sc.nextInt();
                    System.out.print("Enter Late Days: ");
                    int late = sc.nextInt();
                    library.returnBook(rbid, rmid, late);
                    break;

                case 8:
                    System.out.println("| Exiting Library System |");
                    running = false;
                    break;

                default:
                    System.out.println("| Invalid option |");
            }
        }
        sc.close();
    }
}
