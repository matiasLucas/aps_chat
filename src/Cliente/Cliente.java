package Cliente;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import javax.swing.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class Cliente extends JFrame implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private long tamanhoPermitidoKB = 5120; //Igual a 5MB
    private boolean bolArq = false;
    private Arquivo arquivo;
    private JTextArea texto;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JButton btnArq;
    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private JLabel lblTamanho;

    //Componentes da tela do chat
    public Cliente() {
        JLabel lblMessage = new JLabel("Verificar!");
        txtIP = new JTextField("127.0.0.1");
        txtPorta = new JTextField("4000");
        txtNome = new JTextField("Usuário");
        Object[] texts = {lblMessage, txtIP, txtPorta, txtNome };
        JOptionPane.showMessageDialog(null, texts);
        pnlContent = new JPanel();
        texto = new JTextArea(43,70);
        texto.setEditable(false);
        texto.setBackground(new Color(240,240,240));
        txtMsg = new JTextField(50);
        lblHistorico = new JLabel("Chat");
        lblMsg = new JLabel("Mensagem");
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair = new JButton("Sair");
        btnSair.setToolTipText("Sair do Chat");
        btnArq = new JButton("Arquivo");
        btnArq.setToolTipText("Selecionar Arquivo");
        //lblTamanho = new JLabel();
        //lblTamanho.setFont(new java.awt.Font("Dialog", 0, 12));
        //lblTamanho.setText("Tamanho:");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnArq.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);
        pnlContent.add(lblHistorico);
        JScrollPane scroll = new JScrollPane(texto);
        texto.setLineWrap(true);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnArq);
        //pnlContent.add(lblTamanho);
        pnlContent.add(btnSair);
        pnlContent.add(btnSend);

        pnlContent.setBackground(Color.LIGHT_GRAY);
        texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(850,800);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    //Método usado para conectar no server socket, retorna IO Exception caso dê algum erro.
    public void conectar() throws IOException{
        socket = new Socket(txtIP.getText(),Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText()+"\r\n");

        bfw.flush();
    }

     //Método usado para enviar mensagem para o server socket
    public void enviarMensagem(String msg) throws IOException{
        if(bolArq){
            if(validaArquivo()){
                try {
                    Socket socket = new Socket(txtIP.getText().trim(),
                            Integer.parseInt(txtPorta.getText().trim()));

                    BufferedOutputStream bf = new BufferedOutputStream
                            (socket.getOutputStream());
                    byte[] bytea = serializarArquivo();
                    bf.write(bytea);
                    bf.flush();
                    bf.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bolArq = false;
        } else {
        if(msg.equals("Sair")){
            bfw.write("Desconectado \r\n");
            texto.append("Desconectado \r\n");
        }else{
            bfw.write(msg+"\r\n");
            texto.append( txtNome.getText() + " diz -> " + txtMsg.getText()+"\r\n");
        }
        bfw.flush();
        txtMsg.setText("");
        }
    }

    private byte[] serializarArquivo(){
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream ous;
            ous = new ObjectOutputStream(bao);
            ous.writeObject(arquivo);
            return bao.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private boolean validaArquivo(){
        if (arquivo.getTamanhoKB() > tamanhoPermitidoKB){
            JOptionPane.showMessageDialog(this,
                    "Tamanho máximo permitido atingido ("+(tamanhoPermitidoKB/1024)+")");
            return false;
        }else{
            return true;
        }
    }

    public void selecionarArquivo() {
        bolArq = true;
        FileInputStream fis;
        try {

            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Escolha o arquivo");

            if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                File fileSelected = chooser.getSelectedFile();

                byte[] bFile = new byte[(int) fileSelected.length()];
                fis = new FileInputStream(fileSelected);
                fis.read(bFile);
                fis.close();

                long kbSize = fileSelected.length() / 1024;
                txtMsg.setText(fileSelected.getName());
                //lblTamanho.setText(kbSize + " KB");

                arquivo = new Arquivo();
                arquivo.setConteudo(bFile);
                arquivo.setDataHoraUpload(new Date());
                arquivo.setNome(fileSelected.getName());
                arquivo.setTamanhoKB(kbSize);
                arquivo.setIpDestino(txtIP.getText());
                arquivo.setPortaDestino(txtPorta.getText());
                arquivo.setDiretorioDestino(fileSelected.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método usado para receber mensagem do servidor
    public void receber() throws IOException{
        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";

        while(!"Sair".equalsIgnoreCase(msg))
            if(bfr.ready()){
                msg = bfr.readLine();
                if(msg.equals("Sair"))
                    texto.append("Servidor.Servidor caiu! \r\n");
                else
                    texto.append(msg+"\r\n");
            }
    }


    // Método usado quando o usuário clica em sair
    public void sair() throws IOException{
        enviarMensagem("Sair");
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
    }

    // Método que recebe as ações dos botões do usuário
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                enviarMensagem(txtMsg.getText());
            else
            if(e.getActionCommand().equals(btnArq.getActionCommand()))
                selecionarArquivo();
            if(e.getActionCommand().equals(btnSair.getActionCommand()))
                sair();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    // Método que manda mensagem quando o enter é pressionado
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                enviarMensagem(txtMsg.getText());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) throws IOException {
        Cliente app = new Cliente();
        app.conectar();
        app.receber();
    }

}
