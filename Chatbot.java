import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Chatbot {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";

    private static String getChatbotResponse(String userMessage) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            // Request with max_tokens
            String jsonInput = "{"
                    + "\"model\": \"llama-3.1-8b-instant\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + userMessage + "\"}],"
                    + "\"max_tokens\": 2000"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();
                connection.disconnect();
                return parseResponse(response.toString());
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                errorReader.close();
                System.out.println("Error: HTTP " + responseCode + " - " + errorResponse.toString());
                return "Failed to get a valid response from the server.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your request.";
        }
    }

    private static String parseResponse(String jsonResponse) {
        try {
            // Extract content from the response manually
            int choicesStartIndex = jsonResponse.indexOf("\"choices\":") + 10;
            int choicesEndIndex = jsonResponse.indexOf("]", choicesStartIndex);
            String choicesContent = jsonResponse.substring(choicesStartIndex, choicesEndIndex);

            // Find the message content within the choices
            int messageStartIndex = choicesContent.indexOf("\"content\":") + 11;
            int messageEndIndex = choicesContent.indexOf("\"", messageStartIndex);
            String botMessage = choicesContent.substring(messageStartIndex, messageEndIndex);

            // Remove Markdown-style bold formatting (**)
            botMessage = botMessage.replace("**", "");

            // Check if the response is incomplete (ends abruptly)
            if (botMessage.endsWith(":") || botMessage.endsWith("\\")) {
                botMessage += "\n[Response might be incomplete. Please ask again for continuation.]";
            }

            // Replace escape sequences like \n with actual newlines
            botMessage = botMessage.replace("\\n", "\n").replace("\\", "");

            return botMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse the response.";
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the AI Chatbot!");
        System.out.println("Type 'exit' to quit.\n");

        while (true) {
            System.out.print("You: ");
            String userMessage = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userMessage)) {
                System.out.println("Goodbye!");
                break;
            }

            String botResponse = getChatbotResponse(userMessage);

            // Add a line space before the bot's response
            System.out.println();
            System.out.println("Bot:\n" + botResponse);
            System.out.println();
        }

        scanner.close();
    }
}
