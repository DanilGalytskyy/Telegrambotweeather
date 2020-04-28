import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Bot extends TelegramLongPollingBot {
    public static String city_name = "";
    String url = "https://api.openweathermap.org/data/2.5/weather?";
    final String url_key = "f33ff8a6de7d717174ef993a8fd50f3f";
    static int code;

    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        String txt = msg.getText();

        if (txt.equals("/start")) {
            sendMessage(msg, "Привет, здесь ты можешь посмотреть погоду в любом городе мира\uD83C\uDF0D");
        } else {
            city_name = txt;
            code = 0;
            try {
                writeFile(createConnection(createURL(url, url_key, city_name)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (code == 200) {
                    sendMessage(msg, parseJSON());
                } else {
                    sendMessage(msg, "Глупец, иди учи географию \nТакого города нет");
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


    private void sendMessage(Message msg, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "Kingdom_of_weather_bot";
    }

    public String getBotToken() {
        return "1029222404:AAE8CPbN_GBBKDJqJ4MHc3ynwT0w1dycpKQ";
    }

    public static void main(String[] args) throws Exception {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }

    static String createURL(String url, String url_key, String city_name) {
        Scanner sc = new Scanner(System.in);
        HashMap<String, String> hash = new HashMap<>();
        hash.put("q", city_name);
        hash.put("appid", url_key);
        hash.put("units", "metric");
        for (Map.Entry obj : hash.entrySet()) {
            url = url + obj.getKey() + "=" + obj.getValue() + "&";
        }
        return url;
    }

    static String createConnection(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        Scanner fileScanner = new Scanner(connection.getInputStream());
        String response = "";
        while (fileScanner.hasNextLine()) {
            response += fileScanner.nextLine();
        }
        code = connection.getResponseCode();
        return response;
    }

    static void writeFile(String response) throws Exception {
        FileWriter fileWriter = new FileWriter("data.json", false);
        fileWriter.write(response);
        fileWriter.close();
    }

    static String parseJSON() throws Exception {
        FileReader fileReader = new FileReader("data.json");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

        String output = "";

        JSONObject main = (JSONObject) jsonObject.get("main");
        JSONObject sys = (JSONObject) jsonObject.get("sys");
        JSONObject wind = (JSONObject) jsonObject.get("wind");

        output += "Страна: " + sys.get("country") + "\n";
        output += "Город: " + jsonObject.get("name") + "\n";
        output += "Температура: " + main.get("temp") + "°С" + "\n";
        output += "Ощущается как: " + main.get("feels_like") + "°С" + "\n";
        output += "Минимальная температура: " + main.get("temp_min") + "°С" + "\n";
        output += "Максимальная температура: " + main.get("temp_max") + "°С" + "\n";
        output += "Скорость ветра: " + wind.get("speed") + " м/с" + "\n";

        return output;
    }
}
