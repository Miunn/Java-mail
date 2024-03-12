import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class Gui extends JFrame {
    public static void main(String args[]){
        int WIDTH = 1200;
        int HEIGHT = 800;

        // GLOBAL FRAME
        JFrame frame = new JFrame("Java Mail");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLayout(null);

        // TOP BAR (ACTION BAR)
        JPanel actionBar = new JPanel();
        actionBar.setBounds(0, 0, WIDTH, 50);
        actionBar.setBackground(java.awt.Color.GREEN);
        actionBar.setLayout(new FlowLayout());

        // New mail button
        JButton newMail = new JButton("New Mail");
        actionBar.add(newMail);

        // Delete mail button
        JButton deleteMail = new JButton("Delete Mail");
        actionBar.add(deleteMail);

        // Reply mail button
        JButton respondMail = new JButton("Reply");
        actionBar.add(respondMail);

        // Refresh mail button 
        JButton refreshMail = new JButton("Refresh");
        actionBar.add(refreshMail);

        frame.add(actionBar);

        // VERTICAL MAIL LIST
        JPanel mailList = new JPanel();
        mailList.setLayout(new GridLayout(30, 1, 0,3));

        JScrollPane mailScrollPane = new JScrollPane(mailList);
        mailScrollPane.setBounds(0, 50, 200, 713);

        // Mail list
        for (int i = 1; i <= 30; i++) {
            JButton mailItem = new JButton();

            JLabel Label = new JLabel("Expeditor " + i + "      Title " + i);

            mailItem.add(Label);

            mailList.add(mailItem);
        }

        frame.add(mailScrollPane);

        // MAIL CONTENT
        JPanel mailContent = new JPanel();
        mailContent.setBounds(200, 50, 1000, 713);
        mailContent.setBackground(java.awt.Color.BLUE);
        frame.add(mailContent);
        
       
        frame.setVisible(true);
    }

    public class Mail {
        private String expeditor;
        private String title;
        private String content;

        public Mail(String expeditor, String title, String content) {
            this.expeditor = expeditor;
            this.title = title;
            this.content = content;
        }

        public String getExpeditor() {
            return expeditor;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public void setExpeditor(String expeditor) {
            this.expeditor = expeditor;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        
    }
}