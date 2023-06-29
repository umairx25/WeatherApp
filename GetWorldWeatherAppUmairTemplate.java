/*****************************************************************************
*  Uses OpenWeather data from website:  openweathermap.org
*  API key must be requested for the current weather and forcast free plan
*     free plan allows:  60 calls per minute
*
*  Description of how to make the API call: https://openweathermap.org/forecast5
*     and parameters available which you can add (see the metric parameter
*     added for degrees celcius in the call below)!
*****************************************************************************/

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.URI;
import java.io.IOException;
import java.lang.InterruptedException;

import javax.swing.*; //buttons, labels, Jboxes, etc. for GUI
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.border.*;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;      // swing is newer graphics package
import java.awt.*;         // abstract windowing toolkit
import java.awt.event.*;   // for the WindowEvent object
import java.util.Random;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;


 /*WeatherApp class 
 Purpose: To display weather information for city in the world*/
public class GetWorldWeatherAppUmairTemplate
{
   private static final long serialVersionUID = 1L;


  /*****************************************************************************************************
  * main method <BR>
  *
  * Purpose: to instantiate the Car class.
  * @param String[] args
  * @throws Exception
  ****************************************************************************************************/
   public static void main(String[] args) 
   {
      
      GUI frame = new GUI();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
} 



/*GUI class 
 Purpose: The class contains all code to run the GUI design.*/ 
class GUI extends JFrame implements ActionListener, KeyListener
{
   private static final long serialVersionUID = 1L;
   private static String apiKey = "c0f4a39232910b60d78d8445d232ab74";  //you will need a working api key
   
   JTextArea weatherDataTextArea;
   ArrayList<String[]> countryData;
   HashMap<String, String>  timeZoneData;
   JButton getWeatherBtn = new JButton("Get Weather");
   JComboBox<String> countryModelCbox;
   JTextField cityTextField;
      
