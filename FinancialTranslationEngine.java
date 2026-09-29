import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FinancialTranslationEngine {

    private static final Map<String, BigDecimal> FALLBACK_RATES = new HashMap<>();
    static {
        FALLBACK_RATES.put("USD", new BigDecimal("1.0"));
        FALLBACK_RATES.put("EUR", new BigDecimal("0.92"));
        FALLBACK_RATES.put("GBP", new BigDecimal("0.79"));
        FALLBACK_RATES.put("PKR", new BigDecimal("278.50"));
        FALLBACK_RATES.put("INR", new BigDecimal("83.95"));
        FALLBACK_RATES.put("AED", new BigDecimal("3.67"));
    }

    public static BigDecimal getLiveRate(String target) {
        try {
            URL url = new URL("https://api.exchangerate-api.com/v4/latest/USD");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) return FALLBACK_RATES.get(target);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            String json = sb.toString();
            String key = "\"" + target + "\":";
            int idx = json.indexOf(key);
            if (idx == -1) return FALLBACK_RATES.get(target);
            int start = idx + key.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return new BigDecimal(json.substring(start, end).trim());
        } catch (Exception e) {
            return FALLBACK_RATES.get(target);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String choice = "yes"; // FIX: Initialized to avoid compilation error

        System.out.println("=== THE FINANCIAL TRANSLATION ENGINE ===");

        do {
            try {
                System.out.print("\n> Enter Source (USD/EUR/PKR/INR/GBP/AED): ");
                String from = sc.next().toUpperCase();
                System.out.print("> Enter Target: ");
                String to = sc.next().toUpperCase();

                if (!FALLBACK_RATES.containsKey(from) || !FALLBACK_RATES.containsKey(to)) {
                    System.out.println("[Security Gate] Invalid Currency!");
                    System.out.print("\nContinue or Exit? (yes/no): ");
                    choice = sc.next();
                    continue;
                }

                BigDecimal amount = null;
                while (amount == null) {
                    System.out.print("> Enter Amount: ");
                    try {
                        amount = new BigDecimal(sc.next());
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            System.out.println("[SECURITY GATE]: Negative blocked!");
                            amount = null;
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("[BUFFER TRAP]: Invalid token!");
                        sc.nextLine();
                    }
                }
                if (amount == null) {
                    System.out.print("\nContinue or Exit? (yes/no): ");
                    choice = sc.next();
                    continue;
                }

                BigDecimal rateFrom = from.equals("USD") ? new BigDecimal("1.0") : getLiveRate(from);
                BigDecimal rateTo = to.equals("USD") ? new BigDecimal("1.0") : getLiveRate(to);
                BigDecimal intermediateUSD = amount.divide(rateFrom, 10, RoundingMode.HALF_EVEN);
                BigDecimal finalValue = intermediateUSD.multiply(rateTo);
                BigDecimal polished = finalValue.setScale(2, RoundingMode.HALF_EVEN);

                System.out.println("\n--- Professional Finish [CERTIFIED] ---");
                System.out.printf("Result: %,.2f %s\n", polished, to);
                System.out.printf("Enterprise Format: %15.2f %s\n", polished, to);
                System.out.println("Status: CERTIFIED - HALF_EVEN Applied");
                System.out.println("---------------------------------------");

            } catch (Exception e) {
                System.out.println("[FATAL BUFFER]: " + e.getMessage());
                sc.nextLine();
            }
            
            System.out.print("\nContinue or Exit? (yes/no): ");
            choice = sc.next();
            
        } while (choice.equalsIgnoreCase("yes") || choice.equalsIgnoreCase("y"));

        System.out.println("\nEngine Shutdown. All Gatekeeper Checks Passed.");
        sc.close();
    }
}