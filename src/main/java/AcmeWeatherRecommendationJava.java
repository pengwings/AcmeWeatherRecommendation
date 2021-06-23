import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AcmeWeatherRecommendationJava {

    public static void main(String[] args) throws IOException {
        String city = "minneapolis";
        String units = "imperial";
        String APIkey = "09110e603c1d5c272f94f64305c09436";
        String currentWeather = "";
        ObjectMapper mapper = new ObjectMapper();

        String dateFormat = "yyyy-MM-dd";
        String hourFormat = "HH";
        SimpleDateFormat dfObj = new SimpleDateFormat(dateFormat);
        SimpleDateFormat hObj = new SimpleDateFormat(hourFormat);
        String currentDay = dfObj.format(new Date());
        String currentHour = hObj.format(new Date());
        String adjustedHour = adjustHour(currentHour);
        String[] days = new String[5];


        for(int i=0; i<5; i++) {
            days[i] = LocalDate.parse(currentDay).plusDays(i).toString();
        }

        URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&units=" + units + "&APPID=" + APIkey);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("GET");
        http.connect();

        try {
            if (http.getResponseCode() == 200) {
                Scanner sc = new Scanner(url.openStream());
                while(sc.hasNext()) {
                    currentWeather += sc.nextLine();
                }
                sc.close();
            } else {
                throw new RuntimeException("Response Code: " + http.getResponseCode());
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve weather information.");
        }

        Response response = mapper.readValue(currentWeather, Response.class);
        response.getList().forEach(e -> {
            for(String day : days) {
                String givenDate = day + " " + adjustedHour + ":00:00";
                if(givenDate.equals(e.getDtTxt())) {
                   double temperature = e.getMain().get("temp").asDouble();
                   String weather = e.getWeather().get(0).getMain();
                   String icon = e.getWeather().get(0).getIcon();
                    if(temperature>75.0 && icon.equals("01d")) {
                        System.out.println(formatOutput(temperature, weather, icon, day, adjustedHour,1));
                    } else if(temperature < 55.0 || weather.equals("Rain")) {
                        System.out.println(formatOutput(temperature, weather, icon, day, adjustedHour, 3));
                    } else if(temperature <= 75.0 && temperature >= 55.0) {
                        System.out.println(formatOutput(temperature, weather, icon, day, adjustedHour, 2));
                    } else {
                        System.out.println(formatOutput(temperature, weather, icon, day, adjustedHour, 4));
                    }
                }
            }
        });
        http.disconnect();
    }
    private static String formatOutput(double temperature, String weather, String icon, String day, String hour, int method) {
        StringBuilder output = new StringBuilder();
        String intro = "Today is ";
        String temp = ". The temperature is ";
        String sky = ". The weather is ";
        String customer = ":00, the best way to engage a customer is via";
        output.append(intro).append(day);
        output.append(temp).append(temperature);
        output.append(sky).append(weather);
        switch(method) {
            case 1:
                output.append(". At ").append(hour).append(customer).append(" a text message.");
                break;
            case 2:
                output.append(". At ").append(hour).append(customer).append(" an email.");
                break;
            case 3:
                output.append(". At ").append(hour).append(customer).append(" a phone call.");
                break;
            case 4:
                output.append(". At ").append(hour).append(":00, there is no good way engage a customer.");
                break;
        }
        return output.toString();
    }
    private static String adjustHour(String currentHour) {
        int roundedHour = (int)(3*(Math.round(Double.parseDouble(currentHour)/3)) + 6);

        return String.valueOf(roundedHour);
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Response {
    private List<Forecast> list;

    public List<Forecast> getList() {
        return list;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Forecast {
    private JsonNode main;
    private List<Weather> weather;

    @JsonProperty("dt_txt")
    private String dtTxt;

    public JsonNode getMain() {
        return main;
    }
    public List<Weather> getWeather() {
        return weather;
    }
    public String getDtTxt() {
        return dtTxt;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Weather {
    @JsonProperty("main")
    private String main;
    @JsonProperty("icon")
    private String icon;
    
    public String getMain() {
        return main;
    }
    public String getIcon() { return icon; }
}

