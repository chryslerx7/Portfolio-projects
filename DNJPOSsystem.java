
import java.util.ArrayList;
import java.util.Scanner;

public class DNJPOSsystem {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        boolean loggedIn = false;

        String username[] = {"DNJCrew01", "DNJCrew02", "DNJCrew03", "Manager"};
        String password[] = {"IloveCoffee4ever"};

        String php = "PHP";

        String currentUser = "";

        // LOGIN LOOP
        while (!loggedIn) {

            System.out.println("------------------------------");
            System.out.println("----------SYSTEM--------------");
            System.out.println("------------------------------");
            System.out.println("--------DNJ COFFEE------------");
            System.out.println("------------------------------");

            System.out.println("Log In to take orders");
            System.out.print("Enter Username: ");
            String user = s.nextLine();
            System.out.print("Enter Password: ");
            String pass = s.nextLine();

            for (int i = 0; i < username.length; i++) {
                if (user.equals(username[i]) && pass.equals(password[0])) {
                    loggedIn = true;
                    currentUser = username[i];
                    break;
                }
            }

            if (!loggedIn) {
                System.out.println("Incorrect Username or Password! Please Try Again....");
            } else {
                System.out.println("Welcome " + currentUser + " ");
            }

        }

        boolean ordering = true;

        ArrayList<Integer> coffeeIndexes = new ArrayList<>();

        ArrayList<String> orderList = new ArrayList<>();
        ArrayList<Float> priceList = new ArrayList<>();
        float totalPrice = 0;

        // Menu data
        String[][] Brewed1 = {
            {"Brewed Plain", "60", "70", "80"},
            {"Hazelnut", "90", "100", "110"},
            {"Vietnamese", "70", "80", "90"}
        };

        String[][] Espresso = {
            {"Americano", "110", "120", "130"},
            {"Spanish Latte", "130", "140", "150"},
            {"Caramel Macchiato", "130", "140", "150"},
            {"Salted Caramel", "130", "140", "150"},
            {"Mocha", "130", "140", "150"},
            {"White Mocha", "130", "140", "150"},
            {"Cafe Latte", "130", "140", "150"}
        };

        String[][] Matcha = {
            {"Matcha Latte", "105", "115", "130"}
        };

        String[][] Pastry = {
            {"Carbonara", "140"},
            {"Ensaymada", "40"},
            {"Nachos", "160"},
            {"Lasagna", "150"},
            {"Overload Fries", "150"},
            {"Chocolate Brownies", "50"},
            {"Banana Bread", "50"},
            {"Carrot Bread", "60"}
        };

        String[][] Refreshers = {
            {"Apple Berry", "50"},
            {"Mogu-Mogu", "50"},
            {"Lipton", "50"},
            {"Lychee", "50"}
        };

        String[][] Addons = {
            {"syrup Vanilla", "20"},
            {"Syrup Caramel", "20"},
            {"syrup Hazelnut", "20"},
            {"Syrup Chocolate", "20"},
            {"Milk", "20"},
            {"Espresso Shot", "20"},
            {"Whipped Cream", "20"}
        };

