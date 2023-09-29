import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

public class RaspberryPiMonitor {

    // Dirección IP de la Raspberry Pi
    private static final String RASPBERRY_PI_IP = "172.17.101.6";
    // Puerto en el que se ejecuta la aplicación web en la Raspberry Pi
    private static final String WEB_APP_URL = "http://172.17.2.8:9090/preciosDino/";

    private static String Mensaje = "Se ha detectado un problema con la Raspberry Pi o la aplicación web.\n";
    // Lista de direcciones IP de Raspberry Pi a monitorear
    private static String[][] raspberryInfo = {
            {"RB", "172.17.101.4"},
            {"RB", "172.17.101.5"},
            {"RB", "172.17.101.6"},
            {"RB", "172.17.101.7"},
            {"R20", "172.20.101.4"},
            {"R20", "172.20.101.5"},
            {"LET", "172.30.101.4"},
            {"LET", "172.30.101.5"},
            {"CVL", "172.22.101.4"},
            {"CVL", "172.22.101.5"},
            {"CVL", "172.22.101.6"},
            {"SP", "172.24.101.4"},
            {"SP", "172.24.101.5"},
            {"60C", "172.26.101.4"},
            {"60C", "172.26.101.5"},
            {"AG", "172.27.101.4"},
            {"AG", "172.27.101.5"},
            {"TLH", "172.31.101.4"},
            {"TLH", "172.31.101.5"},
            {"TSM", "172.29.101.4"},
            {"TSM", "172.29.101.5"}
    };



    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int horaActual = calendar.get(Calendar.HOUR_OF_DAY);

                // Verificar si está dentro del rango de 08:00 a 22:00 horas
                if ((horaActual >= 9 && horaActual < 16)||(horaActual >= 17 && horaActual < 22)) {
                    System.out.println("Realizando verificación...");
                    verificarEstado();
                } else {
                    System.out.println("Fuera del horario de verificación.");
                }
            }
        }, 0, 30 * 60 * 1000); // Inicio inmediato y repetir cada 30 minutos
    }

    private static void verificarEstado() {
        boolean isRaspberryPiOnline = true;
        boolean isWebAppRunning = checkWebApp(WEB_APP_URL);

        for (String[] IP : raspberryInfo) {
            boolean isIpOnline = checkRaspberryPi(IP[1]);
            if (!isIpOnline) {
                isRaspberryPiOnline = false;
                Mensaje += IP[0] + " " + IP[1] + "\n";
            }
            //System.out.println(IP[0] + " " + IP[1] + " " + isIpOnline);
        }

        if (!isWebAppRunning) {
            Mensaje += "Dirección no encontrada: " + WEB_APP_URL + "\n";
        }

        if (!isRaspberryPiOnline || !isWebAppRunning) {
            System.out.println("Alerta mensaje de error.");
            sendEmailAlert();
            Mensaje = "Se ha detectado un problema con la Raspberry Pi o la aplicación web.\n";
        }
    }

    // Función para verificar el estado de la Raspberry Pi mediente ping
    private static boolean checkRaspberryPi(String RASPBERRY_PI_IP) {
        try {
            InetAddress address = InetAddress.getByName(RASPBERRY_PI_IP);
            return address.isReachable(5000); // Espera un máximo de 5 segundos para la respuesta
        } catch (IOException e) {
            return false;
        }
    }

    // Función para verificar el estado de la aplicación web desde la url
    private static boolean checkWebApp(String linkUrl) {
        try {
            URL url = new URL(linkUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return responseCode == 200; // Si el código de estado es 200, la aplicación web está en funcionamiento
        } catch (IOException e) {
            return false;
        }
    }


    // Función para enviar una alerta por correo electrónico

    private static void sendEmailAlert() {
        String emisorCorreo = "dinoseleccion@gmail.com";
        String emisorPass = "Dino_2023";
        String receptorCorreo = "mcastillo@grupodinosaurio.com";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true"); // Habilita TLS


        Session session = Session.getInstance(properties, null);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emisorCorreo));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receptorCorreo));
            message.setSubject("Alerta: Problema con la Raspberry Pi o la aplicación web");
            message.setText(Mensaje);

            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", emisorCorreo, "vkrfklgbqcdbpydb");

            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