   /*****************************************************************************************************
   * GUI method
   *
   * Purpose: A constructor to builds all the elements, design, and functionality of the GUI.
   ****************************************************************************************************/
   public GUI()
   {
      super("World Weather Information");
      
      // Read in country codes and timezone data
      countryData = readInData("Country List.csv");
      timeZoneData = readInTimeZones("timeZone look up noDesc.csv");
      
      // Build the components     
      JLabel cityLbl = new JLabel("Choose location: ");
      cityLbl.setForeground(Color.white);
      cityLbl.setFont(new Font("Courier", Font.PLAIN, 17));
   
      // weather image
      ImageIcon theIcon = new ImageIcon(getClass().getResource("res/weather2.png")); 
      Image theImage = theIcon.getImage();
      Image modifiedImage = theImage.getScaledInstance(250, 300, java.awt.Image.SCALE_SMOOTH);
      theIcon = new ImageIcon(modifiedImage);
      JLabel weatherPicLbl = new JLabel(" ");
      weatherPicLbl.setIcon(theIcon);
      
      // output weather area
      weatherDataTextArea = new JTextArea("");
      weatherDataTextArea.setFont(new Font("Courier", Font.BOLD, 20));
      weatherDataTextArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),BorderFactory.createRaisedBevelBorder()));
      weatherDataTextArea.setLineWrap(true);
      weatherDataTextArea.setWrapStyleWord(true);
      weatherDataTextArea.setEditable(false);
   
          
      //comboBox - populate CB with cities from input file
      String[] choices = {" "};
      countryModelCbox = new JComboBox<>(choices);
      countryModelCbox.removeAllItems();
      for (int i = 0 ; i<countryData.size(); i++)
      {
         String[] theCountry = countryData.get(i);
         countryModelCbox.addItem(theCountry[0]);
      }
      countryModelCbox.setFont(new Font("Courier", Font.BOLD, 15));
      countryModelCbox.setEditable(false);
      countryModelCbox.setSelectedItem("Canada");
      countryModelCbox.setPreferredSize(new Dimension(300, 30));
      countryModelCbox.addActionListener(this);
      
      //TextField
      cityTextField = new JTextField();
      cityTextField.addKeyListener(this);
      cityTextField.setFont(new Font("Courier", Font.BOLD, 15));
      cityTextField.setPreferredSize(new Dimension(200, 30));
      cityTextField.addActionListener(this);    
      getWeatherBtn.addActionListener(this);
      
   
      // put components and panels in place
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.X_AXIS));
      topPanel.add(cityLbl);
      topPanel.add(countryModelCbox);
      topPanel.add(cityTextField);
      topPanel.add(new JLabel("  "));
      topPanel.add(getWeatherBtn);
      
      JPanel pane = (JPanel) this.getContentPane();  
      pane.setLayout(new BorderLayout());
      pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));  
      pane.add(topPanel, BorderLayout.NORTH);  
      pane.add(weatherPicLbl, BorderLayout.WEST);
      pane.add(weatherDataTextArea, BorderLayout.CENTER);  
   
   
      //Colours for the GUI
      Color outline = new Color(56,0,14);
      Color top = new Color(117,1,30);
      Color unselBox = new Color(171, 123, 123);
      topPanel.setBackground(top);
      weatherDataTextArea.setBackground(new Color(247, 153, 179));
      countryModelCbox.setBackground(unselBox);
      countryModelCbox.setForeground(new Color(20,1,1)); 
      cityTextField.setBackground(unselBox);
      cityTextField.setForeground(new Color(20,1,1));
      pane.setBackground(outline);          
   
      // display the GUI
      pack();
      setSize(800, 400);
      setVisible(true);
   }


   /*****************************************************************************************************
   * readInData method
   * 
   * Purpose: Adds an array list that reads in the data from the data table.
   ****************************************************************************************************/ 
   public ArrayList<String[]> readInData(String fileName)
   {
      // instantiate an ArrayList for all record data
      ArrayList<String[]> countries = new ArrayList<String[]>();
      
      try (InputStream in = getClass().getResourceAsStream("/res/"+fileName);
           BufferedReader reader = new BufferedReader(new InputStreamReader(in))) 
      {
         String line;
      
         while ((line = reader.readLine())!=null)
         {
            String[] country = {line.substring(0,line.indexOf(",")), line.substring(line.length()-2)};
            countries.add(country);
         }   
      }
      catch ( IOException iox )
      {
         System.out.println("Problem reading " + fileName );
         String[] errorMsg = {fileName};
         countries.add(errorMsg);
      }
      //System.out.println(countries);                                          
      return countries;
   } 



  /*****************************************************************************************************
   * readInTimeZones method
   * 
   * Purpose: Adds an array list that reads in the data from the data table.
   ****************************************************************************************************/ 
   public HashMap<String,String> readInTimeZones(String fileName)
   {
      // instantiate an ArrayList for all record data
      HashMap<String,String>  timeZones = new HashMap<String,String>();
      
      try (InputStream in = getClass().getResourceAsStream("/res/"+fileName);
           BufferedReader reader = new BufferedReader(new InputStreamReader(in))) 
      {
         String line = reader.readLine(); //read in titles to throw away
      
         while ((line = reader.readLine())!=null)
         {
            String[] lineInfo = line.split(",");
            String key = lineInfo[0];
            String value = lineInfo[1];
            timeZones.put(key,value);
         }   
      }
      catch ( IOException iox )
      {
         System.out.println("Problem reading " + fileName );
      }
      //System.out.println(timeZones);                                          
      return timeZones;
   } 


    
  
   /*****************************************************************************************************
   * writeToScreen method
   * 
   * Purpose: This method is where you come in!  Make all of your changes here (and hopefully in
   *          new methods to modularize the functionality) to display selected information from what is 
   *          found in the "data" variable, in a readable way.
   ****************************************************************************************************/ 
   public void writeToScreen(String data)
   {
      String city = cityTextField.getText().toLowerCase();
      city = city.substring(0,1).toUpperCase() + city.substring(1);
      String formattedWeatherData = "Weather for "+city+", "+countryModelCbox.getSelectedItem()+":"+
                                    "\n"+data;
   
      weatherDataTextArea.setText(formattedWeatherData);
   }
   
    
   /*****************************************************************************************************
   * getWeather method
   * 
   * Purpose: 
   ****************************************************************************************************/  
   public void getWeather()
   {
      String city = cityTextField.getText().trim();
      String[] selectedCountry = countryData.get(countryModelCbox.getSelectedIndex());
      String countryCode = selectedCountry[1];
      if (city.indexOf(" ") != -1)
         for(int i = 0 ; i<city.length() ; i++)
            if (city.charAt(i) == ' ')
               city = city.substring(0,i)+"%20"+city.substring(i+1);
               
      String temp= "";
      String feels= "";
      String humid= "";
      String max= "";
      String min= "";
            
      String result = requestWeather(city, countryCode); 
      // System.out.println(result);
      if (result.substring(2,7).equals("coord"))
      {
      
         int temps = result.indexOf("temp");
         temp ="Temperature: "+ result.substring(temps+6,result.indexOf(',',temps+6) );
      
         int mins = result.indexOf("temp_min");
         min =("Minimum: "+ result.substring(mins+10,result.indexOf(',',mins+5)) );
      
         int maxs = result.indexOf("temp_max");
         max =("Maximum: "+ result.substring(maxs+10,result.indexOf(',',maxs+5)) );
      
         int humss = result.indexOf("humidity");
         humid =("Humidity: "+ result.substring(humss+10,humss+12));
      
         int feeling = result.indexOf("feels_like");
         feels =("Feels Like: "+ result.substring(feeling+12,result.indexOf(',',feeling+5)) );
       
              
         writeToScreen(temp+"\n"+feels+"\n"+humid+"\n"+max+"\n"+min);
      
      }
         
      else
         weatherDataTextArea.setText("Sorry, "+cityTextField.getText().trim().toUpperCase()+" is not listed.");
   }

   /*****************************************************************************************************
   * actionPerformed method
   * 
   * Purpose: 
   ****************************************************************************************************/ 
   @Override
   public void actionPerformed(ActionEvent ae) 
   {
      if (ae.getSource() == getWeatherBtn )
         getWeather();   
   }
   
   /*****************************************************************************************************
   * keylistener interface methods
   * 
   * Purpose: To process the enter key
   ****************************************************************************************************/ 
   @Override
   public void keyPressed(KeyEvent e) {
      if (e.getKeyCode()==KeyEvent.VK_ENTER){
         getWeather(); 
      }
   }
   @Override
    public void keyReleased(KeyEvent arg) {
   }
   @Override
    public void keyTyped(KeyEvent arg) {
   }

		
   /*****************************************************************************************************
   * requestWeather method
   * 
   * Purpose: Format is java.net.http format from the https://rapidapi.com/community/api/open-weather-map
   *          website for REST API weather data.
   ****************************************************************************************************/ 
   public  String requestWeather(String city, String countryCode) 
   {
      try
      {  //java.net.http format
      
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?q="+city+","+countryCode+"&APPID="+apiKey+"&units=metric"))
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();
      
         HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
         System.out.println("Response from website: \n"+response.body());
         String r = response.body() ;
         
         for (int i= 0; i<r.length() ; i++)
         {
         
            if(r.charAt(i) == 't' && r.charAt(i+1) == 'e' && r.charAt(i+2) == 'm' && r.charAt(i+3) == 'p' && r.charAt(i+4) == '"')
               writeToScreen("Temperature: "+ r.substring(i+6,i+10) );
         }
         
        
         
         
         return (response.body());
         
         // response back example for Toronto, Canada:
         // {"coord":{"lon":-79.4163,"lat":43.7001},"weather":[{"id":804,"main":"Clouds","description":"overcast clouds","icon":"04n"}],"base":"stations","main":{"temp":289.73,"feels_like":289.64,"temp_min":289.21,"temp_max":290.09,"pressure":1019,"humidity":84},"visibility":10000,"wind":{"speed":6.69,"deg":50},"clouds":{"all":100},"dt":1662335424,"sys":{"type":1,"id":718,"country":"CA","sunrise":1662288288,"sunset":1662335340},"timezone":-14400,"id":6167865,"name":"Toronto","cod":200}
      }
      catch(IOException ioe)
      {  System.out.println("IO Exception: "+ioe);}
      catch (InterruptedException ie)
      { System.out.println("InterruptedException: "+ie);}
      
      return "Request failed";
   }
}