        while (ordering) {
            try {
                System.out.println("------------------------------");
                System.out.println("----------SHOW MENU-----------");
                System.out.println("[1] Coffee");
                System.out.println("[2] Pastry");
                System.out.println("[3] Refresher");
                System.out.println("[4] Checkout");
                System.out.println("[5] Logout");

                System.out.print("Enter choice: ");
                int choice = s.nextInt();
                s.nextLine();

                switch (choice) {

                    case 1: // Coffee submenu
                        boolean submenu = true;
                        while (submenu) {
                            System.out.println("------------------------------");
                            System.out.println("Choose Type of Coffee");
                            System.out.println("[A] Brewed");
                            System.out.println("[B] Espresso");
                            System.out.println("[C] Matcha");
                            System.out.println("[D] Addons");
                            System.out.println("[E] Back to Main Menu");
                            System.out.print("Enter choice: "); //Asks the user for input
                            String tmp = s.nextLine().trim();
                            if (tmp.isEmpty()) {
                                System.out.println("Invalid input. Try again.");
                                continue;
                            }
                            char type = Character.toUpperCase(tmp.charAt(0));

                            switch (type) {
                                case 'A': // Brewed
                                    System.out.println("[Brewed]");
                                    System.out.println("Product\t\t8oz\t12oz\t16oz");
                                    for (int i = 0; i < Brewed1.length; i++) {
                                        System.out.print((i + 1) + ". " + Brewed1[i][0] + "\t");
                                        System.out.print(php + Brewed1[i][1] + "\t");
                                        System.out.print(php + Brewed1[i][2] + "\t");
                                        System.out.print(php + Brewed1[i][3] + "\t");
                                        System.out.println();
                                    }
                                    //Takes the order (Product)
                                    System.out.print("Enter the product name (e.g., Hazelnut) or 'C' to cancel: ");
                                    String itemNameBrewed = s.nextLine().trim();
                                    if (itemNameBrewed.equalsIgnoreCase("C")) {
                                        System.out.println("Cancelled selection.");
                                        break;
                                    }
                                    //Ask for Hot or Iced
                                    System.out.print("Hot or Iced? (H/I): ");
                                    String temperature = s.nextLine().trim().toLowerCase();
                                    if (temperature.equalsIgnoreCase("H")) {
                                        temperature = "Hot";
                                    } else if (temperature.equalsIgnoreCase("I")) {
                                        temperature = "Iced";
                                    } else {
                                        System.out.println("Invalid Input!");
                                        break;
                                    }
                                    //Order size
                                    boolean foundB = false;
                                    for (int i = 0; i < Brewed1.length; i++) {
                                        if (itemNameBrewed.equalsIgnoreCase(Brewed1[i][0])) {
                                            foundB = true;
                                            System.out.print("Enter size (8 / 12 / 16): ");
                                            int size = -1;
                                            try {
                                                size = Integer.parseInt(s.nextLine().trim());
                                            } catch (NumberFormatException nfe) {
                                                System.out.println("Invalid size input.");
                                                break;
                                            }
                                            int sizeIndex = 0;
                                            if (size == 8) {
                                                sizeIndex = 1;
                                            } else if (size == 12) {
                                                sizeIndex = 2;
                                            } else if (size == 16) {
                                                sizeIndex = 3;
                                            } else {
                                                System.out.println("Invalid size!");
                                                break;
                                            }
                                            //Order Quantity
                                            int quantity = 0;
                                            System.out.print("Enter quantity: ");
                                            String quantityInput = s.nextLine().trim(); //Takes the order quantity

                                            if (quantityInput.matches("\\d+")) { //Input must be digits only
                                                quantity = Integer.parseInt(quantityInput); //Since the menu is string, parse will read the numbers as digits
                                                if (quantity <= 0) {
                                                    System.out.println("Quantity must be positive.");
                                                    break;
                                                }
                                            } else {
                                                System.out.println("Invalid quantity input.");
                                                break;
                                            }
                                            //Order added to cart
                                            float price = Float.parseFloat(Brewed1[i][sizeIndex]);
                                            orderList.add(Brewed1[i][0] + " (" + temperature + ", " + size + "oz)  - " + quantity);
                                            priceList.add(price * quantity);
                                            totalPrice += price * quantity;
                                            coffeeIndexes.add(orderList.size() - 1);
                                            System.out.println(quantity + " - " + Brewed1[i][0] + " (" + temperature + ", " + size + "oz) added to order. PHP" + price * quantity);
                                            submenu = false; //back to main menu
                                            break;
                                        }
                                    }
                                    if (!foundB) {
                                        System.out.println("Item not found!");
                                    }
                                    break;

                                case 'B': // Espresso
                                    System.out.println("[Espresso]");
                                    System.out.println("Product\t\t\t\t8oz\t12oz\t16oz");
                                    for (int i = 0; i < Espresso.length; i++) {
                                        System.out.print((i + 1) + ". " + Espresso[i][0] + "\t\t");

                                        if (((i + 1) + ". " + Espresso[i][0]).length() < 16) {
                                            System.out.print("\t");
                                        } else {
                                            System.out.print("");
                                        }
                                        System.out.print(php + Espresso[i][1] + "\t");
                                        System.out.print(php + Espresso[i][2] + "\t");
                                        System.out.print(php + Espresso[i][3] + "\t");
                                        System.out.println();
                                    }
                                    //Takes the order (Product)
                                    System.out.print("Enter the product name or 'C' to cancel: ");
                                    String itemNameEsp = s.nextLine().trim();
                                    if (itemNameEsp.equalsIgnoreCase("C")) {
                                        System.out.println("Cancelled selection.");
                                        break;
                                    }
                                    //Ask for Hot or Iced
                                    System.out.print("Hot or Iced? (H/I): ");
                                    temperature = s.nextLine().trim().toLowerCase();
                                    if (temperature.equalsIgnoreCase("H")) {
                                        temperature = "Hot";
                                    } else if (temperature.equalsIgnoreCase("I")) {
                                        temperature = "Iced";
                                    } else {
                                        System.out.println("Invalid Input!");
                                        break;
                                    }
                                    //Order size
                                    boolean foundE = false;
                                    for (int i = 0; i < Espresso.length; i++) {
                                        if (itemNameEsp.equalsIgnoreCase(Espresso[i][0])) {
                                            foundE = true;
                                            System.out.print("Enter size (8 / 12 / 16): ");
                                            int size = -1;
                                            try {
                                                size = Integer.parseInt(s.nextLine().trim());
                                            } catch (NumberFormatException nfe) {
                                                System.out.println("Invalid size input.");
                                                break;
                                            }
                                            int sizeIndex = 0;
                                            if (size == 8) {
                                                sizeIndex = 1;
                                            } else if (size == 12) {
                                                sizeIndex = 2;
                                            } else if (size == 16) {
                                                sizeIndex = 3;
                                            } else {
                                                System.out.println("Invalid size!");
                                                break;
                                            }
                                            //Order Quantity
                                            int quantity = 0;
                                            System.out.print("Enter quantity: ");
                                            String quantityInput = s.nextLine().trim(); //Takes the order quantity

                                            if (quantityInput.matches("\\d+")) { //Input must be digits only
                                                quantity = Integer.parseInt(quantityInput); //Since the menu is string, parse will read the numbers as digits
                                                if (quantity <= 0) {
                                                    System.out.println("Quantity must be positive.");
                                                    break;
                                                }
                                            } else {
                                                System.out.println("Invalid quantity input.");
                                                break;
                                            }
                                            float price = Float.parseFloat(Espresso[i][sizeIndex]);
                                            orderList.add(Espresso[i][0] + " (" + temperature + ", " + size + "oz)  - " + quantity);
                                            priceList.add(price * quantity);
                                            totalPrice += price * quantity;
                                            coffeeIndexes.add(orderList.size() - 1);
                                            System.out.println(quantity + " - " + Espresso[i][0] + " (" + temperature + ", " + size + "oz) added to order. PHP" + price * quantity);
                                            submenu = false; //Back to main menu
                                            break;
                                        }
                                    }
                                    if (!foundE) {
                                        System.out.println("Item not found!");
                                    }
                                    break;

                                case 'C': // Matcha
                                    System.out.println("[Matcha]");
                                    System.out.println("Product\t\t8oz\t12oz\t16oz");
                                    for (int i = 0; i < Matcha.length; i++) {
                                        System.out.print((i + 1) + ". " + Matcha[i][0] + "\t");
                                        System.out.print(php + Matcha[i][1] + "\t");
                                        System.out.print(php + Matcha[i][2] + "\t");
                                        System.out.print(php + Matcha[i][3] + "\t");
                                        System.out.println();
                                    }
                                    //Takes the order (Product)
                                    System.out.print("Enter the product name or 'C' to cancel: ");
                                    String itemNameM = s.nextLine().trim();
                                    if (itemNameM.equalsIgnoreCase("C")) {
                                        System.out.println("Cancelled selection.");
                                        break;
                                    }
                                    //Ask for Hot or Iced
                                    System.out.print("Hot or Iced? (H/I) ");
                                    temperature = s.nextLine().trim().toLowerCase();
                                    if (temperature.equalsIgnoreCase("H")) {
                                        temperature = "Hot";
                                    } else if (temperature.equalsIgnoreCase("I")) {
                                        temperature = "Iced";
                                    } else {
                                        System.out.println("Invalid Input!");
                                        break;
                                    }
                                    //Order size
                                    boolean foundM = false;
                                    for (int i = 0; i < Matcha.length; i++) {
                                        if (itemNameM.equalsIgnoreCase(Matcha[i][0])) {
                                            foundM = true;
                                            System.out.print("Enter size (8 / 12 / 16): ");
                                            int size = -1;
                                            try {
                                                size = Integer.parseInt(s.nextLine().trim());
                                            } catch (NumberFormatException nfe) {
                                                System.out.println("Invalid size input.");
                                                break;
                                            }
                                            int sizeIndex = 0;
                                            if (size == 8) {
                                                sizeIndex = 1;
                                            } else if (size == 12) {
                                                sizeIndex = 2;
                                            } else if (size == 16) {
                                                sizeIndex = 3;
                                            } else {
                                                System.out.println("Invalid size!");
                                                break;
                                            }
                                            //Order Quantity
                                            int quantity = 0;
                                            System.out.print("Enter quantity: ");
                                            String quantityInput = s.nextLine().trim(); //Takes the order quantity

                                            if (quantityInput.matches("\\d+")) { //Input must be digits only
                                                quantity = Integer.parseInt(quantityInput); //Since the menu is string, parse will read the numbers as digits
                                                if (quantity <= 0) {
                                                    System.out.println("Quantity must be positive.");
                                                    break;
                                                }
                                            } else {
                                                System.out.println("Invalid quantity input.");
                                                break;
                                            }
                                            float price = Float.parseFloat(Matcha[i][sizeIndex]);
                                            orderList.add(Matcha[i][0] + " (" + temperature + ", " + size + "oz)  - " + quantity);
                                            priceList.add(price * quantity);
                                            totalPrice += price * quantity;
                                            coffeeIndexes.add(orderList.size() - 1);
                                            System.out.println(quantity + " - " + Matcha[i][0] + " (" + temperature + ", " + size + "oz) added to order. PHP" + price * quantity);
                                            break;
                                        }
                                    }
                                    if (!foundM) {
                                        System.out.println("Item not found!");
                                    }
                                    break;

                                case 'D':

                                    if (orderList.isEmpty()) {
                                        System.out.println("No coffee orders yet! Add a coffee first.");
                                        break;
                                    }

                                    System.out.println("------------------------------");
                                    System.out.println("Select the coffee to add an add-on:");
                                    for (int i = 0; i < orderList.size(); i++) {
                                        System.out.println((i + 1) + ". " + orderList.get(i));
                                    }

                                    System.out.print("Enter the number of the coffee: ");
                                    int coffeeIndex = -1;
                                    try {
                                        coffeeIndex = Integer.parseInt(s.nextLine()) - 1;
                                        if (coffeeIndex < 0 || coffeeIndex >= orderList.size()) {
                                            System.out.println("Invalid coffee selection!");
                                            break;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid input!");
                                        break;
                                    }

                                    String selectedOrder = orderList.get(coffeeIndex);
                                    int quantity = 1;
                                    try {
                                        // Look for the last '-' in the string (before the quantity)
                                        int lastDash = selectedOrder.lastIndexOf("-");
                                        if (lastDash != -1) {
                                            String qtyText = selectedOrder.substring(lastDash + 1).trim();
                                            // Remove any stray symbols like '+' or ')'
                                            qtyText = qtyText.replaceAll("[^0-9]", "");
                                            quantity = Integer.parseInt(qtyText);
                                        }
                                    } catch (Exception e) {
                                        quantity = 1; // fallback to 1 if anything goes wrong
                                    }

                                    boolean repeatAddon = true;
                                    while (repeatAddon) {

                                        if (quantity > 1) {
                                            System.out.print("How many of the " + quantity + " will have add-ons? ");
                                            int addonQty = Integer.parseInt(s.nextLine());
                                            if (addonQty <= 0 || addonQty > quantity) {
                                                System.out.println("Invalid number of cups!");
                                                break;
                                            }

                                            int remaining = quantity - addonQty;
                                            float basePrice = priceList.get(coffeeIndex) / quantity;
                                            float baseTotal = basePrice * remaining;

                                            if (remaining > 0) {
                                                orderList.set(coffeeIndex, selectedOrder.replace("- " + quantity, "- " + remaining));
                                                priceList.set(coffeeIndex, baseTotal);
                                            } else {
                                                orderList.remove(coffeeIndex);
                                                priceList.remove(coffeeIndex);
                                            }

                                            boolean addingMore = true;
                                            ArrayList<String> chosenAddons = new ArrayList<>();
                                            float addonCost = 0;

                                            while (addingMore) {
                                                System.out.println("------------------------------");
                                                System.out.println("Available Add-ons:");
                                                for (int i = 0; i < Addons.length; i++) {
                                                    System.out.println((i + 1) + ". " + Addons[i][0] + " - PHP" + Addons[i][1]);
                                                }
                                                System.out.println("0. Cancel Add-ons");

                                                System.out.print("Enter add-on number: ");
                                                String input = s.nextLine().trim();

                                                //  Handle cancel
                                                if (input.equals("0")) {
                                                    System.out.println(" Add-on selection cancelled.");
                                                        break; 
                                                }

                                                int addonIndex;
                                                try {
                                                    addonIndex = Integer.parseInt(input) - 1;
                                                } catch (NumberFormatException e) {
                                                    System.out.println("Invalid input! Try again.");
                                                    continue;
                                                }

                                                if (addonIndex < 0 || addonIndex >= Addons.length) {
                                                    System.out.println("Invalid add-on!");
                                                    continue;
                                                }

                                                String addonName = Addons[addonIndex][0];
                                                float addonPrice = Float.parseFloat(Addons[addonIndex][1]);
                                                chosenAddons.add(addonName);
                                                addonCost += addonPrice;

                                                System.out.print("Add another add-on? (Y/N): ");
                                                String more = s.nextLine().trim();
                                                if (!more.equalsIgnoreCase("Y")) {
                                                    addingMore = false;
                                                }
                                            }

                                            String newOrder = selectedOrder.split("-")[0].trim() + " + " + String.join(" + ", chosenAddons) + " - " + addonQty;
                                            float totalWithAddons = (basePrice + addonCost) * addonQty;

                                            orderList.add(newOrder);
                                            priceList.add(totalWithAddons);
                                            totalPrice += totalWithAddons;

                                            System.out.println("Added " + chosenAddons + " to " + addonQty + " cup(s) of " + selectedOrder.split("-")[0].trim());

                                            if (remaining > 0) {
                                                System.out.print("Do you want to add another add-on to the remaining cups of this coffee? (Y/N): ");
                                                String again = s.nextLine().trim();
                                                if (again.equalsIgnoreCase("Y")) {

                                                    selectedOrder = orderList.get(coffeeIndex);
                                                    String[] parts = selectedOrder.split("-");
                                                    String qtyText = parts[1].trim();
                                                    quantity = Integer.parseInt(qtyText);
                                                } else {
                                                    repeatAddon = false;
                                                }
                                            } else {
                                                repeatAddon = false;
                                            }

                                        } else {
                                            System.out.println("This coffee has only 1 cup. Add-ons apply to it directly");

                                            System.out.println("------------------------------");
                                            System.out.println("Available Add-ons:");
                                            for (int i = 0; i < Addons.length; i++) {
                                                System.out.println((i + 1) + ". " + Addons[i][0] + " - PHP" + Addons[i][1]);
                                            }

                                            System.out.print("Enter add-on number: ");
                                            int addonIndex = Integer.parseInt(s.nextLine()) - 1;
                                            if (addonIndex < 0 || addonIndex >= Addons.length) {
                                                System.out.println("Invalid add-on!");
                                                break;
                                            }

                                            String addonName = Addons[addonIndex][0];
                                            float addonPrice = Float.parseFloat(Addons[addonIndex][1]);

                                            String updatedOrderName = selectedOrder + " + " + addonName;
                                            orderList.set(coffeeIndex, updatedOrderName);

                                            float oldPrice = priceList.get(coffeeIndex);
                                            priceList.set(coffeeIndex, oldPrice + addonPrice);
                                            totalPrice += addonPrice;

                                            System.out.println(addonName + " added to " + selectedOrder);

                                            repeatAddon = false;
                                        }
                                    }
                                    submenu = false;
                                    break;

                                case 'E':
                                    submenu = false; // back to main menu
                                    break;

                                default:
                                    System.out.println("Invalid Type of Coffee! Try again.");
                                    break;
                            }
                        }
                        break;

                    case 2: // Pastry
                        System.out.println("------------------------------");
                        System.out.println("[Pastry]");
                        System.out.println("Product\t\t\tPrice");
                        for (int i = 0; i < Pastry.length; i++) {
                            System.out.print((i + 1) + ". " + Pastry[i][0]);

                            if (((i + 1) + ". " + Pastry[i][0]).length() < 16) {
                                System.out.print("\t\t");
                            } else {
                                System.out.print("\t");
                            }
                            System.out.println("PHP" + Pastry[i][1]);
                        }
                        //Takes the order (Product)
                        System.out.print("Enter the product name to add or 'C' to cancel: ");
                        String pastryChoice = s.nextLine().trim();
                        if (pastryChoice.equalsIgnoreCase("C")) {
                            System.out.println("Cancelled selection.");
                            break;
                        }
                        boolean foundP = false;
                        for (int i = 0; i < Pastry.length; i++) {
                            if (pastryChoice.equalsIgnoreCase(Pastry[i][0])) {
                                foundP = true;

                                //Order Quantity
                                int quantity = 0;
                                System.out.print("Enter quantity: ");
                                String quantityInput = s.nextLine().trim(); //Takes the order quantity

                                if (quantityInput.matches("\\d+")) { //Input must be digits only
                                    quantity = Integer.parseInt(quantityInput); //Since the menu is string, parse will read the numbers as digits
                                    if (quantity <= 0) {
                                        System.out.println("Quantity must be positive.");
                                        break;
                                    }
                                } else {
                                    System.out.println("Invalid quantity input.");
                                    break;
                                }
                                float price = Float.parseFloat(Pastry[i][1]);
                                orderList.add(Pastry[i][0] + " - " + quantity);
                                priceList.add(price * quantity);
                                totalPrice += price * quantity;
                                System.out.println(quantity + " - " + Pastry[i][0] + " added to order. PHP" + price * quantity);
                                break; //back to main menu
                            }
                        }
                        if (!foundP) {
                            System.out.println("Item not found!");
                        }
                        break;

                    case 3: // Refreshers
                        System.out.println("------------------------------");
                        System.out.println("[Refreshers]");
                        System.out.println("Product\t\t\tPrice");
                        for (int i = 0; i < Refreshers.length; i++) {
                            System.out.println((i + 1) + ". " + Refreshers[i][0] + "\t\tPHP" + Refreshers[i][1]);
                        }
                        //Takes the order (Product)
                        System.out.print("Enter the product name to add or 'C' to cancel: ");
                        String refChoice = s.nextLine().trim();
                        if (refChoice.equalsIgnoreCase("C")) {
                            System.out.println("Cancelled selection.");
                            break;
                        }
                        boolean foundR = false;
                        for (int i = 0; i < Refreshers.length; i++) {
                            if (refChoice.equalsIgnoreCase(Refreshers[i][0])) {
                                foundR = true;

                                //Order Quantity
                                int quantity = 0;
                                System.out.print("Enter quantity: ");
                                String quantityInput = s.nextLine().trim(); //Takes the order quantity

                                if (quantityInput.matches("\\d+")) { //Input must be digits only
                                    quantity = Integer.parseInt(quantityInput); //Since the menu is string, parse will read the numbers as digits
                                    if (quantity <= 0) {
                                        System.out.println("Quantity must be positive.");
                                        break;
                                    }
                                } else {
                                    System.out.println("Invalid quantity input.");
                                    break;
                                }
                                float price = Float.parseFloat(Refreshers[i][1]);
                                orderList.add(Refreshers[i][0] + " - " + quantity);
                                priceList.add(price * quantity);
                                totalPrice += price * quantity;
                                System.out.println(quantity + " - " + Refreshers[i][0] + " added to order. PHP" + price * quantity);
                                break; //back to main menu
                            }
                        }
                        if (!foundR) {
                            System.out.println("Item not found!");
                        }
                        break;

                    case 4: // Checkout            
                        System.out.println("\n===== DNJ COFFEE RECEIPT =====");
                        System.out.println("Cashier: " + currentUser);
                        System.out.println("Date: " + java.time.LocalDateTime.now());
                        System.out.println("------------------------------");

                        if (orderList.isEmpty()) {
                            System.out.println("No items in your order!");
                        } else {
                            for (int i = 0; i < orderList.size(); i++) {
                                System.out.printf("%d. %s - PHP %.2f%n", (i + 1), orderList.get(i), priceList.get(i));
                            }
                            System.out.println("------------------------------");
                            System.out.printf("TOTAL: PHP %.2f%n", totalPrice);

                            int money = 0;
                            int addcash = 0;
                            while (money < totalPrice) {
                                System.out.print("Cash: "); //Input the cash amount provided by the customer
                                String cashInput = s.nextLine().trim();
                                addcash = Integer.parseInt(cashInput);
                                money += addcash;

                                if (money < totalPrice) {
                                    System.out.println("Not enough cash, add " + php + (totalPrice - money) + " more");
                                }
                            }

                            double change = money - totalPrice; // To compute the customer's change

                            System.out.printf("Change: PHP %.2f%n", change);

                            System.out.print("Confirm order? (Y/N): ");
                            String confirmStr = s.nextLine().trim();
                            char confirm = confirmStr.isEmpty() ? 'N' : Character.toUpperCase(confirmStr.charAt(0));

                            if (confirm == 'Y') {
                                System.out.println("Order placed successfully!");
                                // reset for next customer
                                orderList.clear();
                                priceList.clear();
                                totalPrice = 0;
                            } else {
                                System.out.println("Order cancelled.");
                            }
                        }
                        break;

                    case 5: // Logout
                        System.out.println("Logging Out...");
                        loggedIn = false;
                        ordering = false;
                        break;

                    default:
                        System.out.println("Invalid Choice! Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Invalid Input! Try Again....");
                s.nextLine();
            }

        }

        System.out.println("Returning to Login Screen....");
        main(args);
    }
}
